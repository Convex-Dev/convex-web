(ns convex-web.convex
  (:require 
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   
   [cognitect.anomalies :as anomalies]

   [convex-web.site.sandbox.hiccup :as hiccup])
  (:import 
   (convex.peer Server)
   (convex.core.init Init)
   (convex.core.data Keyword Symbol Syntax Address AccountStatus SignedData AVector AList ASet AMap ABlob Blob AccountKey ACell AHashMap)
   (convex.core.data.prim CVMBool)
   (convex.core.lang Core Reader RT Context AFn)
   (convex.core.lang.impl Fn CoreFn)
   (convex.core Order Block Peer State Result)
   (convex.core.crypto AKeyPair PFXTools Ed25519KeyPair)
   (convex.core.transactions Transfer ATransaction Invoke Call)
   (convex.core.util Utils)
   (convex.api Convex)
   (convex.core.data.prim CVMByte)
   (java.util.concurrent TimeoutException)
   (java.net InetSocketAddress)))

(set! *warn-on-reflection* true)

(defn key-store
  "Returns a java.security.KeyStore for f.
  
  Where f can be a string or a file.
  
  Creates keystore if it doesn't exit."
  (^java.security.KeyStore [f]
   (key-store f nil))
  
  (^java.security.KeyStore [f passphrase]
   (let [f (io/file f)]
     (try
       (PFXTools/loadStore f passphrase)
       (catch java.io.FileNotFoundException _
         (PFXTools/createStore f passphrase))))))

(defn key-store-aliases 
  "Returns a seq of aliases (as string) of key-store."
  [^java.security.KeyStore key-store]
  (iterator-seq (.asIterator (.aliases key-store))))

(defn save-key-pair
  "Save AKeyPair to disk.
  
  key-store-file can be a string or a file.
  
  key-pair-passphrase is required."
  [{:keys [key-store
           key-store-passphrase
           key-store-file
           key-pair 
           key-pair-passphrase]}]
  (let [key-store-file (io/file key-store-file)]
    
    ;; Creates the directory named by this abstract pathname, 
    ;; including any necessary but nonexistent parent directories.
    ;; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/File.html
    (when-let [parent (.getParentFile key-store-file)]
      (when-not (.exists parent)
        (.mkdirs parent)))
    
    ;; Saves key-pair in-memory.
    (PFXTools/setKeyPair key-store key-pair key-pair-passphrase)
    
    ;; Persist modified key store to disk.
    (PFXTools/saveStore key-store key-store-file key-store-passphrase)
    
    nil))

(defn ^AKeyPair generate-key-pair []
  (AKeyPair/generate))

(defn account-key ^AccountKey [^String checksum-hex]
  (AccountKey/fromChecksumHex checksum-hex))

(defn ^AccountKey account-key-from-hex 
  "Constructs an AccountKey object from a hex string."
  [^String hex]
  (AccountKey/fromHex hex))

(defn key-pair-data 
  "Returns AKeyPair as a map."
  [^Ed25519KeyPair key-pair]
  {:convex-web.key-pair/account-key (.toChecksumHex (.getAccountKey key-pair))
   :convex-web.key-pair/private-key (.toHexString (.getEncodedPrivateKey key-pair))
   :convex-web.key-pair/seed (.toHexString (.getSeed key-pair))})

(defn ^AKeyPair create-key-pair 
  "Creates AKeyPair from a map."
  [{:convex-web.key-pair/keys [account-key private-key]}]
  (AKeyPair/create 
    (AccountKey/fromChecksumHex account-key)
    (Blob/fromHex private-key)))

