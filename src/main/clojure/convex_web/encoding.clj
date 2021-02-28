(ns convex-web.encoding
  (:require [cognitect.transit :as t]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (java.io ByteArrayOutputStream InputStream)))

(defn transit-decode-string
  "Decode a Transit-JSON encoded string `s`."
  [^String s]
  (t/read (t/reader (io/input-stream (.getBytes s)) :json)))

(defn transit-decode [^InputStream x]
  (when x
    (t/read (t/reader x :json))))

(defn transit-encode
  "Encode `x` in Transit-JSON; or as `(str x)` if the encoding fails.

   Returns a Transit JSON-encoded string."
  [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (t/writer out :json)]
    (try
      (t/write writer x)
      (.toString out)
      (catch Exception ex
        (log/error "Can't Transit encode " (prn-str x))
        (throw ex)))))
