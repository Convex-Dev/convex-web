(ns convex-web.convex
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]

            [cognitect.anomalies :as anomalies])
  (:import (convex.core.data Keyword Symbol Syntax Address AccountStatus SignedData AVector AList ASet AMap ABlob Blob AString AccountKey)
           (convex.core.lang Core Reader ScryptNext RT Context)
           (convex.core Order Block Peer State Init Result)
           (convex.core.crypto AKeyPair)
           (convex.core.transactions Transfer ATransaction Invoke Call)
           (convex.api Convex)
           (java.util.concurrent TimeoutException)
           (clojure.lang AFn)
           (convex.core.lang.expanders AExpander)))

(defn read-source [source lang]
  (try
    (case lang
      :convex-lisp
      (let [^AList l (Reader/readAll source)
            form1 (first l)
            form2 (second l)]
        (if form2
          (.cons l (Symbol/create "do"))
          form1))

      :convex-scrypt
      (ScryptNext/readSyntax source))
    (catch Throwable ex
      (throw (ex-info "Syntax error." {::anomalies/message (ex-message ex)
                                       ::anomalies/category ::anomalies/incorrect})))))

(defmacro execute [context form]
  `(let [^String source# ~(pr-str form)
         context# (.execute ~context (.getResult (.expandCompile ~context (Reader/read source#))))]
     (if (.isExceptional context#)
       (.getExceptional context#)
       (.getResult context#))))

(defn execute-string [^Context context ^String source]
  (let [new-context (.execute context (.getResult (.expandCompile context (Reader/read source))))]
    (if (.isExceptional new-context)
      (.getExceptional new-context)
      (.getResult new-context))))

(defn execute-scrypt [context source]
  (let [context (.execute context (.getResult (.expandCompile context (ScryptNext/readSyntax source))))]
    (if (.isExceptional context)
      (.getExceptional context)
      (.getResult context))))

(defn throwable-category [throwable]
  (cond
    (instance? TimeoutException throwable)
    ::anomalies/unavailable

    (instance? InterruptedException throwable)
    ::anomalies/interrupted))

(defn core-metadata
  "Core metadata indexed by Symbol."
  []
  (reduce
    (fn [m [^Symbol sym ^Syntax syn]]
      (assoc m sym (.getMeta syn)))
    {}
    Core/CORE_NAMESPACE))

(defn value-kind [x]
  (cond
    (instance? Boolean x)
    :boolean

    (instance? Number x)
    :number

    (instance? AString x)
    :string

    (instance? Keyword x)
    :keyword

    (instance? AMap x)
    :map

    (instance? AList x)
    :list

    (instance? AVector x)
    :vector

    (instance? ASet x)
    :set

    (instance? Address x)
    :address

    (instance? ABlob x)
    :blob

    (instance? AFn x)
    :function

    (instance? AExpander x)
    :macro

    (instance? Symbol x)
    :symbol))

(defn datafy
  "Datafy a Convex object `x` to Clojure.

   Throws if there isn't a mapping of a Convex object to Clojure."
  [x]
  (cond
    (nil? x)
    nil

    (instance? Boolean x)
    x

    (instance? Character x)
    x

    (instance? Long x)
    x

    (instance? Double x)
    x

    (instance? AString x)
    (.toString x)

    (instance? Keyword x)
    (keyword (.toString (.getName x)))

    (instance? Symbol x)
    (symbol (some-> x (.getNamespace) (.getName) (.toString)) (.toString (.getName x)))

    (instance? AList x)
    (map datafy x)

    (instance? AVector x)
    (mapv datafy x)

    (instance? AMap x)
    (reduce
      (fn [m [k v]]
        (assoc m (datafy k) (datafy v)))
      {}
      x)

    (instance? ASet x)
    (into #{} (map datafy x))

    (instance? Address x)
    (.toString ^Address x)

    (instance? AFn x)
    (.toString x)

    (instance? AExpander x)
    (.toString x)

    (instance? Blob x)
    (.toHexString ^Blob x)

    (instance? Syntax x)
    (datafy (.getValue ^Syntax x))

    :else
    (throw (ex-info (str "Can't datafy " (some-> x (.getClass) (.getName)) ".") {:object x}))))

(defn datafy-safe [x]
  (try
    (datafy x)
    (catch Exception ex
      (log/error ex)
      nil)))

(defn ^Address address [x]
  (cond
    (nil? x)
    (throw (ex-info (str "Can't coerce nil to " (.getName Address) ".") {}))

    (instance? Address x)
    x

    (nat-int? x)
    (Address/create ^long x)

    (instance? ABlob x)
    (Address/create ^ABlob x)

    (and (string? x) (str/blank? x))
    (throw (ex-info (str "Can't coerce empty string to " (.getName Address) ".") {}))

    (string? x)
    (let [s (if (str/starts-with? x "#")
              (subs x 1)
              x)]
      (try
        (Address/create (Long/parseLong s))
        (catch Exception _
          (throw (ex-info (str "Can't coerce " (pr-str x) " to " (.getName Address) ".") {})))))

    :else
    (throw (ex-info (str "Can't coerce " (pr-str x) " to " (.getName Address) ".") {}))))

(defn metadata [sym]
  (let [sym (cond
              (instance? Symbol sym)
              sym

              (string? sym)
              (Symbol/create ^String sym)

              :else
              (throw (ex-info "'sym' must be either a convex.core.data.Symbol or String." {:sym sym})))]
    (get (core-metadata) sym)))

(defn ^Order peer-order [^Peer peer]
  (.getPeerOrder peer))

(defn ^State consensus-state [^Peer peer]
  (.getConsensusState peer))

(defn consensus-point [^Order order]
  (.getConsensusPoint order))

(defn result-data [^Result result]
  (let [result-id (.getID result)
        result-error-code (.getErrorCode result)
        result-value (.getValue result)
        result-trace (.getTrace result)]
    (merge #:convex-web.result {:id result-id
                                :value (try
                                         (datafy result-value)
                                         (catch Exception _
                                           (str result-value)))}

           (when-let [kind (value-kind result-value)]
             {:convex-web.result/value-kind kind})

           (when result-error-code
             {:convex-web.result/error-code (datafy result-error-code)
              :convex-web.result/trace (datafy result-trace)}))))

(defn transaction-result-data [^ATransaction atransaction ^Result result]
  (let [tx (cond
             (instance? Transfer atransaction)
             #:convex-web.transaction {:type :convex-web.transaction.type/transfer
                                       :target (.toString (.getTarget ^Transfer atransaction))
                                       :amount (.getAmount ^Transfer atransaction)
                                       :sequence (.getSequence ^ATransaction atransaction)}

             (instance? Invoke atransaction)
             #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                       :source (str (.getCommand ^Invoke atransaction))
                                       :sequence (.getSequence ^ATransaction atransaction)}

             (instance? Call atransaction)
             #:convex-web.transaction {:type :convex-web.transaction.type/call})]

    (merge tx {:convex-web.transaction/result (result-data result)})))

(defn block-data [^Peer peer ^Long index ^Block block]
  #:convex-web.block {:index index
                      :timestamp (.getTimeStamp block)
                      :peer (.toChecksumHex (.getPeer block))
                      :transactions
                      (map-indexed
                        (fn [^Long tx-index ^SignedData signed-data]
                          #:convex-web.signed-data {:address (.toChecksumHex (.getAddress signed-data))
                                                    :value (transaction-result-data (.getValue signed-data) (.getResult peer index tx-index))})
                        (.getTransactions block))})

(defn blocks-data [^Peer peer & [{:keys [start end]}]]
  (let [order (peer-order peer)
        start (or start 0)
        end (or end (consensus-point order))]
    (reduce
      (fn [blocks index]
        (conj blocks (block-data peer index (.getBlock order index))))
      []
      (range start end))))

(defn blocks-indexed [^Peer peer]
  (let [order (peer-order peer)]
    (reduce
      (fn [blocks index]
        (assoc blocks index (block-data peer index (.getBlock order index))))
      {}
      (range (consensus-point order)))))

(defn accounts [^Peer peer & [{:keys [start end]}]]
  (let [^State state (.getConsensusState peer)
        start (or start 0)
        end (or end (count (.getAccounts state)))]

    (log/debug (str "Get Accounts [" start " " end "]"))

    (.subVector (.getAccounts state) start (- end start))))

(defn ^AccountStatus account-status [^Peer peer ^long address]
  (.get (accounts peer) address))

(defn syntax-data [^Syntax syn]
  (merge #:convex-web.syntax {:source (.getSource syn)
                              :value
                              (try
                                (datafy (.getValue syn))
                                (catch Exception _
                                  (str (.getValue syn))))}

         (when-let [meta (datafy-safe (.getMeta syn))]
           {:convex-web.syntax/meta meta})

         (when-let [kind (value-kind (.getValue syn))]
           {:convex-web.syntax/value-kind kind})))

(defn environment-data
  "Account Status' environment data.

   Where keys are symbols and values are syntax data."
  [environment]
  (reduce
    (fn [env [^Symbol sym ^Syntax syn]]
      (assoc env (datafy sym) (syntax-data syn)))
    {}
    environment))

(defn account-status-data [^AccountStatus account-status]
  (when account-status
    (let [env (environment-data (.getEnvironment account-status))
          exports? (contains? env '*exports*)
          actor? (.isActor account-status)
          library? (and actor? (not exports?))]
      (merge #:convex-web.account-status {:sequence (.getSequence account-status)
                                          :balance (.getBalance account-status)
                                          :environment env
                                          :actor? actor?
                                          :library? library?
                                          :memory-size (.getMemorySize account-status)
                                          :allowance (.getAllowance account-status)
                                          :type (cond
                                                  library? :library
                                                  actor? :actor
                                                  :else :user)}))))

(defn accounts-indexed
  "Returns a mapping of Address to Account Status."
  [^Peer peer & [{:keys [start end]}]]
  (map-indexed
    (fn [address status]
      (let [status (account-status-data status)]
        #:convex-web.account {:address address
                              ;; Dissoc `environment` because it's too much data.
                              :status (dissoc status :convex-web.account-status/environment)}))
    (accounts peer {:start start :end end})))

(defn hero-sequence [^Peer peer]
  (-> (.getConsensusState peer)
      (.getAccount Init/HERO)
      (.getSequence)))

(defn ^Transfer transfer-transaction [{:keys [address nonce target amount]}]
  (Transfer/create
    (convex-web.convex/address address)
    ^Long nonce
    (convex-web.convex/address target)
    ^Long amount))

(defn ^Invoke invoke-transaction [{:keys [nonce address command]}]
  (Invoke/create ^Address address ^Long nonce command))

(defn ^SignedData sign [^AKeyPair signer ^ATransaction transaction]
  (SignedData/create signer transaction))

(defn wrap-do [^AList x]
  (.cons x (Symbol/create "do")))

(defn execute-query [^Peer peer ^Object form & [{:keys [address]}]]
  (let [^Context context (if address
                           (.executeQuery peer form (convex-web.convex/address address))
                           (.executeQuery peer form))]
    (.getValue context)))

(defn ^Result query [^Convex client {:keys [source address lang] :as q}]
  (let [_ (log/debug "Query" q)

        obj (try
              (case lang
                :convex-lisp
                (wrap-do (Reader/readAll source))

                :convex-scrypt
                (ScryptNext/readSyntax source))
              (catch Throwable ex
                (throw (ex-info "Syntax error." {::anomalies/message (ex-message ex)
                                                 ::anomalies/category ::anomalies/incorrect}))))

        ^Address address (when address
                           (convex-web.convex/address address))]
    (try
      (if address
        (.querySync client obj address)
        (.querySync client obj))
      (catch Exception ex
        (let [message "Failed to get Query result."
              category (or (throwable-category ex) ::anomalies/fault)]
          (log/error ex message)
          (throw (ex-info message
                          (merge q {::anomalies/message (ex-message ex)
                                    ::anomalies/category category})
                          ex)))))))

(defn ^Result transact
  "Transact-sync a SignedData with a default timeout.

   Returns Result.

   Throws ExceptionInfo."
  [^Convex client ^SignedData signed-data]
  (try
    (.transactSync client signed-data 1000)
    (catch Exception ex
      (let [message "Failed to get Transaction result."
            category (or (throwable-category ex) ::anomalies/fault)]
        (log/error ex message)
        (throw (ex-info message
                        {::anomalies/message (ex-message ex)
                         ::anomalies/category category}
                        ex))))))

(defn ^Result transacta
  "Transact-sync an ATransaction with a default timeout.

   Returns Result.

   Throws ExceptionInfo."
  [^Convex client ^ATransaction atransaction]
  (try
    (.transactSync client atransaction 1000)
    (catch Exception ex
      (let [message "Failed to get Transaction result."
            category (or (throwable-category ex) ::anomalies/fault)]
        (log/error ex message)
        (throw (ex-info message
                        {::anomalies/message (ex-message ex)
                         ::anomalies/category category}
                        ex))))))

(defn create-account
  "Creates a new Account on the network.

   Returns a pair of KeyPair and Address."
  [{:keys [^Convex client
           ^AKeyPair signer-key-pair
           ^Address signer-address
           ^Long nonce]}]
  (let [^AKeyPair generated-key-pair (AKeyPair/generate)

        ^AccountKey account-key (.getAccountKey generated-key-pair)

        ^String public-key-str (.toChecksumHex account-key)

        command (read-source (str "(create-account 0x" public-key-str ")") :convex-lisp)

        tx-data {:nonce nonce
                 :address signer-address
                 :command command}

        ^Result result (->> (invoke-transaction tx-data)
                            (sign signer-key-pair)
                            (transact client))]

    ;; Result is an Address.
    [generated-key-pair (.getValue result)]))

(defn ^Result faucet
  "Transfers `amount` from Hero (see `Init/HERO`) to `target`."
  [^Convex client {:keys [nonce target amount]}]
  (->> (transfer-transaction {:nonce nonce :target target :amount amount})
       (sign Init/HERO_KP)
       (transact client)))

(defn convex-core-reference []
  (->> (core-metadata)
       (map
         (fn [[sym metadata]]
           (let [{:keys [doc]} (datafy metadata)

                 {:keys [description examples signature type]} doc]
             {:doc
              (merge {:description description
                      :signature signature
                      :symbol (.toString (.getName sym))
                      :examples examples}
                     (when type
                       {:type (keyword type)}))})))
       (sort-by (comp :symbol :doc))))


(defn key-pair-data [^AKeyPair key-pair]
  {:convex-web.key-pair/account-key (.toChecksumHex (.getAccountKey key-pair))
   :convex-web.key-pair/private-key (.toHexString (.getEncodedPrivateKey key-pair))})

(defn ^AKeyPair create-key-pair [{:convex-web.key-pair/keys [account-key private-key]}]
  (AKeyPair/create (AccountKey/fromChecksumHex account-key) (Blob/fromHex private-key)))

(s/fdef create-key-pair
  :args (s/cat :key-pair :convex-web/key-pair)
  :ret #(instance? AKeyPair %))

(def addresses-lock-ref (atom {}))

(defn lockee [address]
  (let [address (convex-web.convex/address address)]
    (or (get @addresses-lock-ref address)
        (let [lock (Object.)]
          (swap! addresses-lock-ref assoc address lock)
          lock))))

(def ^:dynamic sequence-number-ref (atom {}))

(defn ^Long sequence-number [^Peer peer ^Address address]
  (some-> (.getConsensusState peer)
          (.getAccount address)
          (.getSequence)))

(defn get-sequence-number [address]
  (get @sequence-number-ref (convex-web.convex/address address)))

(defn reset-sequence-number! [address]
  (let [address (convex-web.convex/address address)]
    (swap! sequence-number-ref (fn [m]
                                 (dissoc m address)))))

(defn set-sequence-number! [address next]
  (let [address (convex-web.convex/address address)]
    (swap! sequence-number-ref (fn [m]
                                 (assoc m address next)))))