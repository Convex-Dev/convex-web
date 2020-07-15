(ns convex-web.component
  (:require [convex-web.comms :as comms]
            [convex-web.system :as system]
            [convex-web.peer :as peer]
            [convex-web.web-server :as web-server]
            [convex-web.consumer :as consumer]

            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]

            [aero.core :as aero]
            [com.brunobonacci.mulog :as u]
            [datascript.core :as d]
            [com.stuartsierra.component :as component]
            [taoensso.sente :as sente])
  (:import (convex.peer Server API)
           (convex.net Connection ResultConsumer)))

(defrecord Config [profile config]
  component/Lifecycle

  (start [component]
    (let [config (aero/read-config "convex-web.edn" {:profile profile})]
      (println (str "CONFIG " profile "\n" (with-out-str (pprint/pprint config))))

      (assoc component :config config)))

  (stop [component]
    (assoc component :config nil)))

(defrecord MuLog [config stop]
  component/Lifecycle

  (start [component]
    (let [publisher-config (get-in config [:config :logging :publisher-config])
          stop (u/start-publisher! publisher-config)]
      (assoc component :stop stop)))

  (stop [component]
    (when-let [stop (:stop component)]
      (stop))

    (assoc component :stop nil)))

(defrecord DataScript [conn]
  component/Lifecycle

  (start [component]
    (let [schema {;; -- Command

                  :convex-web.command/id
                  {:db/unique :db.unique/identity
                   :db/index true}


                  ;; -- Account

                  :convex-web.account/address
                  {:db/unique :db.unique/identity
                   :db/index true}

                  :convex-web.account/faucets
                  {:db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/many}


                  ;; -- Session

                  :convex-web.session/id
                  {:db/unique :db.unique/identity
                   :db/index true}

                  :convex-web.session/accounts
                  {:db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/many}}
          conn (d/create-conn schema)]
      (log/debug "Started DataScript")

      (assoc component
        :conn conn)))

  (stop [component]
    (log/debug "Stopped DataScript")

    (assoc component
      :conn nil)))

(defrecord Consumer [datascript consumer]
  component/Lifecycle

  (start [component]
    (let [consumer (consumer/persistent-consumer (system/-datascript-conn datascript))]
      (log/debug "Started Consumer")

      (assoc component
        :consumer consumer)))

  (stop [component]
    (log/debug "Stopped Consumer")

    (assoc component
      :consumer nil)))

(defrecord Convex [server conn consumer]
  component/Lifecycle

  (start [component]
    (let [^Server server (API/launchPeer)
          ^ResultConsumer consumer (system/-consumer-consumer consumer)
          ^Connection connection (peer/conn server consumer)]
      (log/debug "Started Convex")

      (assoc component
        :server server
        :conn connection)))

  (stop [component]
    (when-let [^Connection conn (:conn component)]
      (.close conn))

    (when-let [^Server server (:server component)]
      (.close server))

    (log/debug "Stopped Convex")

    (assoc component
      :conn nil
      :server nil
      :consumer nil)))

(defrecord WebServer [config convex datascript stop-fn]
  component/Lifecycle

  (start [component]
    (let [context {:convex convex
                   :datascript datascript}

          port (get-in config [:config :web-server :port])

          stop-fn (web-server/run-server context {:port port})]
      (log/debug (str "Started WebServer on port " port))

      (assoc component
        :port port
        :stop-fn stop-fn)))

  (stop [component]
    (when-let [stop (:stop-fn component)]
      (stop))

    (log/debug "Stopped WebServer")

    (assoc component
      :datascript nil
      :convex nil
      :port nil
      :stop-fn nil)))

(defrecord WebSocketRouter [convex stop-router-fn datascript]
  component/Lifecycle

  (start [component]
    (let [context {:convex convex
                   :datascript datascript}

          web-socket-handler (partial comms/web-socket-handler context)

          stop-router-fn (sente/start-server-chsk-router! comms/ch-recv web-socket-handler)]
      (log/debug "Started WebSocketRouter")

      (assoc component :stop-router-fn stop-router-fn)))

  (stop [component]
    (when-let [stop-router (:stop-router-fn component)]
      (stop-router))

    (log/debug "Stopped WebSocketRouter")

    (assoc component
      :convex nil
      :stop-router-fn nil
      :datascript nil)))

(defn system
  "System Component."
  [profile]
  (component/system-map
    :config
    (map->Config {:profile profile})

    :mulog
    (component/using
      (map->MuLog {}) [:config])

    :datascript
    (map->DataScript {})

    :consumer
    (component/using
      (map->Consumer {}) [:datascript])

    :convex
    (component/using
      (map->Convex {}) [:consumer])

    :web-server
    (component/using
      (map->WebServer {}) [:config :convex :datascript])))