(ns convex-web.component
  (:require [convex-web.web-server :as web-server]
            [convex-web.store :as store]
            [convex-web.db :as db]
            [convex-web.convex :as convex]

            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clojure.java.io :as io]

            [aero.core :as aero]
            [com.brunobonacci.mulog :as u]
            [datalevin.core :as d]
            [com.stuartsierra.component :as component])
  (:import (convex.peer Server API)
           (convex.core.crypto AKeyPair)
           (convex.core.data Keywords Address)
           (etch EtchStore)
           (org.slf4j.bridge SLF4JBridgeHandler)
           (java.net InetSocketAddress)))

(defrecord Config [profile config]
  component/Lifecycle

  (start [component]
    (let [config (aero/read-config "convex-web.edn" {:profile profile})]

      (println (str "\n==============\n"
                    (str/upper-case (name profile)) " SYSTEM"
                    "\n==============\n\n"

                    "logback.configurationFile: "
                    (System/getProperty "logback.configurationFile")
                    "\n\n"

                    (with-out-str (pprint/pprint config))))

      (SLF4JBridgeHandler/removeHandlersForRootLogger)
      (SLF4JBridgeHandler/install)

      ;; Spec configuration

      (when-let [spec-config (get config :spec)]
        (require 'convex-web.specs)

        (when (:check-asserts? spec-config)
          (s/check-asserts true))

        (when (:instrument? spec-config)
          (stest/instrument)))

      ;; -----------------------------------------

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

(defrecord Datalevin [config conn]
  component/Lifecycle

  (start [component]
    (let [{:keys [dir reset?]} (get-in config [:config :datalevin])

          _ (when reset?
              (println (str "\n** ATTENTION **\nDatabase " dir " will be deleted!\n"))

              (doseq [f (reverse (file-seq (io/file dir)))]
                (io/delete-file f true)))

          conn (d/create-conn dir db/schema)]

      (assoc component
        :conn conn)))

  (stop [component]
    (try
      (d/close conn)
      (catch Exception _
        nil))

    (assoc component
      :conn nil)))

(defrecord Convex [config server client store]
  component/Lifecycle
  
  (start [component]
    (let [peer-config (get-in config [:config :peer])
          
          {convex-world-peer-url :url
           convex-world-peer-port :port
           convex-world-peer-store-config :store} peer-config
          
          ^EtchStore convex-world-peer-store (store/create! convex-world-peer-store-config)
          
          ;; TODO: Read existing key pair.
          ^AKeyPair convex-world-key-pair (convex/generate-key-pair)
          
          ^Server server (API/launchPeer {Keywords/URL convex-world-peer-url
                                          Keywords/PORT convex-world-peer-port
                                          Keywords/STORE convex-world-peer-store
                                          Keywords/KEYPAIR convex-world-key-pair})
          
          ^InetSocketAddress convex-world-host-address (convex/server-address server)
         
          ^Address convex-world-address (convex/server-peer-controller server)
          
          ^convex.api.Convex client (convex.api.Convex/connect
                                      convex-world-host-address 
                                      convex-world-address 
                                      convex-world-key-pair)]
      
      (assoc component
        :server server
        :client client
        :store convex-world-peer-store)))
  
  (stop [component]
    (when-let [^Server server (:server component)]
      (log/info "Close Server")
      
      (.close server))
    
    (assoc component
      :server nil
      :client nil
      :store nil)))

(defrecord WebServer [config convex datalevin stop-fn]
  component/Lifecycle

  (start [component]
    (let [system {:config config
                  :convex convex
                  :datalevin datalevin}

          port (get-in config [:config :web-server :port])

          stop-fn (web-server/run-server system {:port port})]

      (assoc component
        :port port
        :stop-fn stop-fn)))

  (stop [component]
    (when-let [stop (:stop-fn component)]
      (stop))

    (assoc component
      :datalevin nil
      :convex nil
      :port nil
      :stop-fn nil)))

(defn system
  "System Component."
  [profile]
  (component/system-map
    :config
    (map->Config {:profile profile})

    :mulog
    (component/using
      (map->MuLog {}) [:config])

    :datalevin
    (component/using
      (map->Datalevin {}) [:config])

    :convex
    (component/using
      (map->Convex {}) [:config])

    :web-server
    (component/using
      (map->WebServer {}) [:config :convex :datalevin])))