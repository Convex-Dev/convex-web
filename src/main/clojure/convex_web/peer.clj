(ns convex-web.peer
  (:import (convex.net ResultConsumer Connection)
           (convex.peer Server)
           (convex.core.lang Reader ScryptNext)
           (convex.core.data Address AccountStatus Symbol AList)
           (convex.core Peer State)
           (convex.core.transactions Invoke ATransaction Transfer)
           (convex.core.store Stores)))

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

(defn ^Long sequence-number [^Peer peer ^Address address]
  (some-> (consensus-state peer)
          (account-status address)
          (account-sequence)))

(defn ^ATransaction invoke-transaction [^Long nonce ^String source language]
  (let [object (case language
                 :convex-lisp
                 (cond-wrap-do (Reader/readAll source))

                 :convex-scrypt
                 (ScryptNext/readSyntax source))]
    (Invoke/create nonce object)))

(defn ^ATransaction transfer-transaction [^Long nonce ^Address address ^Long amount]
  (Transfer/create nonce address amount))

(defn query [^Connection conn address source]
  (let [^Address address (if (string? address) (Address/fromHex address) address)]
    (.sendQuery conn (cond-wrap-do (Reader/readAll source)) address)))

