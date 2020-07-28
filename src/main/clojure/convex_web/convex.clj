(ns convex-web.convex
  (:require [clojure.string :as str])
  (:import (convex.core.data Keyword Symbol Syntax Address AccountStatus SignedData AVector AList ASet AMap)
           (convex.core.lang Core)
           (convex.core Order Block Peer State Init)
           (convex.core.crypto AKeyPair)
           (convex.core.transactions Transfer ATransaction Invoke)
           (convex.net Connection)))

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

(defn con->clj [x]
  (condp instance? x
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
    (into '() (map con->clj x))

    AVector
    (into [] (map con->clj x))

    AMap
    (reduce
      (fn [m [k v]]
        (assoc m (con->clj k) (con->clj v)))
      {}
      x)

    ASet
    (into #{} (map con->clj x))

    nil))

(defn ^Address address [x]
  (cond
    (nil? x)
    (throw (ex-info (str "Can't coerce nil to " (.getName Address) ".") {}))

    (instance? Address x)
    x

    (and (string? x) (str/blank? x))
    (throw (ex-info (str "Can't coerce empty string to " (.getName Address) ".") {}))

    (string? x)
    (Address/fromHex x)

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

(defn account-status-data [^AccountStatus account-status]
  (when account-status
    (let [env (con->clj (.getEnvironment account-status))
          exports? (contains? env '*exports*)
          actor? (.isActor account-status)
          library? (and actor? (not exports?))]
      (merge #:convex-web.account-status {:sequence (.getSequence account-status)
                                          :balance (.getValue (.getBalance account-status))
                                          :actor? actor?
                                          :library? library?
                                          :environment env}
             (when-let [actor-args (.getActorArgs account-status)]
               #:convex-web.account-status {:actor-args (str actor-args)})))))

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
           (let [{:keys [doc]} (con->clj metadata)

                 {:keys [description examples signature type]} doc]
             {:doc
              (merge {:description description
                      :signature signature
                      :symbol (.getName sym)
                      :examples examples}
                     (when type
                       {:type (keyword type)}))})))
       (sort-by (comp :symbol :doc))))