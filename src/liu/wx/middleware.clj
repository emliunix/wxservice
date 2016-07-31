(ns liu.wx.middleware
  (:require [liu.wx.util :refer [check-params
                                 extract-params
                                 message-invalid-params
                                 curr-timestamp
                                 gen-nonce
                                 flat-xml
                                 to-xml]]
            [liu.wx.aes :refer [encrypt decrypt]]
            [liu.wx.signature :refer [sign]]
            [liu.wx.handler :refer [wx-verifyurl-handler
                                    wx-params-err-handler]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log])
  (:import [java.io StringReader]))

;; wrap-post-data
(defn wrap-post-data [handler]
  "Intercept post data and make it 
   available as :post-data of String type"
  (fn [req]
    (let [body (:body req)]
      (if-not (nil? body)
        (let [body-str (if (string? body)
                         body
                         (slurp body))]
          (handler (assoc
                    ;; remap :body to a stringreader of body-str for compatibility
                    req
                    :body (StringReader. body-str)
                    :post-data body-str)))
        ;; else do nothing but propagate the chain down
        (handler req)))))

;; logging middleware
(defn wrap-logging [handler]
  (fn [req]
    (let [method (:request-method req)
          uri (:uri req)
          query-string (:query-string req)]
      (log/info method uri query-string)
      (handler req))))

(def ^:private wx-params-spec
  {"timestamp" :timestamp
   "signature" :signature
   "nonce" :nonce
   "encrypt_type" [:encrypt-type :optional]
   "msg_signature" [:msg-signature :optional]
   "echostr" [:echostr :optional]})

;; query string parameter extractor
(defn wrap-wx-params [handler & opts]
  "This middleware should be wrapped by wrap-params.
   It takes :err as the error handler, :token as the app token,
   :aes-key as the AesEncoding and Decoding key."
  ;; extract options
  (let [opts (apply array-map opts)
        err-handler (:err opts)]
    (fn [req]
      (log/info "Extracting weixin parameteres.")
      (let [params (:params req)]
        ;; check validity of weixin parameters
        (if (check-params wx-params-spec params)
          ;; weixin specific parameters
          (let [wx-params (extract-params
                           wx-params-spec
                           params)]
            (do (log/info "Parameters OK.")
                (handler (assoc req :wx-params wx-params))))
          ;; parameter error
          (let [req (assoc req
                           :wx-err-msg
                           (message-invalid-params wx-params-spec params))]
            (log/info "Parameters invalid. spec: " wx-params-spec " params: " params)
            (if err-handler
              (err-handler req)
              (handler req))))))))


;; aes encryption and decryption middleware
;; There are several extra parameters need consideration
;; * encrypt_type
;; * msg_signature
;; and the difference between signature and msg_signature
;; should also be clarified.
;; => signature if the signature of querystring parameteres
;; [token timestamp nonce].
;; while msg_signature is sha1 of [token timestamp nonce encrypt]
;; After verification. msg shall be extracted out from xml
;; then decrypted, 

;; 原先没有想到verifyurl除了最开始的GET用到，每一个请求都有用到。
;; 原先把这个功能写成了handler, 现在要改成middleware了。

(defn wrap-wx-sign [handler & opts]
  "Need wrap-wx-params.
   This is used to verify signature of query (or say header?)."
  ;; paramteres 
  (let [opts (apply array-map opts)
        {token :token} opts]
    ;; the handler
    (fn [req]
      (log/info "Checking signature...")
      (let [wx-params (:wx-params req)
            {signature :signature
             timestamp :timestamp
             nonce :nonce} wx-params
            ;; signature generated localy
            sign-gen (sign token timestamp nonce "")]
        (if (= signature sign-gen)
          ;; if it's a get request. then send back echo str
          (handler req)
          ;; else error msg
          (do (log/error "Signature verification failed:\n"
                         " expected->" signature "\n"
                         " actual->" sign-gen "\n"
                         " [token timestamp nonce] => " [token timestamp nonce])
              {:status 400 :body "Signature Verification Error"}))))))

(defn- extract-aes-body [data aes-key appid]
  "takes a map with :encrypt, returns a merge or original map
   and the map decrypted from :encrypt."
  (let [encrypt (:Encrypt data)]
    (merge data
           (flat-xml (decrypt aes-key appid encrypt)))))

(defn- encrypt-aes-body [body aes-key appid token]
  "encrypt body(string)"
  (let [new-encrypt (encrypt aes-key appid body)
        new-nonce (gen-nonce)
        new-timestamp (curr-timestamp)
        new-signature (sign token new-timestamp new-nonce new-encrypt)]
    (to-xml {:Encrypt new-encrypt
             :MsgSignature new-signature
             :TimeStamp new-timestamp
             :Nonce new-nonce})))

(defn wrap-wx-aes [handler & opts]
  "Needs wrap-wx-params & wrap-wx-sign.
  the :aes-key options is required.
  Data is available in :wx-data in req."
  (let [opts (apply array-map opts)
        token (:token opts)
        aes-key (:aes-key opts)
        appid (:appid opts)]
    ;; the handler
    (fn [req]
      ;; 如果没有请求体
      ;; *FIX* 用nil?检查不出来，改成直接检查是不是post请求
      ;; 只针对POST请求进行处理
      (if-not (= :post (:request-method req))
        (handler (assoc req :wx-data nil))
        ;; 否则根据参数选择是否需要解密，
        ;; 并将最终的xml数据以map的形式放在req的:wx-data上。
        (let [wx-params (:wx-params req)
              encrypt-type (:encrypt-type wx-params :none)
              post-data (:post-data req)
              ;; 将body-str转换为一个map(body-str必须是一个xml)
              data (try (flat-xml post-data)
                        (catch Exception e nil))]
          (if (nil? data)
            ;; body不是一个xml
            {:status 400 :body (str "body is not an xml => " post-data)}
            ;; 检查是否需要加解密
            (if (= "aes" encrypt-type)
              (let [res (handler (assoc req :wx-data
                                        (extract-aes-body
                                         data
                                         aes-key
                                         appid)))
                    res-body (:body res)]
                (if-not (nil? res-body)
                  ;; 如果存在返回内容，加密返回内容
                  (assoc res :body (encrypt-aes-body
                                    res-body
                                    aes-key
                                    appid
                                    token))
                  ;; 否则直接返回
                  res))
              ;; 当不采用aes加密时，直接调用 handler
              (handler (assoc req :wx-data data)))))))))

(defn wrap-wx [handler & opts]
  (let [opts (apply array-map opts)
        {token :token
         appid :appid
         aes-key :aes-key} opts]
    (-> handler
        (wrap-wx-aes
         :token token
         :appid appid
         :aes-key aes-key)
        (wrap-wx-sign
         :token token)
        (wrap-wx-params
         :err wx-params-err-handler)
        (wrap-params)
        (wrap-post-data)
        (wrap-logging))))
