(ns convex.web.pagination

  "Paginating results."

  (:require [convex-web.config :as $.web.config]))


;;;;;;;;;; Values


(def min-range
     {:end $.web.config/default-range})


;;;;;;;;;; Functions


(defn decrease-range

  "Range for querying older items."


  ([start]

   (decrease-range start
                   $.web.config/default-range))


  ([start max-range]

   (let [start-2 (max 0
                      (- start
                         max-range))]
     (if (= 0
            start-2)
       {:end max-range}
       {:end   start
        :start start-2}))))



(defn increase-range

  "Range for querying more recent items."


  ([end total]

   (increase-range end
                   total
                   $.web.config/default-range))


  ([end total max-range]

   (let [end-2 (min total
                    (+ end
                       max-range))]
     (if (>= (- end-2
                end)
             $.web.config/default-range)
       {:end   end-2
        :start end}
       {:end end-2}))))



(defn page-count

  "Returns the number of pages based on the number of items per page
   configuration."

  [num-of-items]

  (max 1
       (quot (+ num-of-items
                (dec $.web.config/default-range))
             $.web.config/default-range)))



(defn page-num-reverse

  "Returns the page number for `offset`."

  [offset num-of-items]

  (inc (quot (- (dec num-of-items)
                offset)
             $.web.config/default-range)))



(defn page-num

  "Returns the page number for `offset` given the `n`umber of items per page.

   ```clojure
   (page-num 50 10)
   ;; => 5
   ```"

  [offset n]

  (max 1
       #?(:clj  (int (Math/ceil (/ offset n)))
          :cljs (js/Math.ceil (/ offset n)))))
