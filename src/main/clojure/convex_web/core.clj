(ns convex-web.core
  (:require [convex-web.component]
            [com.stuartsierra.component]))

(defn -main
  "Start Convex Web."
  [& _]
  (com.stuartsierra.component/start (convex-web.component/system :prod)))
