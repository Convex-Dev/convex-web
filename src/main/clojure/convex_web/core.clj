(ns convex-web.core
  (:require [convex-web.component]
            [com.stuartsierra.component]
            [clojure.tools.logging :as log])
  
  (:import 
   (convex.core.util Shutdown)))

(def system nil)

(defn -main
  "Start Convex Web."
  [& _]
  (let [_ (log/info "Starting system...")
        
        system (com.stuartsierra.component/start (convex-web.component/system :prod))
        
        shutdown-hook (fn []
                        (try
                          (log/info "Stopping system...")
                          
                          (com.stuartsierra.component/stop system)
                          
                          (catch Exception ex
                            (log/error ex "There was an error while stopping system in shutdown hook."))))]
    
    (alter-var-root #'convex-web.core/system (constantly system))
    
    (Shutdown/addHook Shutdown/SERVER ^Runnable shutdown-hook)
    
    (Thread/setDefaultUncaughtExceptionHandler
      (reify
        Thread$UncaughtExceptionHandler
        (uncaughtException [_ thread throwable]
          (log/error throwable (format "Uncaught exception %s on thread %s" throwable thread)))))))
