(ns convex-web.session
  (:require [datalevin.core :as d]
            [ring.middleware.session.store :refer [SessionStore]]
            [clojure.tools.logging :as log])
  (:import (java.util UUID)))

(defn all [db]
  (d/q '[:find [(pull ?e [* {:convex-web.session/accounts
                             [:convex-web.account/address
                              :convex-web.account/key-pair]}]) ...]
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
