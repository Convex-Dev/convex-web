(ns convex-web.session
  (:require
   [clojure.tools.logging :as log]

   [datalevin.core :as d]
   [ring.middleware.session.store :refer [SessionStore]]


   [convex-web.convex :as convex])

  (:import (java.util UUID)))

(defn all [db]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $
         :where [?e :convex-web.session/id _]]
       db))

(defn find-session [db id]
  (when-let [session (d/q '[:find (pull ?e [*]) .
                            :in $ ?id
                            :where [?e :convex-web.session/id ?id]]
                       db id)]
    (let [{wallet :convex-web.session/wallet} session

          ;; TODO: Change the app to use :convex-web.session/wallet instead.
          session (assoc session :convex-web.session/accounts (vec wallet))]
      session)))

(defn find-account [db {:keys [sid address]}]
  (let [wallet (d/q '[:find ?wallet .
                      :in $ ?sid
                      :where
                      [?session :convex-web.session/id ?sid]
                      [?session :convex-web.session/wallet ?wallet]]
                 db sid)]
    (reduce
      (fn [_ {this-address :convex-web.account/address :as account}]
        (when (= (convex/address-safe address) (convex/address-safe this-address))
          (reduced account)))
      nil
      wallet)))

(defn all-ring [db]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $
         :where [?e :ring.session/key _]]
       db))

(defn find-ring-session [db key]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?key
         :where [?e :ring.session/key ?key]]
       db key))


(deftype PersistentSessionStore [conn]
  SessionStore
  (read-session [_ key]
    (find-ring-session @conn key))

  (write-session [_ key data]
    (let [;; Key is nil when it's a new Session
          key (or key (str (UUID/randomUUID)))
          session (merge {:ring.session/key key} data)]

      (log/debug "Transact Session" session)

      (d/transact! conn [session])

      key))

  (delete-session [_ key]
    (log/debug "Delete Session" key)

    (d/transact! conn [[:db.fn/retractEntity key]])

    nil))

(defn persistent-session-store [conn]
  (PersistentSessionStore. conn))
