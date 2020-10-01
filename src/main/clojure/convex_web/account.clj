(ns convex-web.account
  (:require [datalevin.core :as d]))

(defn find-by-address [db address]
  (d/q '[:find (pull ?e [* {:convex-web.account/faucets [*]}]) .
         :in $ ?address
         :where [?e :convex-web.account/address ?address]]
       db address))
