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
