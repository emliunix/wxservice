(ns liu.wx.signature
  (:require [clojure.string :refer [join]]))

(defn hexstr [data-bytes]
  (apply str 
         (map 
          #(.substring 
            (Integer/toString 
             (+ (bit-and % 0xff) 0x100) 16) 1) 
          data-bytes)))

(defn sha1 [s]
  (hexstr
   (.. (java.security.MessageDigest/getInstance "sha1")
       (digest (.getBytes s)))))

(defn gen-signature [token timestamp nonce encrypt]
  (->> [token timestamp nonce encrypt]
       sort
       str
       sha1))
