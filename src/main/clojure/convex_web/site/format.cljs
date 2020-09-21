(ns convex-web.site.format
  (:require [goog.i18n.NumberFormat]
            [goog.i18n.NumberFormat.Format]
            [clojure.string :as str])
  (:import (goog.i18n DateTimeParse)
           (goog.date DateTime)))

(defn date-time-from-millis [timestamp]
  (DateTime/fromTimestamp timestamp))

(defn date-time-to-string [^goog.date.DateTime date-time]
  (.toUTCRfc3339String date-time))

(defn format-number [n]
  (.format (goog.i18n.NumberFormat. goog.i18n.NumberFormat.Format/DECIMAL) n))

(defn address-blob [address]
  (when-not (str/blank? address)
    (if (str/starts-with? address "0x")
      address
      (str "0x" address))))
