(ns convex.web.pagination
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
  (max 1 (quot (+ num-of-items (dec config/default-range)) config/default-range)))

(defn page-num-reverse
  "Returns the page number for `offset`."
  [offset num-of-items]
  (inc (quot (- (dec num-of-items) offset) convex-web.config/default-range)))

(defn page-num
  "Returns the page number for `offset`.

   `n` is the number of items per page.

   Example:

   (page-num 50 10)
   ;; => 5"
  [offset n]
  (max 1 #?(:clj  (int (Math/ceil (/ offset n)))
            :cljs (js/Math.ceil (/ offset n)))))
