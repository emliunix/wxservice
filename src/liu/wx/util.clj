(ns liu.wx.util
  (:require [clojure.tools.logging :as log]))

(defn hexstr [data-bytes]
  (apply str 
         (map 
          #(.substring 
            (Integer/toString 
             (+ (bit-and % 0xff) 0x100) 16) 1) 
          data-bytes)))

(defn cat-bytes [& bytes-list]
  (let [result (byte-array (->> bytes-list
                                (map alength)
                                (reduce +)))]
    (loop [idx 0
           [curr & more] bytes-list]
      (let [len-curr (count curr)]
        (when (not (nil? curr))
          (System/arraycopy curr 0 result idx len-curr)
          (recur (+ idx len-curr) more))))
    result))

(defn curr-timestamp []
  (str (quot (System/currentTimeMillis) 1000)))

;; parameter checker and extractor

(defn check-params [spec params]
  (reduce
   #(and %1 %2)
   (map
    (fn mapfn [[k v]]
      (if (and
           (sequential? v)
           (some (partial = :optional) v))
        true
        (contains? params k)))
    spec))) 

(defn extract-params [spec params]
  (reduce
   (fn [acc-map [k newk]]
     (let [key (if (sequential? newk)
                 (first newk)
                 newk)]
       (if (contains? params k)
         (assoc acc-map key
                (get params k nil))
         acc-map)))
   {}
   spec))

(defn message-invalid-params [spec params]
  (reduce
   (fn [msgs [k v]]
     (if (and
          ;; parameter is required
          (keyword? v)
          ;; but no corresponding entry found
          (not (contains? params k)))
       (conj msgs (str k " is missing."))
       msgs))
   []
   spec))
