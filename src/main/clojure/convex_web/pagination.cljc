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

(defn page-count [total]
  (+ (quot total config/default-range) (min (rem total config/default-range) 1)))

(defn page-index [total]
  (let [index (reduce
                (fn [{:keys [n] :as acc} p]
                  (merge acc {;; Mapping of [start end] to page-num
                              [(max 0 (- n config/default-range)) n] (inc p)

                              :n (- n config/default-range)}))
                {:n total}
                (range (page-count total)))
        ;; Remove no longer needed key
        index (dissoc index :n)
        ;; It must me sorted for consistency
        index (into (sorted-map) index)]
    index))

(defn page-num [start total]
  (let [index (page-index total)]
    (some
      (fn [[[start' _] page-num]]
        (when (>= start start')
          page-num))
      (reverse index))))