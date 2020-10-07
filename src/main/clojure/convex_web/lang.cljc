(ns convex-web.lang)

(def convex-lisp-examples
  {:self-balance
   {:source "*balance*"}

   :self-address
   {:source "*address*"}

   :check-balance
   {:source "(balance \"7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f\")"}

   :transfer
   {:source "(transfer \"7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f\" 1000)"}

   :subcurrency-actor
   {:source "(deploy '(do (def owner *caller*)
                   (defn contract-transfer
                     [receiver amount]
                     (assert (= owner *caller*))
                     (transfer receiver amount))
                   (defn contract-balance [] *balance*)
                   (export contract-transfer contract-balance)))"}})
