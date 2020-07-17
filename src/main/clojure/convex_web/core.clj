(ns convex-web.core
  (:require [convex-web.component]
            [com.stuartsierra.component])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))

(defn -main
  "Start Convex Web."
  [& _]
  (SLF4JBridgeHandler/removeHandlersForRootLogger)
  (SLF4JBridgeHandler/install)

  (com.stuartsierra.component/start (convex-web.component/system :prod)))
