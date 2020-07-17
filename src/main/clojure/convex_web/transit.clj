(ns convex-web.transit
  (:require [cognitect.transit :as t]
            [clojure.java.io :as io]))

(defn decode-string [^String s]
  (t/read (t/reader (io/input-stream (.getBytes s)) :json)))
