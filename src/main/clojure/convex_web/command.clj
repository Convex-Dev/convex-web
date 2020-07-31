(ns convex-web.command
  (:require [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.peer :as peer]
            [convex-web.convex :as convex]

            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.pprint :as pprint]

            [datascript.core :as d]
            [expound.alpha :as expound])
  (:import (convex.core.data Address Symbol ABlob AMap AVector ASet AList)
           (convex.core.lang Reader Symbols)
           (convex.core.lang.impl CoreFn)))

(defmulti object-string type)

(defmethod object-string :default
  [object]
  (let [object-clojure (or (convex/con->clj object) (str object))]
    (try
      (with-out-str (pprint/pprint object-clojure))
      (catch Exception ex
        (log/error ex "Failed to pretty print object - fallback to 'str'.")

        (str object-clojure)))))

(defmethod object-string Address
  [^Address object]
  (.toChecksumHex object))

(defn source [{:convex-web.command/keys [transaction query]}]
  (or (get query :convex-web.query/source)
      (get transaction :convex-web.transaction/source)))

(defn result-metadata [{:convex-web.command/keys [status object] :as command}]
  (let [source-form (when-let [source (source command)]
                      (first (Reader/readAll source)))]
    (case status
      :convex-web.command.status/running
      {}

      :convex-web.command.status/success
      (cond
        (= :nil object)
        {:doc {:type :nil}}

        (instance? Boolean object)
        {:doc {:type :boolean}}

        (instance? Number object)
        {:doc {:type :number}}

        (instance? String object)
        {:doc {:type :string}}

        (instance? AMap object)
        {:doc {:type :map}}

        (instance? AList object)
        {:doc {:type :list}}

        (instance? AVector object)
        {:doc {:type :vector}}

        (instance? ASet object)
        {:doc {:type :set}}

        (instance? Address object)
        {:doc {:type :address}}

        (instance? ABlob object)
        {:doc {:type :blob}
         :length (.length ^ABlob object)
         :hex-string (.toHexString ^ABlob object)}

        ;; Lookup metadata for a symbol (except the quote ' symbol).
        ;; Instead of checking the result object type, we read the source and check the first form.
        (and (instance? Symbol source-form) (not= Symbols/QUOTE source-form))
        (some-> source-form
                (convex/metadata)
                (convex/con->clj)
                (assoc-in [:doc :symbol] (.getName ^Symbol source-form)))

        ;; This must be after the special handling above because special forms (`def`, ...) returns a Symbol.
        (instance? Symbol object)
        {:doc {:type :symbol}})

      :convex-web.command.status/error
      {:doc {:type :message}})))

(defn with-metadata [command]
  (if-let [metadata (result-metadata command)]
    (assoc command ::metadata metadata)
    command))

(defn sanitize [{:convex-web.command/keys [status object error] :as command}]
  (merge (select-keys command [::id
                               ::mode
                               ::status
                               ::metadata])
         (case status
           :convex-web.command.status/running
           {}

           :convex-web.command.status/success
           {::object (object-string object)}

           :convex-web.command.status/error
           {::error (object-string error)})))

(defn query-all [db]
  (let [query '[:find [(pull ?e [*]) ...]
                :in $
                :where [?e :convex-web.command/id]]]
    (d/q query db)))

(defn query-by-id [db id]
  (let [query '[:find (pull ?e [*]) .
                :in $ ?id
                :where [?e :convex-web.command/id ?id]]]
    (d/q query db id)))

(defn execute-query [context {::keys [address query]}]
  (let [{:convex-web.query/keys [source]} query
        conn (system/convex-conn context)]
    (peer/query conn address source)))

(defn execute-transaction [context {::keys [address transaction]}]
  (let [{:convex-web.transaction/keys [source amount type]} transaction

        conn (system/convex-conn context)
        peer (peer/peer (system/convex-server context))
        datascript-conn (system/datascript-conn context)
        sequence-number (peer/sequence-number peer (Address/fromHex address))

        {:convex-web.account/keys [key-pair]} (account/find-by-address @datascript-conn address)

        transaction (case type
                      :convex-web.transaction.type/invoke
                      (peer/invoke-transaction (inc sequence-number) source)

                      :convex-web.transaction.type/transfer
                      (let [to (Address/fromHex (:convex-web.transaction/target transaction))]
                        (peer/transfer-transaction (inc sequence-number) to amount)))]
    (->> (convex/sign key-pair transaction)
         (convex/transact conn))))

(defn execute [context {::keys [mode] :as command}]
  (if-not (s/valid? :convex-web/command command)
    (throw (ex-info "Invalid Command." {:message (expound/expound-str :convex-web/command command)}))
    (let [conn (system/datascript-conn context)]
      (locking conn
        (let [id (cond
                   (= :convex-web.command.mode/query mode)
                   (execute-query context command)

                   (= :convex-web.command.mode/transaction mode)
                   (execute-transaction context command))

              running-command (merge (select-keys command [:convex-web.command/mode
                                                           :convex-web.command/address
                                                           :convex-web.command/query
                                                           :convex-web.command/transaction])
                                     #:convex-web.command {:id id
                                                           :status :convex-web.command.status/running})]

          (when-not (s/valid? :convex-web/command running-command)
            (throw (ex-info "Invalid Command." {:message (expound/expound-str :convex-web/command running-command)})))

          (d/transact! conn [running-command])

          (select-keys running-command [:convex-web.command/id
                                        :convex-web.command/status]))))))