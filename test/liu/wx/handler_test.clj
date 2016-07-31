(ns liu.wx.handler-test
  (:require [clojure.test :refer [deftest is]]
            [liu.wx.handler :as t]))

(deftest wx-verifyurl-handler-test
  (is (= 200
         (:status (t/wx-verifyurl-handler {:wx-params {:echostr "hello"}}))))
  (is (= 400
         (:status (t/wx-verifyurl-handler {:wx-params {}})))))
