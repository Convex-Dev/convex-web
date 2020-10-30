(ns convex-web.peer
  (:refer-clojure :exclude [read])
  (:require [convex-web.convex :as convex])
  (:import (convex.peer Server)
           (convex.core.lang Context)
           (convex.core.data Address AccountStatus)
           (convex.core Peer State)))

(defn ^Peer peer [^Server server]
  (.getPeer server))

(defn ^State consensus-state [^Peer peer]
  (.getConsensusState peer))

(defn ^AccountStatus account-status [^State state ^Address account]
  (.getAccount state account))

(defn ^Long account-sequence [^AccountStatus account]
  (.getSequence account))

(defn ^Long sequence-number [^Peer peer ^Address address]
  (some-> (consensus-state peer)
          (account-status address)
          (account-sequence)))

(defn query [^Peer peer ^Object form & [{:keys [address]}]]
  (let [^Context context (if address
                           (.executeQuery peer form (convex/address address))
                           (.executeQuery peer form))]
    (.getValue context)))

