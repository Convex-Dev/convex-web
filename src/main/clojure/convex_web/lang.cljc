(ns convex-web.lang)

(def convex-lisp-examples
  {:self-balance
   {:source "*balance*"}

   :self-address
   {:source "*address*"}

   :check-balance
   {:source "(balance #9)"}

   :transfer
   {:source "(transfer #9 1000)"}

   :subcurrency-actor
   {:source "(deploy '(do (def owner *caller*)
                   (defn contract-transfer
                     [receiver amount]
                     (assert (= owner *caller*))
                     (transfer receiver amount))
                   (defn contract-balance [] *balance*)
                   (export contract-transfer contract-balance)))"}})
