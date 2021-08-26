(ns convex-web.component
  (:require [convex-web.web-server :as web-server]
            [convex-web.store :as store]
            [convex-web.db :as db]
            [convex-web.convex :as convex]
            [convex-web.config :as config]

            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clojure.java.io :as io]

            [aero.core :as aero]
            [expound.alpha :as expound]
            [com.brunobonacci.mulog :as u]
            [datalevin.core :as d]
            [com.stuartsierra.component :as component])
  (:import (convex.peer Server API)
           (convex.core Result)
           (convex.core.crypto AKeyPair)
           (convex.core.data Keywords Address)
           (etch EtchStore)
           (org.slf4j.bridge SLF4JBridgeHandler)
           (java.net InetSocketAddress)))

(defrecord Config [profile config]
  component/Lifecycle
  
  (start [component]
    (let [config (config/read-config profile)]
      
      (when-not (s/valid? :convex-web/config config)
        (let [message (str "config.edn:\n" 
                        (expound/expound-str :convex-web/config config))]
          
          (log/error message)
          
          (throw (ex-info message {:config config}))))
      
      (log/info 
        (str "\n==============\n"
          (str/upper-case (name profile)) " SYSTEM"
          "\n==============\n\n"
          
          "logback.configurationFile: "
          (System/getProperty "logback.configurationFile")
          "\n\n"
          
          ;; ATTENTION
          ;; We can't log secrets.
          (with-out-str (pprint/pprint 
                          (-> config 
                            (dissoc :secrets)
                            (update :peer dissoc :key-store-passphrase :key-passphrase))))))
      
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
              (log/info (str "\n** ATTENTION **\nDatabase " dir " will be deleted!\n"))
              
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
           convex-world-peer-key-store-path :key-store
           convex-world-peer-key-store-passphrase :key-store-passphrase
           convex-world-peer-key-passphrase :key-passphrase} peer-config
          
          ^EtchStore convex-world-peer-store (store/create! peer-config)
          
          convex-world-key-store (convex/key-store
                                   convex-world-peer-key-store-path 
                                   convex-world-peer-key-store-passphrase)
          
          [^AKeyPair convex-world-key-pair restore?] 
          (let [restored-key-pair 
                (reduce
                  (fn [_ alias]
                    (log/debug "Attempt to restore key pair for alias" alias)
                    
                    (let [key-pair (try 
                                     (convex/restore-key-pair
                                       {:key-store convex-world-key-store
                                        :alias alias
                                        :passphrase convex-world-peer-key-passphrase})
                                     (catch Exception ex
                                       (log/error ex "Can't restore key pair for alias" alias)))]
                      (when key-pair
                        (log/debug "Successfully restored key pair for alias" alias)
                        
                        (reduced [key-pair true]))))
                  nil
                  (convex/key-store-aliases convex-world-key-store))]
            
            (or restored-key-pair
              (do 
                (log/error "Can't restore convex.world key pair. It's okay, a new one will be generated.")
                
                (let [generated-key-pair (convex/generate-key-pair)]
                  
                  (convex/save-key-pair 
                    {:key-store convex-world-key-store
                     :key-store-passphrase convex-world-peer-key-store-passphrase
                     :key-store-file convex-world-peer-key-store-path
                     :key-pair generated-key-pair
                     :key-pair-passphrase convex-world-peer-key-passphrase})
                  
                  (log/info "Generated a new key pair for convex.world:" (.toHexString (.getAccountKey generated-key-pair)))
                  
                  [generated-key-pair false]))))
          
          ^Server server (API/launchPeer {Keywords/URL convex-world-peer-url
                                          Keywords/PORT convex-world-peer-port
                                          Keywords/STORE convex-world-peer-store
                                          Keywords/RESTORE restore?
                                          Keywords/KEYPAIR convex-world-key-pair})
          
          ^InetSocketAddress convex-world-host-address (convex/server-address server)
          _ (log/debug "convex-world-host-address" convex-world-host-address)
          
          ^Address convex-world-peer-controller (convex/server-peer-controller server)
          _ (log/debug "convex-world-peer-controller" convex-world-peer-controller)
          
          ^convex.api.Convex client (convex.api.Convex/connect
                                      convex-world-host-address 
                                      convex-world-peer-controller 
                                      convex-world-key-pair)
          
          peer-data {:url (str convex-world-peer-url":" convex-world-peer-port)}
          peer-data-str (pr-str peer-data)
          peer-data-source (str "(set-peer-data " (.getAccountKey convex-world-key-pair) " " peer-data-str ")")
          
          ;; Transaction to set convex.world Peer data on the network.
          ^Result result (convex/transact client 
                           (convex/invoke-transaction 
                             {:nonce 0
                              :address convex-world-peer-controller
                              :command (convex/read-source peer-data-source)}))]
      
      (if (.isError result)
        (log/error "Error setting convex.world Peer data" result)
        (log/info "Successfully set convex.world Peer data" result))
      
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