(ns liu.wx.util
  (:require [clojure.tools.logging :as log]
            [clojure.data.xml :as xml]))

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

(defn gen-nonce []
  (apply str
         (repeatedly 16 #(Integer/toString (rand-int 16) 16))))

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

;; 对于微信这种没有attribute的xml封装两个函数

(defn- xml-els-to-map [els]
  (if (every? string? els)
    ;; 当 子元素 都是字符串的时候，返回一个字符串或者一个字符串的vector
    (if (= 1 (count els))
      (first els)
      els)
    (reduce
     (fn [acc el]
       ;; 只处理 element 元素
       (if (= clojure.data.xml.Element (type el))
         (conj acc [(:tag el)
                    (xml-els-to-map (:content el))])
         acc))
     {}
     els)))

(defn flat-xml [xml-str]
  "Ignores all attributes. Make xml simple nested maps."
  ;; *FIX* 当els既包含字符串又包含elemnt的时候，不知道怎么处理。
  (let [els (:content (xml/parse-str xml-str))]
    (xml-els-to-map els)))

(defn- to-xml-sexp [els]
  (map
   (fn [[k v]]
     [k {} (if (map? v) (to-xml-sexp v) [:-cdata v])])
   els))

(defn to-xml [els]
  (xml/emit-str
   (xml/sexp-as-element
    [:xml {} (to-xml-sexp els)])))
