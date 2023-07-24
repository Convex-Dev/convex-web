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

(defn -convex-server ^Server [convex]
  (:server convex))

(defn -convex-client ^Convex [convex]
  (:client convex))

(defn convex-server ^Server [system]
  (-convex-server (convex system)))

(defn convex-peer ^Server [system]
  (.getPeer (-convex-server (convex system))))

(defn convex-client ^Convex [system]
  (-convex-client (convex system)))

(defn convex-world-address
  "Genesis Address."
  ^Address [_system]
  (convex/genesis-address))

(defn convex-world-context ^State [system]
  (convex/server-context (convex-server system)))

(defn convex-world-key-pair ^AKeyPair [system]
  (convex/server-key-pair (convex-server system)))

(defn convex-world-account-checksum-hex ^String [system]
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
