(ns liu.wx.aes
  (:require [liu.wx.util :refer [hexstr cat-bytes]])
  (:import [javax.crypto Cipher]
           [javax.crypto.spec SecretKeySpec IvParameterSpec]
           [org.apache.commons.codec.binary Base64]
           [java.nio ByteBuffer ByteOrder]
           [java.util Arrays]))

(defn -aeskeybytes [aeskey]
  (let [bs (Base64/decodeBase64 aeskey)]
    (println (alength bs))
    bs))

(def aeskeybytes (memoize -aeskeybytes))

(defn -aes [mode aeskeybs ctnt]
  (-> (doto (Cipher/getInstance "AES/CBC/NoPadding")
        (.init
         ({:encrypt Cipher/ENCRYPT_MODE
           :decrypt Cipher/DECRYPT_MODE}
          mode mode)
         (SecretKeySpec. aeskeybs "AES")
         (IvParameterSpec. aeskeybs 0 16)))
      (.doFinal ctnt)))

(defn int32-to-nbo [n]
  (let [b (ByteBuffer/allocate 4)
        arr (byte-array 4)]
    (doto b
      ;; set it to network byte order
      (.order ByteOrder/BIG_ENDIAN)
      (.putInt n)
      (.flip)
      (.get arr))
    arr))

(defn nbo-to-int32 [bs]
  (-> (doto (ByteBuffer/wrap bs)
        (.order ByteOrder/BIG_ENDIAN))
    (.getInt)))

(defn random-16bytes []
  (into-array Byte/TYPE
   (map #(byte %)
        (take 16 (iterate (fn [x] (rand-int 16)) (rand-int 16))))))

(defn pkcs7-encode [size]
  (let [-np (- 32 (mod size 32))
        np (if (= 0 -np) 32 -np)]
    (byte-array np (byte np))))

(defn pkcs7-decode [ctnt]
  (let [len (alength ctnt)
        pad-b (aget ctnt (dec len))]
    (if (or (< pad-b 1) (> pad-b 32))
      ctnt
      (Arrays/copyOfRange ctnt 0 (- len pad-b)))))

(defn encrypt
  "Convert text content into BASE64 encoded String(Not URLSafe)"
  ([key appid ctnt] (encrypt key appid ctnt "UTF-8"))
  ([key appid ctnt charset]
   (let [rsixteen-bs (random-16bytes) ; 16B 的随机串
         ctnt-bs (.getBytes ctnt charset) ; ctnt 的 byte[] 形式
         ; ctnt 长度转换为网络字节序的 4字节数据
         nbo-bs (int32-to-nbo (alength ctnt-bs)) 
         ;; 原始数据长度
         appid-bs (.getBytes appid)
         len-raw (reduce + (map
                            alength
                            [rsixteen-bs nbo-bs ctnt-bs appid-bs]))
         ;; padding 数据
         padding-bs (pkcs7-encode len-raw) 
         ]
     (Base64/encodeBase64String
      (-aes
       :encrypt
       (aeskeybytes key)
       ;; 将以上数据按照如下顺序组合
       ;; [rsixteen-bs nbo-bs ctnt-bs appid-bs padding-bs]
       (cat-bytes
        rsixteen-bs
        nbo-bs
        ctnt-bs
        appid-bs
        padding-bs))))))

(defn decrypt
  "Convert BASE64 encoded encrypted string to uncrypted text content with
  the specified encoding."
  ([key appid sec-ctnt] (decrypt key appid sec-ctnt "UTF-8"))
  ([key appid sec-ctnt charset]
   (let [raw (-aes :decrypt (aeskeybytes key) (Base64/decodeBase64 sec-ctnt))
         unpadded-raw (pkcs7-decode raw)
         nbo-bs (Arrays/copyOfRange unpadded-raw 16 20)
         ctnt-size (nbo-to-int32 nbo-bs)
         ctnt (Arrays/copyOfRange unpadded-raw 20 (+ 20 ctnt-size))
         msg-appid (String. (Arrays/copyOfRange unpadded-raw (+ 20 ctnt-size) (alength unpadded-raw)))]
     (when (= appid msg-appid)
       (String. ctnt charset)))))
