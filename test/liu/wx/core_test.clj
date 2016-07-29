(ns liu.wx.core-test
  (:require [liu.wx.core :as t]
            [liu.wx.middleware :refer [wrap-wx-params]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.test :refer [deftest is]]
            ))

(def token "emtoken")
(def aes-key "DOZEPQXatVT1RbgvP4nFuACCoixAdPlAR1T51aU33c4")
(def query "signature=2f3200ec27097e9d7880e14db97cff16e4a6b86d&echostr=8197925266815712683&timestamp=1469801643&nonce=1972100835")

(def mock-req
  {:request-method :get
   :query-string query})

(deftest wx-verifyurl-handler
  (is (= {:body "test"}
         ((-> t/wx-verifyurl-handler
              (wrap-wx-params
               :token token
               :aes-key aes-key)
              wrap-params)
          mock-req))))
