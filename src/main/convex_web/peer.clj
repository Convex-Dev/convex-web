(ns convex-web.peer
  (:import (convex.net ResultConsumer Connection)
           (convex.peer Server)
           (convex.core.lang Reader)
           (convex.core.data Address AccountStatus Symbol AList)
           (convex.core Peer State)
           (convex.core.transactions Invoke ATransaction Transfer)
           (convex.core.crypto AKeyPair)
           (convex.core.store Stores)))

;; (Stores/etCurrent Stores/CLIENT_STORE)

(defn ^Connection conn [^Server server ^ResultConsumer consumer]
  (Connection/connect (.getHostAddress server) consumer Stores/CLIENT_STORE))

(defn ^Peer peer [^Server server]
  (.getPeer server))

(defn ^State consensus-state [^Peer peer]
  (.getConsensusState peer))

(defn ^AccountStatus account-status [^State state ^Address account]
  (.getAccount state account))

(defn ^Long account-sequence [^AccountStatus account]
  (.getSequence account))

(defn wrap-do [^AList x]
  (.cons x (Symbol/create "do")))

(defn cond-wrap-do [^AList x]
  (let [form1 (first x)
        form2 (second x)]
    (if form2
      (wrap-do x)
      form1)))

(defn ^Long sequence-number [^Peer peer ^Address account]
  (-> (consensus-state peer)
      (account-status account)
      (account-sequence)))

(defn ^ATransaction invoke-transaction [^Long nonce ^String source]
  (Invoke/create nonce (cond-wrap-do (Reader/readAll source))))

(defn ^ATransaction transfer-transaction [^Long nonce ^Address address ^Long amount]
  (Transfer/create nonce address amount))

(defn query [^Connection conn address source]
  (let [^Address address (if (string? address) (Address/fromHex address) address)]
    (.sendQuery conn (cond-wrap-do (Reader/readAll source)) address)))

(defn transact [^Connection conn ^ATransaction transaction ^AKeyPair sign-key-pair]
  (.sendTransaction conn (.signData sign-key-pair transaction)))
