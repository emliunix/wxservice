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

(deftest curr-timestamp-test
  (is (let [ts (t/curr-timestamp)]
        (and (string? ts)
             (re-matches #"\d+" ts)))))
  
(deftest gen-nonce-test
  (is (let [n (t/gen-nonce)]
        (string? n))))

;; *TODO* test for check-params & extract-params & message-invalid-params

;; test for xml related functiosn

(deftest flat-xml-test
  (is (= {:hello "String"
          :world "sTRING"}
         (t/flat-xml "<xml><hello><![CDATA[String]]></hello><world><![CDATA[sTRING]]></world></xml>"))))

(deftest to-xml-sexp-test
  (is (= [:xml {} (list [:El1 {} [:-cdata "String 1"]]
                    [:El2 {} (list [:El3 {} [:-cdata "String 2"]]
                               [:el4 {} [:-cdata "String 3"]])])]
         [:xml {} (#'liu.wx.util/to-xml-sexp {:El1 "String 1"
                                              :El2 {:El3 "String 2"
                                                    :el4 "String 3"}})])))

(deftest to-xml-test
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><el1><![CDATA[String 1]]></el1><el2><el3><![CDATA[String 2]]></el3><el4><![CDATA[String 3]]></el4></el2></xml>"
         (t/to-xml {:el1 "String 1"
                    :el2 {:el3 "String 2"
                          :el4 "String 3"}}))))
