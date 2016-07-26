(ns liu.wx.util-test
  (:require [clojure.test :refer [deftest is]]
            [liu.wx.util :as t])
  (:import [java.util Arrays]))

(deftest hexstr-test
  (is (= "010203041a"
         (t/hexstr (byte-array [1 2 3 4 26])))))

(deftest cat-bytes
  (is (Arrays/equals (byte-array [1 2 3 4 26 10])
                     (t/cat-bytes (byte-array [1 2 3])
                                  (byte-array [4 26 10])))))
