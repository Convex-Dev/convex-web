(ns convex-web.peer
  (:refer-clojure :exclude [read])
  (:require [cognitect.anomalies :as anomalies]
            [convex-web.convex :as convex])
  (:import (convex.peer Server)
           (convex.core.lang Reader ScryptNext)
           (convex.core.data Address AccountStatus Symbol AList)
           (convex.core Peer State)
           (convex.core.transactions Invoke ATransaction Transfer)))

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

(defn query [^Peer peer {:keys [^String source address lang]}]
  (let [form (try
               (case lang
                 :convex-lisp
                 (wrap-do (Reader/readAll source))

                 :convex-scrypt
                 (ScryptNext/readSyntax source))
               (catch Throwable ex
                 (throw (ex-info "Syntax error." {::anomalies/message (ex-message ex)
                                                  ::anomalies/category ::anomalies/incorrect}))))
        context (.executeQuery peer form (convex/address address))]
    (.getValue context)))

