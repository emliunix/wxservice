(ns liu.wx.router-test
  (:require [clojure.test :refer [deftest is are]]
            [liu.wx.router :as t]))

(defn- mock-msg [msg-type]
  {:MsgType msg-type})

(def ^:private event-prototype {:MsgType "event"})

(defn- mock-event [event-type]
  (merge event-prototype
         (if (= "subscribe-and-scan" event-type)
           {:Event "subscribe"
            :EventKey "qrscene_xxxxxx"}
           {:Event event-type})))

(deftest get-msg-type-test
  (are [a b] (= a (#'liu.wx.router/get-msg-type b))
    :text       (mock-msg "text")
    :image      (mock-msg "image")
    :voice      (mock-msg "voice")
    :video      (mock-msg "video")
    :shortvideo (mock-msg "shortvideo")
    :location   (mock-msg "location")
    :link       (mock-msg "link")

    :subscribe            (mock-event "subscribe")
    :unsubscribe          (mock-event "unsubscribe")
    :subscribe-and-scan   (mock-event "subscribe-and-scan")
    :SCAN                 (mock-event "SCAN")
    :LOCATION             (mock-event "LOCATION")
    :CLICK                (mock-event "CLICK")
    :VIEW                 (mock-event "VIEW")
    ))
