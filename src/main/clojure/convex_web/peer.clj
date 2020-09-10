(ns convex-web.peer
  (:refer-clojure :exclude [read])
  (:require [cognitect.anomalies :as anomalies])
  (:import (convex.net ResultConsumer Connection)
           (convex.peer Server)
           (convex.core.lang Reader ScryptNext)
           (convex.core.data Address AccountStatus Symbol AList)
           (convex.core Peer State Init)
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

(defn read [source lang]
  (try
    (case lang
      :convex-lisp
      (cond-wrap-do (Reader/readAll source))

      :convex-scrypt
      (ScryptNext/readSyntax source))
    (catch Throwable ex
      (throw (ex-info "Syntax error." {::anomalies/message (ex-message ex)
                                       ::anomalies/category ::anomalies/incorrect})))))

(defn ^ATransaction create-invoke [^Long nonce command]
  (Invoke/create nonce command))

(defn ^ATransaction invoke-transaction [^Long nonce ^String source lang]
  (let [object (case lang
                 :convex-lisp
                 (cond-wrap-do (Reader/readAll source))

                 :convex-scrypt
                 (ScryptNext/readSyntax source))]
    (Invoke/create nonce object)))

(defn ^ATransaction transfer-transaction [^Long nonce ^Address address ^Long amount]
  (Transfer/create nonce address amount))

(defn send-query [^Connection conn address source]
  (let [^Address address (if (string? address) (Address/fromHex address) address)]
    (.sendQuery conn (cond-wrap-do (Reader/readAll source)) address)))

(defn query [^Peer peer {:keys [^String source ^Address address lang]}]
  (let [form (try
               (case lang
                 :convex-lisp
                 (wrap-do (Reader/readAll source))

                 :convex-scrypt
                 (ScryptNext/readSyntax source))
               (catch Throwable ex
                 (throw (ex-info "Syntax error." {::anomalies/message (ex-message ex)
                                                  ::anomalies/category ::anomalies/incorrect}))))
        context (.executeQuery peer form address)]
    (.getValue context)))

