(ns liu.wx.aes-test
  (:require [clojure.test :refer [deftest is are]]
            [liu.wx.util :refer [cat-bytes]]
            [liu.wx.aes :as t])
  (:import [java.util Arrays]))

;; These are values cited from official sample code of weixin,
;; so the reliability of the code is guranted if decryption functions.
;; However, encryption is not possiblly tested against the official
;; data since encryption makes use of random values
(def aes-key "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG")
(def appid "wx2c2769f8efd9abc2")
(def sec-msg "1B0w3EEkJGo/5Pc2sd9ZNhcSJKJrbhqcTT4DIh3hqRlRroeD1G8+UCT8RNLZUrm+OArzFmcJNqNhcSvkERnZ+XKE2gLY63KD2hGLKM+WVojzAWEc6DLq2H15r/QWtunLP1YY8u95ktcu9FSBKTC42R8gWUlyh1URB7a5xrigXesazHud17+ioZk9CSw5TlylzuW/EIN1xiEdhi5/j4S4jz4JgS8pQ4rPt9c5b+26qh4Jp94Lwnm2eJb54g5xKBbesyZ0WtdxCzcbh7yvDAn+t6i81Hm/dNOSLDsTsxp3mfnO0iaaGQdVW49Dc/RLzjDSwawK4y8OybDN1YxPALbEAHOKFAa5GBn30vZL9Ka9MRpxdWuAo73jsySRwUHj94Ej1Tuy0uvW7xoz8NM17+8wZGRlgrHVsv5FebaaQR9HhHkvDVRpdtg7janXlUiNPB+qloQaktrUZxk3yAQUvle59oBaVwRitC2e0aS8DC0s1OoJRgCY004tTkMCw6IUPFJgvRgUkVBBk6jg1ES36ZgkgacAMiWEcnmnpwXJ++eN6wnhKM3BXH6guAwy+Nqd4juHG1bLIyNN05kzA/+u3Plz4KVY+OQPxxOyekCgDlKRXhwQpF7d8zYiUqfu1Wsrf9wc")
(def msg " <xml><ToUserName><![CDATA[oia2TjjewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType>  <![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Descript  ion><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>")

(deftest int32-to-nbo-test
  (is (Arrays/equals
       ;; the expected byte array
       (into-array Byte/TYPE (map byte [1 2 3 4]))
       (t/int32-to-nbo 16909060))))

(deftest nbo-to-int32-test
  (is (= 16909060
         (t/nbo-to-int32 (byte-array [1 2 3 4])))))

(deftest pkcs7-encode-test
  (is (Arrays/equals (byte-array 32 (byte 32))
                     (t/pkcs7-encode 0))
      "pkcs7-encode should return an 32-bytes length array of 32")
  (is (Arrays/equals (byte-array 32 (byte 32))
                     (t/pkcs7-encode 64))
      "exact n x 32 length, byte[32] of 32 should be returned.")
  (is (Arrays/equals (byte-array 22 (byte 22))
                     (t/pkcs7-encode 42))))

(deftest pkcs7-decode-test
  (is (Arrays/equals
       (byte-array 123)
       (t/pkcs7-decode (cat-bytes (byte-array 123) (t/pkcs7-encode 123))))))


(deftest random-16bytes-test
  (is (= 16
         (alength (t/random-16bytes))))
  (is (= (type (bytes (byte-array 0)))
         (type (t/random-16bytes)))))

;; Test encryption and decryption
;; Since encrypt make use of random 16-len bytes internally,
;; the encrypted msg is undetermined
;; Instead decrypt it and test the equality of the msg decrypted
;; with the original message.
(deftest cryption-test
  (is (= msg
         (t/decrypt aes-key appid (t/encrypt aes-key appid msg)))))

;; Test decryption functionality
(deftest decrypt-test
  (is (= msg
         (t/decrypt aes-key appid sec-msg))))
