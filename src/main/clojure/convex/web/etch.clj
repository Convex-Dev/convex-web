(ns convex.web.etch

  "Preparing Etch instances."

  (:require [clojure.java.io       :as java.io]
            [clojure.tools.logging :as log]
            [convex.db             :as $.db]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn open

  "Creates an Etch instance and sets it for global use."
  
  [{:as   config
    :keys [etch-store
           etch-store-temp-prefix
           etch-store-temp?]}]

  (try
    ;;
    (let [store (if etch-store-temp?
                  ($.db/open-tmp etch-store-temp-prefix)
                  (let [etch-store-file (java.io/file etch-store)]
                    ;; Ensures parent directory exists.
                    (when-let [parent (.getParentFile etch-store-file)]
                      (when-not (.exists parent)
                        (.mkdirs parent)))
                    ($.db/open etch-store-file)))]
      ($.db/global-set store)
      (log/debug "Etch instance ready"
                 store)
      store)
    ;;
    (catch Exception ex
      (throw (ex-info "Failed to create Etch store." 
                      (select-keys config
                                   [:etch-store
                                    :etch-store-temp-prefix
                                    :etch-store-temp?])
                      ex)))))
