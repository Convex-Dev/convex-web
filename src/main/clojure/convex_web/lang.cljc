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
                           (defn get ^{:callable? true} [] stored-data)
                           (defn set ^{:callable? true} [x] (def stored-data x)))))"}

   :subcurrency-actor
   {:source "(deploy '(do (def owner *caller*)
                   (defn contract-transfer ^{:callable? true}
                     [receiver amount]
                     (assert (= owner *caller*))
                     (transfer receiver amount))
                   (defn contract-balance ^{:callable? true} [] *balance*)))"}})
