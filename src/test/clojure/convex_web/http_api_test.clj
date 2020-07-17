(ns convex-web.http-api-test
  (:require [convex-web.component]
            [convex-web.transit :as transit]

            [clojure.test :refer :all]

            [com.stuartsierra.component]
            [org.httpkit.client :as http])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))

(def system nil)

(use-fixtures :once (fn [f]
                      (SLF4JBridgeHandler/removeHandlersForRootLogger)
                      (SLF4JBridgeHandler/install)

                      (let [system (com.stuartsierra.component/start
                                     (convex-web.component/system :test))]

                        (alter-var-root #'system (constantly system))

                        (f)

                        (com.stuartsierra.component/stop system))))

(deftest GET-blocks-range-test
  (let [port (get-in system [:config :config :web-server :port])
        url (str "http://localhost:" port "/api/internal/blocks-range")

        {:keys [status body]} @(http/get url)]
    (is (= 200 status))

    (is (= {:convex-web/blocks []
            :meta {:end 0
                   :start 0
                   :total 0}}
           (transit/decode-string body)))))