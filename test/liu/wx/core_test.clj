(ns liu.wx.core-test
  (:require [liu.wx.core :as t]
            [liu.wx.middleware :refer [wrap-wx-params]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.test :refer [deftest is]]))
