(ns liu.wx.config
  (:require [clojure.java.io :as io])
  (:import [java.util Properties]))

(defn get-env [k]
  (System/getenv (str k)))

(defn get-java-prop [k]
  (System/getProperty k))

(def ^:dynamic *wx-prop-file* "config.properties")

(defn- -get-prop [k]
  (..
   (doto (Properties.)
     (.load (io/reader (io/resource *wx-prop-file*))))
   (getProperty k)))

(def get-prop (memoize -get-prop))

(defn- get-multi-source [k & fs]
  (loop [[f & fs] fs]
    (when (not (nil? f))
      (if-let [v (f k)]
        v
        (recur fs)))))

(defn get-config [k]
  (get-multi-source k get-env get-prop get-java-prop))
