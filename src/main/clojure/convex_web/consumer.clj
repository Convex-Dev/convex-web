(ns convex-web.consumer
  (:require [convex-web.convex :as convex]
            [convex-web.command :as command]

            [clojure.tools.logging :as log]

            [datalevin.core :as d]
            [com.brunobonacci.mulog :as u])
  (:import (convex.net ResultConsumer)))

(defn ^ResultConsumer result-consumer [{:keys [handle-result handle-error]}]
  (proxy [ResultConsumer] []
    (handleResult [id object]
      (handle-result id object))

    (handleError [id code message]
      (handle-error id code message))))

(defn ^ResultConsumer command-consumer [db-conn]
  (result-consumer
    {:handle-result
     (fn [^Long id object]
       (try
         (log/info (str "Consumer result " id ":") object)

         ;; TODO Change design.
         #_(let [{:convex-web.command/keys [mode address] :as c} (command/find-by-id @db-conn id)]
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

         (locking db-conn
           (let [c (command/find-by-id @db-conn id)

                 _ (log/debug (str "Find Command " id ":") c)

                 c (merge c {:convex-web.command/status :convex-web.command.status/success} (when (some? object)
                                                                                              {:convex-web.command/object object}))

                 c (-> c
                       (command/wrap-result-metadata)
                       (command/wrap-result))]

             (d/transact! db-conn [c])))
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
         #_(let [{:convex-web.command/keys [mode address] :as c} (command/find-by-id @db-conn id)]
             (u/log :logging.event/repl-error
                    :severity :info
                    :address address
                    :mode mode
                    :source (command/source c)
                    :message (str message)))

         (d/transact! db-conn [#:convex-web.command{:id id
                                                    :status :convex-web.command.status/error
                                                    :error
                                                    {:code (convex/datafy code)
                                                     :message (convex/datafy message)}}])
         (catch Exception ex
           (u/log :logging.event/system-error
                  :severity :error
                  :message (str "Consumer failed to transact object " id " error result: " message)
                  :exception ex))))}))
