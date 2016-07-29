(ns liu.wx.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.logging :as log]
            [liu.wx.middleware :refer [wrap-logging wrap-wx-params]]
            [liu.wx.config :refer [get-config]]
            [liu.wx.signature :refer [sign]])
  (:gen-class))

(defn debug-handler [req]
  (let [content (slurp (:body req))]
    (log/info "Msg send in: " (:params req) content)
    {:body content
     :headers { "Content-Type" "text/html" }
     :status 200 }))

(defn wx-verifyurl-handler [req]
  "Wrap this in wrap-wx-params."
  (log/info "Verifing token...")
  (let [{signature :signature
         timestamp :timestamp
         nonce :nonce
         echo :echostr} (:wx-params req)
        token (:wx-token req)
        ;; signature generated localy
        sign-gen (sign token timestamp nonce "")]
    (if (= signature sign-gen)
      ;; then send back echo str
      (do (log/info "Signature verified: " sign-gen)
          {:body echo})
      ;; else error msg
      (do (log/error "Signature verification failed:\n"
                     " expected->" signature "\n"
                     " actual->" sign-gen "\n"
                     " [token timestamp nonce] => " [token timestamp nonce])
          {:status 400 :body "Error"}))))

(defn wx-params-err-handler [req]
  {:body (str "<ul style='color: red;'>"
              (apply str (map  #(str "<li>" % "</li>") (:wx-err-msg req)))
              "</ul>")
   :headers { "Content-Type" "text/html" }
   :status 400})

;; configs
(def token (get-config "TOKEN"))
(def aeskey (get-config "AES_KEY"))

(defn router [& routes]
  (let [routes (apply array-map routes)]
    (fn [req]
      ((routes (:request-method req)
               (routes :default
                       (fn [req] {:status 404
                                  :body "No routes defined"})))
       req))))

(def app
  (-> (router
       :get wx-verifyurl-handler
       :default debug-handler)
      (wrap-wx-params
       :token token
       :err wx-params-err-handler)
      wrap-params
      wrap-logging))
