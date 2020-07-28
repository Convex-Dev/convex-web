(ns convex-web.session
  (:require [datascript.core :as d]
            [clojure.tools.logging :as log]))

(defn all [db]
  (d/q '[:find [(pull ?e [* {:convex-web.session/accounts
                             [:convex-web.account/address]}]) ...]
         :in $
         :where [?e :convex-web.session/id _]]
       db))

(defn find-session [db id]
  (d/q '[:find (pull ?e [:convex-web.session/id
                         {:convex-web.session/accounts
                          [:convex-web.account/address]}]) .
         :in $ ?id
         :where [?e :convex-web.session/id ?id]]
       db id))

(defn find-account [db address]
  (d/q '[:find (pull ?accounts [:convex-web.account/address
                                :convex-web.account/key-pair]) .
         :in $ ?address
         :where
         [_ :convex-web.session/accounts ?accounts]
         [?accounts :convex-web.account/address ?address]]
       db
       address))
