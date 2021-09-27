(ns convex-web.command
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.logging :as log]
   [clojure.stacktrace :as stacktrace]
   [clojure.pprint :as pprint]

   [datalevin.core :as d]

   [convex-web.system :as system]
   [convex-web.convex :as convex]
   [convex-web.specs])

  (:import
   (convex.core.data Address Symbol ABlob AMap AVector ASet AList AString)
   (convex.core.lang Reader Symbols)
   (convex.core Result)
   (convex.core.data.prim CVMBool CVMLong CVMDouble)))

(defn source [command]
  (let [{:convex-web.command/keys [transaction query]} command]
    (or (get query :convex-web.query/source)
        (get transaction :convex-web.transaction/source))))

(s/fdef source
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/non-empty-string)

(defn language [command]
  (let [{:convex-web.command/keys [transaction query]} command]
    (or (get query :convex-web.query/language)
        (get transaction :convex-web.transaction/language))))

(s/fdef language
  :args (s/cat :command :convex-web/command)
  :ret :convex-web/language)

;; --

(defn sandbox-result
  "Sandbox renderers are 'dispatched' by the metadata and the object value.

   Object maps can be defined per type."
  [result-value]
  (cond
    (instance? Address result-value)
    {:address (.longValue ^Address result-value)}

    (instance? ABlob result-value)
    {:length (.count ^ABlob result-value)
     :hex-string (.toHexString ^ABlob result-value)}

    :else
    (try
      (convex/datafy result-value)
      (catch Exception ex
        (log/warn ex "Result wrapping failed to datafy result-value. It will fallback to `(str result-value)`.")

        (str result-value)))))

;; TODO Merge with `value-kind`.
(defn result-metadata [result-value & [{:keys [source lang]}]]
  (let [source-form (try
                      (when (and source (= :convex-lisp lang))
                        (first (Reader/readAll source)))
                      (catch Throwable _
                        nil))]
    (cond
      (instance? CVMBool result-value)
      {:type :boolean}

      (instance? CVMLong result-value)
      {:type :long}

      (instance? CVMDouble result-value)
      {:type :double}

      (instance? AString result-value)
      {:type :string}

      (instance? AMap result-value)
      {:type :map}

      (instance? AList result-value)
      {:type :list}

      (instance? AVector result-value)
      {:type :vector}

      (instance? ASet result-value)
      {:type :set}

      (instance? Address result-value)
      {:type :address}

      (instance? ABlob result-value)
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
      (instance? Symbol result-value)
      {:type :symbol}

      :else
      {})))

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
  (let [{::keys [signer query]} command

        {signer-address :convex-web.account/address} signer

        {:convex-web.query/keys [source language]} query]

    (convex/query (system/convex-client system) {:address signer-address
                                                 :source source
                                                 :lang language})))

(s/fdef execute-query
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(s/nilable (instance? Result %)))

;; --

(defn execute-transaction [system command]
  (let [{::keys [signer transaction]} command

        {signer-address :convex-web.account/address} signer

        signer-address (convex/address signer-address)]

    (locking (convex/lockee signer-address)

      (let [{:convex-web.transaction/keys [source amount type target]} transaction

            peer (system/convex-peer system)

            next-sequence-number (inc (or (convex/get-sequence-number signer-address)
                                        (convex/sequence-number peer signer-address)
                                        0))

            {signer-key-pair :convex-web.account/key-pair} signer

            atransaction (case type
                           :convex-web.transaction.type/invoke
                           (convex/invoke-transaction {:nonce next-sequence-number
                                                       :address signer-address
                                                       :command (convex/read-source source)} )

                           :convex-web.transaction.type/transfer
                           (convex/transfer-transaction {:address signer-address
                                                         :nonce next-sequence-number
                                                         :target target
                                                         :amount amount}))]

        (when-not (:convex-web.key-pair/private-key signer-key-pair)
          (throw (ex-info (str "Wallet doesn't have a private key set up for account " signer-address ".")
                   (merge {} signer-key-pair))))

        (try
          (let [^Result r (->> (convex/sign (convex/create-key-pair signer-key-pair) atransaction)
                            (convex/transact-signed (system/convex-client system)))

                bad-sequence-number? (when-let [error-code (.getErrorCode r)]
                                       (= :SEQUENCE (convex/datafy error-code)))]

            (if bad-sequence-number?
              (log/error "Result error: Bad sequence number." {:attempted-sequence-number next-sequence-number})
              (convex/set-sequence-number! signer-address next-sequence-number))

            r)
          (catch Throwable t
            (convex/reset-sequence-number! signer-address)

            (log/error t "Transaction failed." (merge transaction {:attempted-sequence-number next-sequence-number}))

            (throw t)))))))

(s/fdef execute-transaction
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret #(s/nilable (instance? Result %)))

;; --

(defn execute [system {::keys [mode] :as command}]
  (let [_ (log/debug (str "Execute\n" (with-out-str (pprint/pprint command))))

        {:keys [result error]}
        (try
          {:result (cond
                     (= :convex-web.command.mode/query mode)
                     (execute-query system command)
                     
                     (= :convex-web.command.mode/transaction mode)
                     (execute-transaction system command))}
          (catch Throwable ex
            (log/error ex "Command execution failed.")
            
            {:error ex}))
        
        _ (if-let [result-value (some-> ^Result result (.getValue))]
            (log/debug (str "Source: " (source command) "\nType: " (type result-value) " \nValue: " result-value))
            (log/warn "Missing result. Command:" command))
        
        result-value (some-> result .getValue)
        result-error-code (some-> result .getErrorCode)
        result-trace (some-> result .getTrace)
        
        _ (when result-error-code
            (log/error "Command returned an error:" result-error-code result-value))
        
        ;; Command status.
        command' (if result
                   (merge 
                     #:convex-web.command 
                     {:result (convex/result-data result)
                      :status
                      (if result-error-code
                        :convex-web.command.status/error
                        :convex-web.command.status/success)}
                     
                     (when result-error-code
                       #:convex-web.command 
                       {:error
                        {:code (convex/datafy result-error-code)
                         :message (convex/datafy result-value)
                         :trace (convex/datafy result-trace)}}))
                   
                   ;; If there isn't a Result, `error` won't have a code,
                   ;; and the Exception's message will be used as its message.
                   #:convex-web.command 
                   {:status :convex-web.command.status/error
                    :error {:code (or (:convex-web.result/error-code (ex-data error)) :Server)
                            :message (ex-message (or (some-> error stacktrace/root-cause) error))}})
        
        ;; Updated Command.
        command' (merge (select-keys command [:convex-web.command/id
                                              :convex-web.command/timestamp
                                              :convex-web.command/mode
                                              :convex-web.command/language
                                              :convex-web.command/address
                                              :convex-web.command/query
                                              :convex-web.command/transaction])
                   
                   command')]
    
    command'))

(s/fdef execute
  :args (s/cat :system :convex-web/system :command :convex-web/incoming-command)
  :ret :convex-web/command)