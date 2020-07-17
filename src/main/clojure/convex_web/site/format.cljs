(ns convex-web.site.format
  (:require [goog.i18n.NumberFormat]
            [goog.i18n.NumberFormat.Format])
  (:import (goog.i18n DateTimeParse)
           (goog.date DateTime)))

(defn date-time-from-millis [timestamp]
  (DateTime/fromTimestamp timestamp))

(defn date-time-to-string [^goog.date.DateTime date-time]
  (.toUTCRfc3339String date-time))

(defn format-number [n]
  (.format (goog.i18n.NumberFormat. goog.i18n.NumberFormat.Format/DECIMAL) n))
