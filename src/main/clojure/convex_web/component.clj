(ns convex-web.component
  (:require [convex-web.system :as system]
            [convex-web.peer :as peer]
            [convex-web.web-server :as web-server]
            [convex-web.consumer :as consumer]
            [convex-web.db :as db]

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
           (convex.net Connection ResultConsumer)
           (org.slf4j.bridge SLF4JBridgeHandler)
           (convex.core Init)))

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

                    (with-out-str (pprint/pprint config))
                    "\n--------------------------------------------------\n"))

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
              (log/info "Reset database:" dir)

              (doseq [f (reverse (file-seq (io/file dir)))]
                (io/delete-file f true)))

          conn (d/create-conn dir db/schema)]

      (log/debug "Started Datalevin")

      (assoc component
        :conn conn)))

  (stop [component]
    (try
      (d/close conn)
      (catch Exception _
        nil))

    (log/debug "Stopped Datalevin")

    (assoc component
      :conn nil)))

(defrecord Convex [server client]
  component/Lifecycle

  (start [component]
    (let [^Server server (API/launchPeer)
          ^convex.api.Convex client (convex.api.Convex/connect (.getHostAddress server) Init/HERO_KP)]
      (log/debug "Started Convex")

      (assoc component
        :server server
        :client client)))

  (stop [component]
    (when-let [^convex.api.Convex client (:client component)]
      (.disconnect client))

    (when-let [^Server server (:server component)]
      (.close server))

    (log/debug "Stopped Convex")

    (assoc component
      :conn nil
      :server nil
      :consumer nil
      :client nil)))

(defrecord WebServer [config convex datalevin stop-fn]
  component/Lifecycle

  (start [component]
    (let [system {:config config
                  :convex convex
                  :datalevin datalevin}

          port (get-in config [:config :web-server :port])

          stop-fn (web-server/run-server system {:port port})]
      (log/debug (str "Started WebServer on port " port))

      (assoc component
        :port port
        :stop-fn stop-fn)))

  (stop [component]
    (when-let [stop (:stop-fn component)]
      (stop))

    (log/debug "Stopped WebServer")

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
      (map->Convex {}) [])

    :web-server
    (component/using
      (map->WebServer {}) [:config :convex :datalevin])))