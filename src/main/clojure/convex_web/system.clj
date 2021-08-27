(ns convex-web.system
  (:require [convex-web.convex :as convex])
  (:import 
   (convex.peer Server)
   (convex.api Convex)
   (convex.core State)
   (convex.core.data Address)
   (convex.core.crypto AKeyPair)))

(defn convex
  "Convex Component."
  [system]
  (:convex system))

(defn web-server
  "Web Server Component."
  [system]
  (:web-server system))

(defn datalevin
  "Datalevin Component."
  [system]
  (:datalevin system))

(defn config
  "Config component."
  [system]
  (:config system))


;; -- Convex

(defn ^Server -convex-server [convex]
  (:server convex))

(defn ^Convex -convex-client [convex]
  (:client convex))

(defn ^Server convex-server [system]
  (-convex-server (convex system)))

(defn ^Server convex-peer [system]
  (.getPeer (-convex-server (convex system))))

(defn ^Convex convex-client [system]
  (-convex-client (convex system)))

(defn ^Address convex-world-address
  "Peer Controller Address."
  [system]
  (convex/server-peer-controller (convex-server system)))

(defn ^State convex-world-context [system]
  (convex/server-context (convex-server system)))

(defn ^AKeyPair convex-world-key-pair [system]
  (convex/server-key-pair (convex-server system)))

(defn ^String convex-world-account-checksum-hex [system]
  (convex/server-account-checksum-hex (convex-server system)))


;; -- DB

(defn -db-conn
  [db]
  (:conn db))

(defn db-conn
  "Connections are lightweight in-memory structures (~atoms) with direct
   support of transaction listeners ([[listen!]], [[unlisten!]]) and other
   handy DataScript APIs ([[transact!]], [[reset-conn!]], [[db]]).

   To access underlying immutable DB value, deref: `@conn`."
  [system]
  (-db-conn (datalevin system)))

(defn db
  "Returns the underlying immutable DataScript database value from a connection."
  [system]
  @(-db-conn (datalevin system)))


;; -- Consumer

(defn -consumer-consumer [consumer]
  (:consumer consumer))


;; -- Site

(defn site-asset-prefix-url [system]
  (get-in (config system) [:config :site :asset-prefix-url]))

(defn site-config [system]
  (select-keys (get-in (config system) [:config :site]) [:security]))
