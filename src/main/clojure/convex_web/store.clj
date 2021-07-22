(ns convex-web.store
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (etch EtchStore)
           (convex.core.store Stores)))

(def store nil)

(defn ^EtchStore create! [{:keys [etch-store-temp?
                                  etch-store-temp-prefix
                                  etch-store] :as config}]
  (try
    (or store 
      (let [^EtchStore store (if etch-store-temp?
                               (EtchStore/createTemp etch-store-temp-prefix)
                               (let [etch-store-file (io/file etch-store)]
                                 ;; Creates the directory named by this abstract pathname, 
                                 ;; including any necessary but nonexistent parent directories.
                                 ;; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/File.html
                                 (when-let [parent (.getParentFile etch-store-file)]
                                   (when-not (.exists parent)
                                     (.mkdirs parent)))
                                 
                                 (EtchStore/create etch-store-file)))]
        
        (Stores/setGlobalStore store)
        
        (alter-var-root #'store (constantly store))
        
        (log/debug "Store is ready" store)
        
        store))
    (catch Exception ex
      (throw (ex-info "Failed to create Etch store." 
               (select-keys config [:etch-store-temp? :etch-store-temp-prefix :etch-store]) ex)))))
