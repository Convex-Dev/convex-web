(ns convex-web.consumer
  (:require [convex-web.command :as command]

            [datascript.core :as d]
            [com.brunobonacci.mulog :as u])
  (:import (convex.net ResultConsumer Message)))

(defn ^ResultConsumer result-consumer [{:keys [handle-result handle-error]}]
  (proxy [ResultConsumer] []
    (handleResult [id object]
      (handle-result id object))

    (handleError [id message]
      (handle-error id message))))

(defn ^ResultConsumer persistent-consumer [datascript-conn]
  (result-consumer
    {:handle-result
     (fn [^Long id object]
       (try
         (u/log :logging.event/repl-user
                :severity :info
                :source (command/source (command/query-by-id @datascript-conn id)))

         (d/transact! datascript-conn [#:convex-web.command {:id id
                                                             :status :convex-web.command.status/success
                                                             :object (if (nil? object) :nil object)}])
         (catch Exception ex
           (u/log :logging.event/system-error
                  :severity :error
                  :message (str "Failed to transact object " id " successful result." ex)))))

     :handle-error
     (fn [^Long id ^Message message]
       (try
         (u/log :logging.event/repl-error
                :severity :info
                :source (command/source (command/query-by-id @datascript-conn id))
                :message (str message))

         (d/transact! datascript-conn [#:convex-web.command{:id id
                                                            :status :convex-web.command.status/error
                                                            :error message}])
         (catch Exception ex
           (u/log :logging.event/system-error
                  :severity :error
                  :message (str "Failed to transact object " id " error result. " ex)))))}))
