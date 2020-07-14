(ns convex-web.explorer
  (:require [convex-web.config :as config]))

(defn previous-range
  "Range to query previous items."
  ([start]
   (previous-range start config/default-range))
  ([start max-range]
   ;; Fallback to 0 if start is negative.
   {:start (max 0 (- start max-range))
    :end start}))

(defn next-range
  "Range to query next items."
  ([end total]
   (next-range end total config/default-range))
  ([end total max-range]
   {:start end
    ;; Fallback to total if end is greater than total.
    :end (min total (+ end max-range))}))