(s/fdef create-key-pair
  :args (s/cat :key-pair :convex-web/key-pair)
  :ret #(instance? AKeyPair %))

(defn read-source [source]
  (try
    (let [^AList l (Reader/readAll source)
          form1 (first l)
          form2 (second l)]
      (if form2
        (.cons l (Symbol/create "do"))
        form1))
    (catch Throwable ex
      (throw (ex-info (str "Reader error: " (ex-message ex)) {::anomalies/category ::anomalies/incorrect
                                                              :convex-web.result/error-code :READER})))))

(defmacro execute [context form]
  `(let [^String source# ~(pr-str form)
         context# (.execute ~context (.getResult (.expandCompile ~context (Reader/read source#))))]
     (if (.isExceptional context#)
       (.getExceptional context#)
       (.getResult context#))))

(defn execute-string* 
  "Executes and returns a new Context."
  [^Context context ^String source]
  (.execute context (.getResult (.expandCompile context (Reader/read source)))))

(defn execute-string [^Context context ^String source]
  (let [new-context (.execute context (.getResult (.expandCompile context (Reader/read source))))]
    (if (.isExceptional new-context)
      (.getExceptional new-context)
      (.getResult new-context))))

(defn execute-string-unsafe [^Context context ^String source]
  (let [new-context (.execute context (.getResult (.expandCompile context (Reader/read source))))]
    (if (.isExceptional new-context)
      (throw (ex-info (.toString (.getExceptional new-context))
               {:exceptional (.getExceptional new-context)}))
      (.getResult new-context))))

(defn ^Address genesis-address []
  (Init/getGenesisAddress))

(defn server-peer-controller
  "Gets the Peer controller Address."
  ^Address [^Server server]
  (.getPeerController server))

(defn server-address 
  "Gets the host address for this Server (including port), or null if closed."
  ^InetSocketAddress [^Server server]
  (.getHostAddress server))

(defn server-key-pair
  "Returns the Keypair for this peer server."
  ^AKeyPair [^Server server]
  (.getKeyPair server))

(defn server-account-key
  ^AccountKey [^Server server]
  (.getAccountKey (server-key-pair server)))

(defn server-account-checksum-hex 
  ^String [^Server server]
  (.toChecksumHex (server-account-key server)))

(defn server-state ^State [^Server server]
  (Init/createState [(server-account-key server)]))

(defn server-context ^Context [^Server server]
  (Context/createFake (server-state server) (server-peer-controller server)))

(defn restore-key-pair
  ^AKeyPair [{:keys [^java.security.KeyStore key-store
                     ^String alias 
                     ^String passphrase]}]
  (try 
    (PFXTools/getKeyPair key-store alias passphrase)
    (catch Exception ex
      (throw (ex-info (ex-message ex)
               {::anomalies/message (ex-message ex)
                ::anomalies/category ::anomalies/incorrect})))))

(defn lookup-metadata
  ([^Context context ^Symbol sym]
   (.lookupMeta context sym))
  ([^Context context ^Address address ^Symbol sym]
   (.lookupMeta context address sym)))

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
    (fn [m [^Symbol sym ^AHashMap metadata]]
      (assoc m sym metadata))
    {}
    Core/METADATA))

(defn datafy
  "Datafy a Convex object `x` to Clojure.

   Throws if there isn't a mapping of a Convex object to Clojure."
  [x]
  (cond
    (nil? x)
    nil

    (instance? Keyword x)
    (keyword (.toString (.getName ^Keyword x)))

    (instance? Symbol x)
    (symbol (.getName ^Symbol x))

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

    (instance? AccountKey x)
    (.toChecksumHex ^AccountKey x)

    (instance? Address x)
    (.longValue ^Address x)

    (instance? AFn x)
    (.toString ^AFn x)

    (instance? ABlob x)
    (.toHexString ^ABlob x)
    
    (instance? CVMByte x)
    (.longValue ^CVMByte x)

    (instance? Syntax x)
    (datafy (.getValue ^Syntax x))

    :else
    (let [e (fn []
              (throw (ex-info (str "Can't datafy " (pr-str x) " " (some-> ^Object x (.getClass) (.getName)) ".") {:object x})))]
      (try
        (let [x' (RT/jvm x)]
          (if (identical? x x')
            (e)
            x'))
        (catch Exception _
          (e))))))

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
    (let [message (str "Can't coerce " (pr-str x) " to " (.getName Address) ".")]
      (throw (ex-info message {::anomalies/message message
                               ::anomalies/category ::anomalies/incorrect})))))

(defn ^Address address-safe [x]
  (try
    (address x)
    (catch Exception _
      nil)))

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
  (let [^ACell result-id (.getID result)
        ^ACell result-error-code (.getErrorCode result)
        ^ACell result-value (.getValue result)
        ^AVector result-trace (.getTrace result)]
    (merge #:convex-web.result {:id (datafy result-id)
                                :type (or (some-> result-value .getType .toString) "Nil")
                                :value (or (some-> result-value Utils/print) "nil")}
      
      ;; Interactive Syntax.
      (when (instance? Syntax result-value)
        (let [^AHashMap syntax-meta (.getMeta ^Syntax result-value)

              ^CVMBool interactive? (.get syntax-meta (Keyword/create "interactive?"))

              interactive? (some-> interactive? .booleanValue)

              ;; It's never nil.
              interactive? (or interactive? false)]

          (merge {:convex-web.result/interactive? interactive?}
            (when interactive?
              {:convex-web.result/metadata (datafy syntax-meta)
               :convex-web.result/interactive (s/conform ::hiccup/element (datafy result-value))}))))

      (when (instance? CoreFn result-value)
        {:convex-web.result/metadata (datafy (metadata (.getSymbol ^CoreFn result-value)))})
      
      (when result-error-code
        {:convex-web.result/error-code (datafy result-error-code)
         :convex-web.result/trace (datafy result-trace)}))))

(defn transaction-result-data [^ATransaction atransaction ^Result result]
  (let [tx (cond
             (instance? Transfer atransaction)
             #:convex-web.transaction 
             {:type :convex-web.transaction.type/transfer
              :target (.longValue (.getTarget ^Transfer atransaction))
              :amount (.getAmount ^Transfer atransaction)
              :sequence (.getSequence ^ATransaction atransaction)}
             
             (instance? Invoke atransaction)
             #:convex-web.transaction 
             {:type :convex-web.transaction.type/invoke
              :source (str (.getCommand ^Invoke atransaction))
              :sequence (.getSequence ^ATransaction atransaction)}
             
             (instance? Call atransaction)
             #:convex-web.transaction 
             {:type :convex-web.transaction.type/call})]
    
    (merge tx {:convex-web.transaction/result (result-data result)})))

(defn block-data [^Peer peer ^Long index ^Block block]
  #:convex-web.block 
  {:index index
   :timestamp (.getTimeStamp block)
   :peer (.toChecksumHex (.getPeer block))
   :transactions
   (map-indexed
     (fn [^Long tx-index ^SignedData signed-data]
       (let [^ATransaction transaction (.getValue signed-data)]
         #:convex-web.signed-data 
         {:address (.longValue (.getAddress transaction))
          :account-key (.toChecksumHex (.getAccountKey signed-data))
          :value (transaction-result-data 
                   (.getValue signed-data)
                   (.getResult peer index tx-index))}))
     (.getTransactions block))})

(defn blocks-data [^Peer peer & [{:keys [start end]}]]
  (let [order (peer-order peer)
        start (or start 0)
        end (or end (consensus-point order))]

    (log/debug "Query Blocks range:" start end)

    (reduce
      (fn [blocks index]
        (conj blocks (block-data peer index (.getBlock order index))))
      []
      (range start end))))

(defn environment-data
  "Account Status' environment data.

   Mapping of symbol to cell."
  [environment]
  (reduce
    (fn [env [^Symbol sym ^ACell acell]]
      (assoc env (datafy sym) (datafy acell)))
    {}
    environment))

(defn account-status-data [^AccountStatus account-status]
  (when account-status
    (let [env (environment-data (.getEnvironment account-status))
          
          ;; Reify exported functions giving it a name and arglists attributes.
          exports (map 
                    (fn [sym]
                      (let [f (.get (.getEnvironment account-status) sym)
                            
                            arglists  (cond                              
                                        (instance? Fn f)
                                        (map 
                                          (fn [^Symbol param]
                                            (.getName param))
                                          (.getParams ^Fn f))
                                        
                                        ;; TODO: Handle MultiFn.
                                        :else
                                        [])]
                        {:name (datafy sym)
                         :arglists arglists}))
                    (.getCallableFunctions account-status))
          
          actor? (.isActor account-status)
          
          library? (and actor? (empty? exports))]
      
      (merge #:convex-web.account-status {:account-key (some-> account-status .getAccountKey .toChecksumHex)
                                          :controller (datafy (.getController account-status))
                                          :sequence (.getSequence account-status)
                                          :balance (.getBalance account-status)
                                          :environment env
                                          :exports exports
                                          :actor? actor?
                                          :library? library?
                                          :memory-size (.getMemorySize account-status)
                                          :allowance (.getMemory account-status)
                                          :type (cond
                                                  library? :library
                                                  actor? :actor
                                                  :else :user)}))))

(defn accounts-indexed
  "Returns a mapping of Address long to Account Status."
  [^Peer peer & [{:keys [start end]}]]
  (let [state (.getConsensusState peer)
        start (or start 0)
        end (or end (.count (.getAccounts ^State state)))
        all (.getAccounts ^State state)]
    (reduce
      (fn [acc address-long]
        (assoc acc address-long (.get all (RT/cvm address-long))))
      {}
      (range start end))))

(defn ranged-accounts [^Peer peer & [{:keys [start end]}]]
  (reduce-kv
    (fn [all address-long account-status]
      (conj all #:convex-web.account {:address address-long
                                      :status (dissoc (account-status-data account-status) :convex-web.account-status/environment)}))
    []
    (accounts-indexed peer {:start start :end end})))

(defn ^AccountStatus account-status [^Peer peer ^Address address]
  (when address
    (get (accounts-indexed peer) (.longValue address))))

(defn ^Transfer transfer-transaction [{:keys [address nonce target amount]}]
  (Transfer/create
    (convex-web.convex/address address)
    ^Long nonce
    (convex-web.convex/address target)
    ^Long amount))

(defn ^Invoke invoke-transaction [{:keys [nonce address command]}]
  (Invoke/create ^Address address ^Long nonce ^ACell command))

(defn ^SignedData sign [^AKeyPair signer ^ATransaction transaction]
  (SignedData/create signer transaction))

(defn execute-query [^Peer peer ^Object form & [{:keys [address]}]]
  (let [^Context context (if address
                           (.executeQuery peer form (convex-web.convex/address address))
                           (.executeQuery peer form))]
    (.getValue context)))

(defn ^Result query [^Convex client {:keys [source address] :as q}]
  (let [^ACell acell (read-source source)]
    (try
      (log/debug "Query sync" q)

      (if address
        (.querySync client ^ACell acell (convex-web.convex/address address))
        (.querySync client ^ACell acell))

      (catch Exception ex
        (let [message "Failed to get Query result."

              category (or (throwable-category ex) ::anomalies/fault)]

          (log/error ex message)

          (throw (ex-info message
                   (merge q {::anomalies/message (ex-message ex)
                             ::anomalies/category category})
                   ex)))))))

(defn ^Result transact-signed
  "Transact-sync a SignedData with a default timeout.

   Returns Result.

   Throws ExceptionInfo."
  [^Convex client ^SignedData signed-data]
  (try
    (.transactSync client signed-data 10000)
    (catch Exception ex
      (let [message "Failed to get Transaction result."
            category (or (throwable-category ex) ::anomalies/fault)]
        (log/error ex message)
        (throw (ex-info message
                        {::anomalies/message (ex-message ex)
                         ::anomalies/category category}
                        ex))))))

(defn ^Result transact
  "Transact-sync an ATransaction with a default timeout.

   Returns Result.

   Throws ExceptionInfo."
  [^Convex client ^ATransaction atransaction]
  (try
    (.transactSync client atransaction 10000)
    (catch Exception ex
      (let [message "Failed to get Transaction result."
            category (or (throwable-category ex) ::anomalies/fault)]
        (log/error ex message)
        (throw (ex-info message
                        {::anomalies/message (ex-message ex)
                         ::anomalies/category category}
                        ex))))))

(defn ^Address create-account
  "Creates a new Account on the network.

   Returns Address.

   Throws ExceptionInfo if the transaction fails."
  [^Convex client ^Address genesis-address ^AccountKey account-key]
  (let [^String account-public-key (.toChecksumHex account-key)
        
        command (read-source (str "(create-account 0x" account-public-key ")"))

        tx-data {:nonce 0
                 :address genesis-address
                 :command command}

        ^Result result (->> (invoke-transaction tx-data)
                            (transact client))]

    (if (.isError result)
      (let [error-code (datafy-safe (.getErrorCode result))
            error-result (datafy-safe (.getValue result))
            message (pr-str error-code error-result)]
        (throw (ex-info message {::anomalies/message message
                                 ::anomalies/category ::anomalies/fault
                                 :result result})))
      (.getValue result))))

(defn ^Result faucet
  "Transfers `amount` from Hero (see `Init/HERO`) to `target`."
  [^Convex client {:keys [address target amount]}]
  (->> (transfer-transaction
         {:address address
          :nonce 0
          :target target
          :amount amount})
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
                      :symbol (.toString (.getName ^Symbol sym))
                      :examples examples}
                     (when type
                       {:type (keyword type)}))})))
       (sort-by (comp :symbol :doc))))


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


(defn library-metadata [^Context context library-name]
  (let [source (str "(account (call *registry* (cns-resolve '" library-name ")))")]
    (try
      (let [^AccountStatus account-status (execute-string-unsafe context source)
            
            ^AHashMap metadata (.getMetadata account-status)]
        (into {}
          (map
            (fn [[sym _]]
              [(datafy sym) (datafy (.get metadata sym))]))
          (.getEnvironment account-status)))
      (catch Exception ex
        (throw (ex-info (ex-message ex) {:source source} ex))))))

(defn library-reference
  "Metadata for Convex core libraries.
  
   It's a mapping of library name to its metadata."
  [^Context context]
  (let [libraries ["asset.box"
                   "asset.box.actor"
                   "asset.nft.simple"
                   "asset.nft.tokens"
                   "convex.asset"
                   "convex.core"
                   "convex.fungible"
                   "convex.play"
                   "convex.registry"
                   "convex.trust"
                   "convex.trusted-oracle"
                   "convex.trusted-oracle.actor"]]
    (->> libraries
      (map
        (fn [library-name]
          [library-name (library-metadata context library-name)]))
      (into {}))))