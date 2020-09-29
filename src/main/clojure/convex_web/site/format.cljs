(ns convex-web.site.format
  (:require [goog.i18n.NumberFormat]
            [goog.i18n.NumberFormat.Format]
            [clojure.string :as str]

            ["timeago.js" :as timeago])
  (:import (goog.i18n DateTimeParse)
           (goog.date DateTime)))

(defn date-time-from-millis [timestamp]
  (DateTime/fromTimestamp timestamp))

(defn date-time-to-string [^goog.date.DateTime date-time]
  (.toUTCRfc3339String date-time))

(defn format-number [n]
  (.format (goog.i18n.NumberFormat. goog.i18n.NumberFormat.Format/DECIMAL) n))

(defn time-ago
  "Returns a time-ago friendly text.

   `timestamp` can be a UTC RFC 3339 string, or the number of millis."
  [timestamp]
  (let [timestamp (if (number? timestamp)
                    (-> timestamp
                        (date-time-from-millis)
                        (date-time-to-string))
                    timestamp)]
    (timeago/format timestamp)))

(defn prefix-0x [s]
  (when-not (str/blank? s)
    (if (str/starts-with? s "0x")
      s
      (str "0x" s))))

(defn trim-0x [s]
  (when-not (str/blank? s)
    (str/replace s #"^0x" "")))
