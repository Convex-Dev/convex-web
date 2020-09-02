(ns convex-web.consumer
  (:require [convex-web.convex :as convex]

            [datascript.core :as d]
            [com.brunobonacci.mulog :as u]
            [clojure.tools.logging :as log])
  (:import (convex.net ResultConsumer)))

(defn ^ResultConsumer result-consumer [{:keys [handle-result handle-error]}]
  (proxy [ResultConsumer] []
    (handleResult [id object]
      (handle-result id object))

    (handleError [id code message]
      (handle-error id code message))))

(defn ^ResultConsumer command-consumer [datascript-conn]
  (result-consumer
    {:handle-result
     (fn [^Long id object]
       (try
         (log/error "Consumer result" id object)

         ;; TODO Change design.
         #_(let [{:convex-web.command/keys [mode address] :as c} (command/query-by-id @datascript-conn id)]
             (try
               (u/log :logging.event/repl-user
                      :severity :info
                      :address address
                      :mode mode
                      :source (command/source c))

               (catch Exception ex
                 (u/log :logging.event/system-error
                        :severity :error
                        :message (str "Consumer received an invalid Command: " c)
                        :exception ex))))

         (d/transact! datascript-conn [(merge {:convex-web.command/id id
                                               :convex-web.command/status :convex-web.command.status/success}
                                              (when (some? object)
                                                {:convex-web.command/object object}))])
         (catch Exception ex
           (u/log :logging.event/system-error
                  :severity :error
                  :message (str "Consumer failed to transact object " id " successful result: " object)
                  :exception ex))))

     :handle-error
     (fn [^Long id ^Object code ^Object message]
       (try
         (log/error "Consumer error" code message)

         ;; TODO Change design. (Same issue as above)
         #_(let [{:convex-web.command/keys [mode address] :as c} (command/query-by-id @datascript-conn id)]
             (u/log :logging.event/repl-error
                    :severity :info
                    :address address
                    :mode mode
                    :source (command/source c)
                    :message (str message)))

         (d/transact! datascript-conn [#:convex-web.command{:id id
                                                            :status :convex-web.command.status/error
                                                            :error
                                                            {:code (convex/datafy code)
                                                             :message (convex/datafy message)}}])
         (catch Exception ex
           (u/log :logging.event/system-error
                  :severity :error
                  :message (str "Consumer failed to transact object " id " error result: " message)
                  :exception ex))))}))
