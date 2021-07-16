(ns convex-web.store
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (etch EtchStore)
           (convex.core.store Stores)))

(def store nil)

(defn ^EtchStore create! [{:keys [temp?]}]
  (or store 
    (let [^EtchStore store (if temp?
                             (EtchStore/createTemp "convex-db")
                             (EtchStore/create (io/file (get (System/getProperties) "user.dir") "convex-db")))]
      
      (Stores/setGlobalStore store)
      
      (alter-var-root #'store (constantly store))
      
      (log/debug "Store is ready" store)
      
      store)))
