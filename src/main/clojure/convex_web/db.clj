(ns convex-web.db)

(def schema
  {;; -- Command

   :convex-web.command/id
   {:db/unique :db.unique/identity
    :db/index true}


   ;; -- Account

   :convex-web.account/address
   {:db/unique :db.unique/identity
    :db/index true}

   :convex-web.account/faucets
   {:db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}


   ;; -- Session

   :convex-web.session/id
   {:db/unique :db.unique/identity
    :db/index true}

   :convex-web.session/accounts
   {:db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}})
