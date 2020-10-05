(ns convex-web.encoding
  (:require [cognitect.transit :as t]
            [clojure.java.io :as io])
  (:import (java.io ByteArrayOutputStream InputStream)))

(defn transit-decode-string [^String s]
  (t/read (t/reader (io/input-stream (.getBytes s)) :json)))

(defn transit-decode [^InputStream x]
  (when x
    (t/read (t/reader x :json))))

(defn transit-encode [x]
  (try
    (let [out (ByteArrayOutputStream. 4096)
          writer (t/writer out :json)]
      (t/write writer x)
      (.toString out))
    (catch Exception _
      (transit-encode (str x)))))
