(ns liu.wx.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.logging :as log]
            [liu.wx.middleware :refer [wrap-logging
                                       wrap-wx
                                       wrap-post-data]]
            [liu.wx.config :refer [get-config]]
            [liu.wx.router :refer [router]]
            [liu.wx.handler :refer [wx-verifyurl-handler
                                    msg-text-handler]])
  (:import [java.io StringReader])
  (:gen-class))

(defn debug-handler [req]
  (let [content (:body req)]
    (log/info "Msg send in: " (:params req) content)
    {:body "This is debug content"
     :headers { "Content-Type" "text/html" }
     :status 200 }))

;; configs
(def token (get-config "TOKEN"))
(def aeskey (get-config "AES_KEY"))
(def appid (get-config "APPID"))
(def appsecret (get-config "APPSECRET"))

(defn dispatch [& routes]
  (let [routes (apply array-map routes)]
    (fn [req]
      ((routes (:request-method req)
               (routes :default
                       (fn [req] {:status 404
                                  :body "No routes defined"})))
       req))))

(def routes
  {:text msg-text-handler
   :invalid debug-handler})

(def app
  (-> (dispatch
       :get wx-verifyurl-handler
       :default (router routes))
      (wrap-wx
       :token token
       :aes-key aeskey
       :appid appid)))

#_(def app
  (fn [req]
    (log/info (slurp (:body req)))))
