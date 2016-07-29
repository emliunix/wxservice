(ns liu.wx.signature
  (:require [clojure.string :refer [join]]
            [liu.wx.util :refer [hexstr]]))

(defn- sha1 [s]
  (hexstr
   (.. (java.security.MessageDigest/getInstance "sha1")
       (digest (.getBytes s)))))

(defn sign [token timestamp nonce encrypt]
  (->> [token timestamp nonce encrypt]
      sort
      (apply str)
      sha1))
