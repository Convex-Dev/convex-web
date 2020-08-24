(ns convex-web.http-api-client
  (:require [convex-web.component]

            [clojure.test :refer :all]
            [clojure.data.json :as json]

            [org.httpkit.client :as http])
  (:import (convex.core.crypto Hash)
           (convex.core Init)))

(defn sig [hash]
  (.toHexString (.sign Init/HERO_KP (Hash/fromHex hash))))

(defn POST-public-v1-transaction-prepare [server-url {:keys [address source] :as body}]
  (let [url (str server-url "/api/v1/transaction/prepare")
        body (json/write-str body)]
    (http/post url {:body body})))

(defn POST-public-v1-transaction-prepare' [server-url {:keys [address source] :as body}]
  (let [response @(POST-public-v1-transaction-prepare server-url body)]
    (json/read-str (get response :body) :key-fn keyword)))

(defn POST-public-v1-transaction-submit [server-url {:keys [address hash sig] :as body}]
  (let [url (str server-url "/api/v1/transaction/submit")
        body (json/write-str body)]
    (http/post url {:body body})))

(defn POST-public-v1-transaction-submit' [server-url {:keys [address hash sig] :as body}]
  (let [response @(POST-public-v1-transaction-submit server-url body)]
    (json/read-str (get response :body) :key-fn keyword)))


