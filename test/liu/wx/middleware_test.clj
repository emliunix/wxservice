(ns liu.wx.middleware-test
  (:require [liu.wx.middleware :as t]
            [liu.wx.aes :refer [decrypt]]
            [liu.wx.util :refer [flat-xml]]
            [clojure.test :refer [deftest is]]
            [ring.middleware.params :refer [wrap-params]])
  (:import [java.io StringReader ByteArrayInputStream]))

(def req {:params {:timestamp "1409735669"
                   :signature "46aa4ce40ec889062d2c3606257bee4b24ed26f4"
                   :nonce "1320562132"
                   :echostr "Hello world!!!"}})

#_(deftest get-weixin-test
  (is (= (get-in req [:params :echostr])
         (:body (t/get-weixin req)))))

(def token "emtoken")
(def aes-key "DOZEPQXatVT1RbgvP4nFuACCoixAdPlAR1T51aU33c4")
(def appid "wxbe4700010b5f32f7")
(def query "signature=2f3200ec27097e9d7880e14db97cff16e4a6b86d&echostr=8197925266815712683&timestamp=1469801643&nonce=1972100835")
(def echostr-in-query "8197925266815712683")

(def mock-req-get
  {:request-method :get
   :query-string query})

(def query-post "signature=54c894b2aca4bd19ef8361c2b431519fb386406a&timestamp=1469851040&nonce=1959607&encrypt_type=aes&msg_signature=ef53661f8e4cafaaaf0b67aa58661d7d79cd33eb")
(def post-data "<xml>
    <ToUserName><![CDATA[emliunix_road]]></ToUserName>
    <Encrypt><![CDATA[5925NxCtqSYvTH7ffc0V22eV0pRnFGrWrhpz70pb4Tto6Qaf4PHj3RyjPP33+NHN65c8MDZixEm+6gVV+jK/rNDKPqBnECJtZrRQFdI1laKfrcZg5+2fzpM3+XsjElg7/+bSKJZE4LIfdh7gYzVkzyND2Bg36v4Dvn8H8dgCKo5i/9pcAmWynq0Nn53VA37NyhXCP/rcWBJ2JqaioZKyOweoaLtLNOR5+5095IcwdZT9/Z5IaNwAsiOzB3irHYozhvHFeRFHv6ikzsVqN4P3icwK/554gt/YMw+pPUM3j8ACmpJO8ZHRMqBQIPxsZtYbqRHmqGWit4oZaSE3+uh2T3tD1dI78eJMADEo1+XHmVJcglpAMieWSDoAPpVtkj4y+ddvTkJrQcu8JmNi0MWXPi/bfHPLGUseNUfbZdTZShswo+9FNJTz+9hPSc5wnFz5GFI80ZsdLzNks1zbpxJhSw==]]></Encrypt>
</xml>")

;; make it a function since a new StringReader
;; shall be created each time a request is
;; needed.
(defn mock-req-post []
  {:request-method :post
   :query-string query-post
   :body (StringReader. post-data)})

(deftest wrap-wx-test
  ;; get test
  (is (= {:body echostr-in-query :status 200}
         ((-> (fn [req] {:status 200 :body (get-in req [:wx-params :echostr])})
              (t/wrap-wx
               :token token
               :aes-key aes-key
               :appid appid)
              wrap-params)
          mock-req-get)))
  ;;post test
  (is (= "test"
         ;; pipe response into further processing to extract out encrypted data.
         (->> ((-> (fn [req] {:status 200 :body "test"})
                   (t/wrap-wx
                    :token token
                    :aes-key aes-key
                    :appid appid)
                   wrap-params)
               (mock-req-post))
              :body
              flat-xml
              :Encrypt
              (decrypt aes-key appid)))))

;; wrap post data test
(deftest wrap-post-data-test
  (is (= "test post data"
         (-> ((-> (fn [req]
                    ;; 将post-data通过:body传回
                    {:body (:post-data req)})
                  ;; 请求体现在被读取到:post-data中了
                  (t/wrap-post-data))
              ;; mock 一个 request
              {:body (ByteArrayInputStream. (.getBytes "test post data"))})
             ;; 将 body 的内容解出来
             :body))))
