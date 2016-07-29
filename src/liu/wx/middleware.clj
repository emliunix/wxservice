(ns liu.wx.middleware
  (:require [liu.wx.util :refer [check-params
                                 extract-params
                                 message-invalid-params]]
            [clojure.tools.logging :as log]))

(defn wrap-logging [handler]
  (fn [req]
    (let [method (:request-method req)
          uri (:uri req)
          query-string (:query-string req)]
      (log/info method uri query-string)
      (handler req))))

(def wx-params-spec
  {"timestamp" :timestamp
   "signature" :signature
   "nonce" :nonce
   "echostr" [:echostr :optional]})

(defn wrap-wx-params [handler & opts]
  "This middleware should be wrapped by wrap-params.
   It takes :err as the error handler, :token as the app token,
   :aes-key as the AesEncoding and Decoding key."
  ;; extract options
  (let [opts (apply array-map opts)
        {err-handler :err
         wx-token :token
         aes-key :aes-key} opts]
    (fn [req]
      (log/info "Extracting weixin parameteres.")
      (let [req (assoc req
                       :wx-token wx-token
                       :wx-aes-key aes-key)
            params (:params req)]
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
