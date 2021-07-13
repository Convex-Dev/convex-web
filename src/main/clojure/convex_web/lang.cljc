(ns convex-web.lang)

(def convex-lisp-examples
  {:self-balance
   {:source "*balance*"}

   :self-address
   {:source "*address*"}

   :check-balance
   {:source "(balance #11)"}

   :transfer
   {:source "(transfer #11 1000)"}

   :simple-storage-actor
   {:source "(def storage-example-address
              (deploy '(do (def stored-data nil)
                           (defn get [] stored-data)
                           (defn set [x] (def stored-data x))
                           (export get set))))"}

   :subcurrency-actor
   {:source "(deploy '(do (def owner *caller*)
                   (defn contract-transfer
                     [receiver amount]
                     (assert (= owner *caller*))
                     (transfer receiver amount))
                   (defn contract-balance [] *balance*)
                   (export contract-transfer contract-balance)))"}})
