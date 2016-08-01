(ns liu.wx.handler
  (:require [clojure.tools.logging :as log]))

(defn wx-params-err-handler [req]
  {:body (str "<ul style='color: red;'>"
              (apply str (map  #(str "<li>" % "</li>") (:wx-err-msg req)))
              "</ul>")
   :headers { "Content-Type" "text/html" }
   :status 400})

(defn wx-verifyurl-handler [req]
  "The echo back handler for initial verification get request from weixin."
  (if-let [echo (get-in req [:wx-params :echostr])]
    {:body echo :status 200}
    (do (log/error "Echostr parameter not present.")
        {:body "Echostr parameter not present." :status 400})))


;; event handlers
;; Messages
;; * text
;; * image
;; * voice
;; * video
;; * shortvideo
;; * location
;; * link
;; Events:
;; * subscribe / unsubscribe
;; * subscribe / SCAN 二维码场景扫描
;; * LOCATION
;; * CLICK
;; * VIEW
(defn msg-text-handler [req]
  (let [wx-data (:wx-data req)
        {touser :ToUserName
         fromuser :FromUserName
         ctime :CreateTime
         msgid :MsgId
         ctnt :Content} wx-data]
    (log/infof "%s send [%s: %s] to %s at %s" fromuser touser msgid ctnt ctime)
    {:body ctnt :status 200}))
