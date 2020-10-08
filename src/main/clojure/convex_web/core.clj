(ns convex-web.core
  (:require [convex-web.component]
            [com.stuartsierra.component]
            [clojure.tools.logging :as log]))

(defn -main
  "Start Convex Web."
  [& _]
  (let [_ (log/info "Starting system...")

        system (com.stuartsierra.component/start (convex-web.component/system :prod))]

    (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable (fn []
                                                                (try
                                                                  (log/info "Stopping system...")

                                                                  (com.stuartsierra.component/stop system)

                                                                  (catch Exception ex
                                                                    (log/error ex "There was an error while stopping system in shutdown hook."))))))))
