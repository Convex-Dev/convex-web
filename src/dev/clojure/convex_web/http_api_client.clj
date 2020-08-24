(ns convex-web.http-api-client
  (:require [convex-web.component]

            [clojure.test :refer :all]
            [clojure.data.json :as json]

            [org.httpkit.client :as http]))

(defn public-v1-transaction-prepare [server-url {:keys [address source] :as body}]
  (let [prepare-url (str server-url "/api/v1/transaction/prepare")
        prepare-body (json/write-str body)]
    (http/post prepare-url {:body prepare-body})))


