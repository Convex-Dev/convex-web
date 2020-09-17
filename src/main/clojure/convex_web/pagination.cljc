(ns convex-web.pagination
  (:require [convex-web.config :as config]))

(def min-range
  {:end config/default-range})

(defn decrease-range
  "Range to query older items."
  ([start]
   (decrease-range start config/default-range))
  ([start max-range]
   (let [start' (max 0 (- start max-range))]
     (if (= 0 start')
       {:end max-range}
       {:start start'
        :end start}))))

(defn increase-range
  "Range to query more recent items."
  ([end total]
   (increase-range end total config/default-range))
  ([end total max-range]
   (let [end' (min total (+ end max-range))]
     (if (>= (- end' end) config/default-range)
       {:start end
        :end end'}
       {:end end'}))))

(defn page-count
  "Returns the number of pages based on the number of items per page
   configuration."
  [num-of-items]
  (+ (quot num-of-items config/default-range) (min (rem num-of-items config/default-range) 1)))

(defn page-index
  "Returns a mapping of range to page number based on the number of items per
   page configuration."
  [num-of-items]
  (let [index (reduce
                (fn [{:keys [n] :as acc} p]
                  (merge acc {;; Mapping of [start end] to page-num
                              [(max 0 (- n config/default-range)) n] (inc p)

                              :n (- n config/default-range)}))
                {:n num-of-items}
                (range (page-count num-of-items)))
        ;; Remove no longer needed key
        index (dissoc index :n)
        ;; It must me sorted for consistency
        index (into (sorted-map) index)]
    index))

(defn page-num
  "Returns the page number for `offset`."
  [offset num-of-items]
  (let [index (page-index num-of-items)]
    (some
      (fn [[[start' _] page-num]]
        (when (>= offset start')
          page-num))
      (reverse index))))