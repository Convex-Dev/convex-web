(ns convex-web.account
  (:require
   [clojure.spec.alpha :as s]

   [datalevin.core :as d]

   [convex-web.convex :as convex]))

(defn find-all [db]
  (d/q '[:find [(pull ?e [* {:convex-web.account/faucets [*]}]) ...]
         :in $
         :where [?e :convex-web.account/address]]
       db))

(defn find-by-address [db addressable]
  (d/q '[:find (pull ?e [* {:convex-web.account/faucets [*]}]) .
         :in $ ?address
         :where [?e :convex-web.account/address ?address]]
       db (.longValue (convex/address addressable))))

(defn equivalent?
  "Returns true if account-a is equivalent to account-b.

  Check account's address and key pair for equivalence."
  [account-a account-b]
  (let [account-keyseq [:convex-web.account/address :convex-web.account/key-pair]]
    (= (select-keys account-a account-keyseq)
      (select-keys account-b account-keyseq))))

(s/fdef equivalent?
  :args (s/cat :a :convex-web/account :b :convex-web/account)
  :ret boolean?)

(def ^{:arglists '([account-a account-b])}
  nonequivalent?
  (complement equivalent?))

(s/fdef nonequivalent?
  :args (s/cat :a :convex-web/account :b :convex-web/account)
  :ret boolean?)