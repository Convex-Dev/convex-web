(ns convex-web.command
  (:require [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.peer :as peer]
            [convex-web.convex :as convex]

            [clojure.spec.alpha :as s]

            [datascript.core :as d]
            [expound.alpha :as expound])
  (:import (convex.core.data Address Symbol ABlob AMap AVector ASet AList)
           (convex.core.lang Reader Symbols)))

(defn source [{:convex-web.command/keys [transaction query]}]
  (or (get query :convex-web.query/source)
      (get transaction :convex-web.transaction/source)))

(defn wrap-result [{:convex-web.command/keys [status object] :as command}]
  (case status
    :convex-web.command.status/success
    (assoc command ::object (cond
                              (instance? Address object)
                              {:hex-string (.toHexString object)
                               :checksum-hex (.toChecksumHex object)}

                              (instance? ABlob object)
                              {:length (.length ^ABlob object)
                               :hex-string (.toHexString ^ABlob object)}

                              :else
                              (convex/datafy object)))

    ;; Don't need to handle error status because error is already datafy'ed.

    command))

(defn wrap-result-metadata [{:convex-web.command/keys [status object] :as command}]
  (let [source-form (when-let [source (source command)]
                      (first (Reader/readAll source)))

        metadata (case status
                   :convex-web.command.status/running
                   {}

                   :convex-web.command.status/success
                   (cond
                     (= :nil object)
                     {:type :nil}

                     (instance? Boolean object)
                     {:type :boolean}

                     (instance? Number object)
                     {:type :number}

                     (instance? String object)
                     {:type :string}

                     (instance? AMap object)
                     {:type :map}

                     (instance? AList object)
                     {:type :list}

                     (instance? AVector object)
                     {:type :vector}

                     (instance? ASet object)
                     {:type :set}

                     (instance? Address object)
                     {:type :address}

                     (instance? ABlob object)
                     {:type :blob}

                     ;; Lookup metadata for a symbol (except the quote ' symbol).
                     ;; Instead of checking the result object type, we read the source and check the first form.
                     (and (instance? Symbol source-form) (not= Symbols/QUOTE source-form))
                     (let [doc (some-> source-form
                                       (convex/metadata)
                                       (convex/datafy)
                                       (assoc-in [:doc :symbol] (.getName ^Symbol source-form)))]
                       (merge doc {:type (get-in doc [:doc :type])}))

                     ;; This must be after the special handling above because special forms (`def`, ...) returns a Symbol.
                     (instance? Symbol object)
                     {:type :symbol})

                   :convex-web.command.status/error
                   {:type :error})]
    (assoc command ::metadata metadata)))

(defn prune [command]
  (select-keys command [::id ::mode ::status ::metadata ::object ::error]))

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