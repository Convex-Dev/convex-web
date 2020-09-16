(ns convex-web.explorer
  (:require [convex-web.config :as config]))

(def min-range
  {:end config/default-range})

(defn decrease-range
  "Range to query older items."
  ([start]
   (decrease-range start config/default-range))
  ([start max-range]
   ;; Fallback to 0 if start is negative.
   {:start (max 0 (- start max-range))
    :end start}))

(defn increase-range
  "Range to query more recent items."
  ([end total]
   (increase-range end total config/default-range))
  ([end total max-range]
   {:start end
    ;; Fallback to total if end is greater than total.
    :end (min total (+ end max-range))}))
