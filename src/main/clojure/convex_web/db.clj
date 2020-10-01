(ns convex-web.db)

(def schema
  {;; -- Command

   :convex-web.command/id
   {:db/unique :db.unique/identity}


   ;; -- Account

   :convex-web.account/address
   {:db/unique :db.unique/identity}

   :convex-web.account/faucets
   {:db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}


   ;; -- Session

   :convex-web.session/id
   {:db/unique :db.unique/identity}

   :convex-web.session/accounts
   {:db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}})
