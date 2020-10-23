(ns convex-web.command
  (:require [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.peer :as peer]
            [convex-web.convex :as convex]
            [convex-web.specs]

            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [clojure.stacktrace :as stacktrace]
            [clojure.pprint :as pprint]

            [datalevin.core :as d])
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
  (let [source-form (try
                      (when (= :convex-lisp (language command))
                        (when-let [source (source command)]
                          (first (Reader/readAll source))))
                      (catch Throwable _
                        nil))

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
                                       (assoc-in [:doc :symbol] (.toString (.getName ^Symbol source-form))))]
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

(defn execute-query [system command]
  (let [{::keys [address query]} command
        {:convex-web.query/keys [source language]} query]
    (convex/query (system/convex-client system) {:address address
                                                 :source source
                                                 :lang language})))

(s/fdef execute-query
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(s/nilable (instance? Result %)))

;; --

(defn execute-transaction [system command]
  (let [{::keys [address transaction]} command]
    (locking (convex/lockee address)
      (let [{:convex-web.transaction/keys [source language amount type]} transaction

            peer (system/convex-peer-server system)

            caller-address (convex/address address)

            next-sequence-number (inc (or (convex/get-sequence-number caller-address)
                                          (peer/sequence-number peer caller-address)
                                          0))

            {:convex-web.account/keys [key-pair]} (account/find-by-address (system/db system) caller-address)

            atransaction (case type
                           :convex-web.transaction.type/invoke
                           (peer/invoke-transaction next-sequence-number source language)

                           :convex-web.transaction.type/transfer
                           (let [to (convex/address (:convex-web.transaction/target transaction))]
                             (peer/transfer-transaction next-sequence-number to amount)))]
        (try
          (let [^Result r (->> (convex/sign (convex/create-key-pair key-pair) atransaction)
                               (convex/transact (system/convex-client system)))

                bad-sequence-number? (when-let [error-code (.getErrorCode r)]
                                       (= :SEQUENCE (convex/datafy error-code)))]

            (if bad-sequence-number?
              (log/error "Result error: Bad sequence number." {:attempted-sequence-number next-sequence-number})
              (convex/set-sequence-number! caller-address next-sequence-number))

            r)
          (catch Throwable t
            (convex/reset-sequence-number! caller-address)

            (log/error t "Transaction failed." (merge transaction {:attempted-sequence-number next-sequence-number}))

            (throw t)))))))

(s/fdef execute-transaction
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(s/nilable (instance? Result %)))

;; --

(defn execute [system {::keys [mode] :as command}]
  (try
    (let [{:keys [result error]} (try
                                   {:result (cond
                                              (= :convex-web.command.mode/query mode)
                                              (execute-query system command)

                                              (= :convex-web.command.mode/transaction mode)
                                              (execute-transaction system command))}
                                   (catch Throwable ex
                                     (log/error ex "Command execution failed.")

                                     {:error ex}))

          _ (if-let [result-value (some-> ^Result result (.getValue))]
              (log/debug "Result value:" (type result-value) result-value)
              (log/warn "Result is nil for command:" command))

          result-id (some-> result .getID)
          result-value (some-> result .getValue)
          result-error-code (some-> result .getErrorCode)

          _ (when result-error-code
              (log/error "Command returned an error:" result-error-code result-value))

          ;; Command status.
          command' (if result
                     (merge #:convex-web.command {:id result-id
                                                  :object result-value
                                                  :status
                                                  (if result-error-code
                                                    :convex-web.command.status/error
                                                    :convex-web.command.status/success)}
                            (when result-error-code
                              #:convex-web.command {:error
                                                    {:code (convex/datafy result-error-code)
                                                     :message (convex/datafy result-value)}}))

                     ;; If there isn't a Result, `error` won't have a code,
                     ;; and the Exception's message will be used as its message.
                     #:convex-web.command {:status :convex-web.command.status/error
                                           :error
                                           {:message (ex-message (or (some-> error stacktrace/root-cause) error))}})

          ;; Updated Command.
          command' (merge (select-keys command [:convex-web.command/mode
                                                :convex-web.command/language
                                                :convex-web.command/address
                                                :convex-web.command/query
                                                :convex-web.command/transaction])

                          command')

          ;; Wrapped Command.
          command' (-> command'
                       (wrap-result-metadata)
                       (wrap-result))]

      command')))

(s/fdef execute
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret :convex-web/command)