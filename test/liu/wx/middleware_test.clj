(ns liu.wx.middleware-test
  (:require [liu.wx.middleware :as t]
            [clojure.test :refer [deftest is]]))

(def req {:params {:timestamp "1409735669"
                   :signature "46aa4ce40ec889062d2c3606257bee4b24ed26f4"
                   :nonce "1320562132"
                   :echostr "Hello world!!!"}})

#_(deftest get-weixin-test
  (is (= (get-in req [:params :echostr])
         (:body (t/get-weixin req)))))
