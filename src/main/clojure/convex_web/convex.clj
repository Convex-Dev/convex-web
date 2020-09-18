(ns convex-web.convex
  (:require [clojure.string :as str])
  (:import (convex.core.data Keyword Symbol Syntax Address AccountStatus SignedData AVector AList ASet AMap ABlob)
           (convex.core.lang Core Reader ScryptNext RT)
           (convex.core Order Block Peer State Init)
           (convex.core.crypto AKeyPair)
           (convex.core.transactions Transfer ATransaction Invoke)
           (convex.net Connection)))

(defmacro execute [context form]
  `(let [^String source# ~(pr-str form)
         context# (.execute ~context (.getResult (.expandCompile ~context (Reader/read source#))))]
     (if (.isExceptional context#)
       (.getExceptional context#)
       (.getResult context#))))

(defn execute-scrypt [context source]
  (let [context (.execute context (.getResult (.expandCompile context (ScryptNext/readSyntax source))))]
    (if (.isExceptional context)
      (.getExceptional context)
      (.getResult context))))

(defn ^String address->checksum-hex [^Address address]
  (.toChecksumHex address))

(defn core-metadata
  "Core metadata indexed by Symbol."
  []
  (reduce
    (fn [m [^Symbol sym ^Syntax syn]]
      (assoc m sym (.getMeta syn)))
    {}
    Core/CORE_NAMESPACE))

(defn datafy
  ([x]
   (datafy x {:default str}))
  ([x {:keys [default]}]
   (let [datafy #(datafy % {:default default})]
     (condp instance? x
       Boolean
       x

       Character
       x

       Long
       x

       Double
       x

       String
       x

       Keyword
       (keyword (.getName x))

       Symbol
       (symbol (some-> x (.getNamespace) (.getName)) (.getName x))

       AList
       (map datafy x)

       AVector
       (mapv datafy x)

       AMap
       (reduce
         (fn [m [k v]]
           (assoc m (datafy k) (datafy v)))
         {}
         x)

       ASet
       (into #{} (map datafy x))

       Syntax
       (datafy (.getValue ^Syntax x))

       (default x)))))

(defn ^Address address [x]
  (cond
    (nil? x)
    (throw (ex-info (str "Can't coerce nil to " (.getName Address) ".") {}))

    (instance? Address x)
    x

    (instance? ABlob x)
    (or (RT/address x) (throw (ex-info "Blob cannot be converted to Address." {:blob x})))

    (and (string? x) (str/blank? x))
    (throw (ex-info (str "Can't coerce empty string to " (.getName Address) ".") {}))

    (string? x)
    (let [hex-text (if (str/starts-with? "0x" x)
                     (subs x 2)
                     x)]
      (Address/fromHex hex-text))

    :else
    (throw (ex-info (str "Can't coerce " (.getName (type x)) " to " (.getName Address) ".") {:address x
                                                                                             :type (type x)}))))

(defn metadata [sym]
  (let [sym (cond
              (instance? Symbol sym)
              sym

              (string? sym)
              (Symbol/create sym)

              :else
              (throw (ex-info "'sym' must be either a convex.core.data.Symbol or String." {:sym sym})))]
    (get (core-metadata) sym)))

(defn ^Order peer-order [^Peer peer]
  (.getPeerOrder peer))

(defn consensus-point [^Order order]
  (.getConsensusPoint order))

(defn transaction [^ATransaction atransaction]
  (cond
    (instance? Transfer atransaction)
    #:convex-web.transaction {:type :convex-web.transaction.type/transfer
                              :target (.toChecksumHex (.getTarget ^Transfer atransaction))
                              :amount (.getAmount ^Transfer atransaction)
                              :sequence (.getSequence ^ATransaction atransaction)}

    (instance? Invoke atransaction)
    #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                              :source (str (.getCommand ^Invoke atransaction))
                              :sequence (.getSequence ^ATransaction atransaction)}))

(defn signed-data-transaction [^SignedData signed-data]
  #:convex-web.signed-data {:address (.toChecksumHex (.getAddress signed-data))
                            :value (transaction (.getValue signed-data))})

(defn block [index ^Block block]
  #:convex-web.block {:index index
                      :timestamp (.getTimeStamp block)
                      :peer (.toChecksumHex (.getPeer block))
                      :transactions (map signed-data-transaction (.getTransactions block))})

(defn blocks [^Peer peer & [{:keys [start end]}]]
  (let [order (peer-order peer)
        start (or start 0)
        end (or end (consensus-point order))]
    (reduce
      (fn [blocks index]
        (conj blocks (block index (.getBlock order index))))
      []
      (range start end))))

(defn blocks-indexed [^Peer peer]
  (let [order (peer-order peer)]
    (reduce
      (fn [blocks index]
        (assoc blocks index (block index (.getBlock order index))))
      {}
      (range (consensus-point order)))))

(defn accounts [^Peer peer & [{:keys [start end]}]]
  ;; Get timestamp - from state
  (let [^State state (.getConsensusState peer)
        start (or start 0)
        end (or end (count (.getAccounts state)))]
    (reduce
      (fn [m i]
        (let [[address status] (.entryAt (.getAccounts state) i)]
          (assoc m address status)))
      {}
      (range start end))))

(defn ^AccountStatus account-status [^Peer peer string-or-address]
  (let [address->status (accounts peer)]
    (address->status (address string-or-address))))

(defn syntax-data [^Syntax syn]
  #:convex-web.syntax {:source (.getSource syn)
                       :meta (datafy (.getMeta syn))
                       :value (datafy (.getValue syn))})

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
                                          :balance (.getValue (.getBalance account-status))
                                          :environment env
                                          :actor? actor?
                                          :library? library?
                                          :memory-size (.getMemorySize account-status)
                                          :allowance (.getAllowance account-status)
                                          :type (cond
                                                  library? :library
                                                  actor? :actor
                                                  :else :user)}))))

(defn hero-sequence [^Peer peer]
  (-> (.getConsensusState peer)
      (.getAccount Init/HERO)
      (.getSequence)))

(defn ^Transfer transfer [{:keys [nonce target amount]}]
  (Transfer/create ^Long nonce (address target) ^Long amount))

(defn ^SignedData sign [^AKeyPair signer ^ATransaction transaction]
  (SignedData/create signer transaction))

(defn ^Long transact [^Connection conn ^SignedData data]
  (.sendTransaction conn data))

(defn ^AKeyPair generate-account [^Connection conn ^AKeyPair signer ^Long nonce]
  ;; TODO
  ;; Extract transfer/transaction.
  (let [^AKeyPair generated-key-pair (AKeyPair/generate)
        ^Address generated-address (.getAddress generated-key-pair)]

    (->> (transfer {:nonce nonce :target generated-address :amount 100000000})
         (sign signer)
         (transact conn))

    generated-key-pair))

(defn faucet
  "Transfers `amount` from Hero (see `Init/HERO`) to `target`."
  [^Connection conn {:keys [nonce target amount]}]
  (->> (transfer {:nonce nonce :target target :amount amount})
       (sign Init/HERO_KP)
       (transact conn)))

(defn reference []
  (->> (core-metadata)
       (map
         (fn [[sym metadata]]
           (let [{:keys [doc]} (datafy metadata)

                 {:keys [description examples signature type]} doc]
             {:doc
              (merge {:description description
                      :signature signature
                      :symbol (.getName sym)
                      :examples examples}
                     (when type
                       {:type (keyword type)}))})))
       (sort-by (comp :symbol :doc))))