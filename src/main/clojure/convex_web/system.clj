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

(defn datascript
  "DataScript Component."
  [system]
  (:datascript system))


;; -- Convex

(defn ^Server -convex-server [convex]
  (:server convex))

(defn ^Connection -convex-conn [convex]
  (:conn convex))

(defn ^Convex -convex-client [convex]
  (:client convex))

(defn ^Server convex-server [system]
  (-convex-server (convex system)))

(defn ^Server convex-peer-server [system]
  (.getPeer (-convex-server (convex system))))

(defn ^Connection convex-conn [system]
  (-convex-conn (convex system)))

(defn ^Convex convex-client [system]
  (-convex-client (convex system)))


;; -- DataScript

(defn -datascript-conn
  "Connections are lightweight in-memory structures (~atoms) with direct
   support of transaction listeners ([[listen!]], [[unlisten!]]) and other
   handy DataScript APIs ([[transact!]], [[reset-conn!]], [[db]]).

   To access underlying immutable DB value, deref: `@conn`."
  [datascript]
  (:conn datascript))

(defn datascript-conn
  "Connections are lightweight in-memory structures (~atoms) with direct
   support of transaction listeners ([[listen!]], [[unlisten!]]) and other
   handy DataScript APIs ([[transact!]], [[reset-conn!]], [[db]]).

   To access underlying immutable DB value, deref: `@conn`."
  [system]
  (-datascript-conn (datascript system)))

(defn db
  "Returns the underlying immutable DataScript database value from a connection."
  [system]
  @(-datascript-conn (datascript system)))


;; -- Consumer

(defn -consumer-consumer [consumer]
  (:consumer consumer))


;; -- Site

(defn site-asset-prefix-url [system]
  (get-in system [:config :config :site :asset-prefix-url]))
