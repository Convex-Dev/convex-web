(ns convex-web.command
  (:require [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.peer :as peer]
            [convex-web.convex :as convex]
            [convex-web.specs]

            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [clojure.datafy :refer [datafy]]

            [datalevin.core :as d]
            [expound.alpha :as expound])
  (:import (convex.core.data Address Symbol ABlob AMap AVector ASet AList AString)
           (convex.core.lang Reader Symbols)
           (convex.core Result)))

(defn source [{:convex-web.command/keys [transaction query]}]
  (or (get query :convex-web.query/source)
      (get transaction :convex-web.transaction/source)))

(s/fdef source
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/non-empty-string)

(defn language [{:convex-web.command/keys [transaction query]}]
  (or (get query :convex-web.query/language)
      (get transaction :convex-web.transaction/language)))

(s/fdef language
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/language)

;; --

(defn wrap-result [{:convex-web.command/keys [object] :as command}]
  (assoc command ::object (cond
                            (instance? Address object)
                            {:hex-string (.toHexString object)
                             :checksum-hex (.toChecksumHex object)}

                            (instance? ABlob object)
                            {:length (.length ^ABlob object)
                             :hex-string (.toHexString ^ABlob object)}

                            :else
                            (convex/datafy object))))

(s/fdef wrap-result
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/command)

;; --

(defn wrap-result-metadata [{:convex-web.command/keys [status object] :as command}]
  (let [source-form (when (= :convex-lisp (language command))
                      (when-let [source (source command)]
                        (first (Reader/readAll source))))

        metadata (case status
                   :convex-web.command.status/running
                   {}

                   :convex-web.command.status/success
                   (cond
                     (instance? Boolean object)
                     {:type :boolean}

                     (instance? Number object)
                     {:type :number}

                     (instance? AString object)
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
                     {:type :symbol}

                     :else
                     {})

                   :convex-web.command.status/error
                   {:type :error})]
    (assoc command ::metadata metadata)))

(s/fdef wrap-result-metadata
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/command)

;; --

(defn prune [command]
  (select-keys command [::id ::mode ::status ::metadata ::object ::error]))

;; --

(defn find-all [db]
  (let [query '[:find [(pull ?e [*]) ...]
                :in $
                :where [?e :convex-web.command/id]]]
    (d/q query db)))

(defn find-by-id [db id]
  (let [query '[:find (pull ?e [*]) .
                :in $ ?id
                :where [?e :convex-web.command/id ?id]]]
    (d/q query db id)))

;; --

(defn execute-query [system {::keys [address query]}]
  (let [{:convex-web.query/keys [source language]} query]
    (convex/query (system/convex-client system) {:address address
                                                 :source source
                                                 :lang language})))

(s/fdef execute-query
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(instance? Result %))

;; --

(defn execute-transaction [system {::keys [address transaction]}]
  (let [{:convex-web.transaction/keys [source language amount type]} transaction

        peer (system/convex-peer-server system)

        address (convex/address address)

        next-sequence-number (convex/next-sequence-number! {:address address
                                                            :not-found (or (peer/sequence-number peer address) 1)})

        {:convex-web.account/keys [key-pair]} (account/find-by-address (system/db system) address)

        transaction (case type
                      :convex-web.transaction.type/invoke
                      (peer/invoke-transaction next-sequence-number source language)

                      :convex-web.transaction.type/transfer
                      (let [to (convex/address (:convex-web.transaction/target transaction))]
                        (peer/transfer-transaction next-sequence-number to amount)))]
    (->> (convex/sign (convex/create-key-pair key-pair) transaction)
         (convex/transact (system/convex-client system)))))

(s/fdef execute-transaction
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(instance? Result %))

;; --

(defn execute [system {::keys [mode] :as command}]
  (if-not (s/valid? :convex-web/command command)
    (throw (ex-info "Invalid Command." {:message (expound/expound-str :convex-web/command command)}))
    (let [^Result result (cond
                           (= :convex-web.command.mode/query mode)
                           (execute-query system command)

                           (= :convex-web.command.mode/transaction mode)
                           (execute-transaction system command))

          command' (merge (select-keys command [:convex-web.command/mode
                                                :convex-web.command/language
                                                :convex-web.command/address
                                                :convex-web.command/query
                                                :convex-web.command/transaction])

                          #:convex-web.command {:id (.getID result)
                                                :object (.getValue result)
                                                :status
                                                (if (.getErrorCode result)
                                                  :convex-web.command.status/error
                                                  :convex-web.command.status/success)}

                          (when-let [error-code (.getErrorCode result)]
                            #:convex-web.command {:error
                                                  {:code (datafy error-code)
                                                   :message (datafy (.getValue result))}}))

          command' (-> command'
                       (wrap-result-metadata)
                       (wrap-result))]

      command')))

(s/fdef execute
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret :convex-web/command)