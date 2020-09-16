(ns convex-web.explorer
  (:require [convex-web.config :as config]))

(defn next-range
  "Range to query next (older) items."
  ([start]
   (next-range start config/default-range))
  ([start max-range]
   ;; Fallback to 0 if start is negative.
   {:start (max 0 (- start max-range))
    :end start}))

(defn previous-range
  "Range to query previous (more recent) items."
  ([end total]
   (previous-range end total config/default-range))
  ([end total max-range]
   {:start end
    ;; Fallback to total if end is greater than total.
    :end (min total (+ end max-range))}))
