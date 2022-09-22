(ns convex-web.session

  "User session management."

  (:require [clojure.pprint                :as pprint]
            [clojure.tools.logging         :as log]
            [convex-web.convex             :as $.web.convex]
            [datalevin.core                :as d]
            [ring.middleware.session.store :as ring.middleware.session.store]))


;;;;;;;;;;


(defn all
  
  [db]

  (d/q '[:find  [(pull ?e [*]) ...]
         :in    $
         :where [?e :convex-web.session/id _]]
       db))



(defn all-ring

  [db]

  (d/q '[:find  [(pull ?e [*]) ...]
         :in    $
         :where [?e :ring.session/key _]]
       db))



(defn find-account

  [db {:keys [address
              sid]}]

  (let [wallet (d/q '[:find  ?wallet .
                      :in    $ ?sid
                      :where
                      [?session :convex-web.session/id ?sid]
                      [?session :convex-web.session/wallet ?wallet]]
                 db
                 sid)]
    (reduce (fn [_acc account]
              (when (= ($.web.convex/address-safe address)
                       ($.web.convex/address-safe (account :convex-web.account/address)))
                (reduced account)))
            nil
            wallet)))



(defn find-ring-session

  [db key]

  (d/q '[:find  (pull ?e [*]) .
         :in    $ ?key
         :where [?e :ring.session/key ?key]]
       db
       key))



(defn find-session [db id]
  (when-let [session (d/q '[:find  (pull ?e [*]) .
                            :in    $ ?id
                            :where [?e :convex-web.session/id ?id]]
                          db
                          id)]
    (assoc session
           ;; TODO. Change the app to use `:convex-web.session/wallet` as well.
           :convex-web.session/accounts
           (session :convex-web.session/wallet))))


;;;;;;;;;;


(deftype PersistentSessionStore
         [conn]

  ring.middleware.session.store/SessionStore

    (delete-session [_ key]
      (log/debug "Delete Ring session"
                 key)
      (d/transact! conn
                   [[:db.fn/retractEntity key]])
      nil)

    (read-session [_ key]
      (find-ring-session @conn
                         key))

    (write-session [_ key data]
      (let [;; Key is nil when it's a new session
            key     (or key
                        (str (random-uuid)))
            session (merge {:ring.session/key key}
                           data)]
        (log/debug (str "Write Ring session:"
                        \newline
                        (with-out-str
                          (pprint/pprint session))))
        (d/transact! conn
                     [session])
        key)))



(defn persistent-session-store
  [conn]
  (PersistentSessionStore. conn))
