(ns convex-web.system
  (:import (convex.peer Server)
           (convex.net Connection)
           (convex.api Convex)))

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
