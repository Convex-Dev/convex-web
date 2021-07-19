(ns convex-web.core
  (:require [convex-web.component]
            [com.stuartsierra.component]
            [clojure.tools.logging :as log]))

(defn -main
  "Start Convex Web."
  [& _]
  (let [system (do
                 (log/info "Starting system...")
                 (com.stuartsierra.component/start (convex-web.component/system :prod)))
        
        shutdown-hook (Thread. ^Runnable (fn []
                                           (try
                                             (log/info "Stopping system...")
                                             
                                             (com.stuartsierra.component/stop system)
                                             
                                             (catch Exception ex
                                               (log/error ex "There was an error while stopping system in shutdown hook.")))))]
    
    (.addShutdownHook (Runtime/getRuntime) shutdown-hook)
    
    (Thread/setDefaultUncaughtExceptionHandler
      (reify
        Thread$UncaughtExceptionHandler
        (uncaughtException [_ thread throwable]
          (log/error throwable (format "Uncaught exception %s on thread %s" throwable thread)))))))
