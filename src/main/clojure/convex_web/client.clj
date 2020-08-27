(ns convex-web.client
  (:require [convex-web.component]

            [clojure.test :refer :all]
            [clojure.data.json :as json]

            [org.httpkit.client :as http])
  (:import (convex.core.crypto Hash AKeyPair)))

(defn sig [^AKeyPair key-pair ^String hash]
  (.toHexString (.sign key-pair (Hash/fromHex hash))))

(defn POST-public-v1-query [server-url {:keys [address source] :as body}]
  (let [url (str server-url "/api/v1/query")
        body (json/write-str body)]
    (http/post url {:body body})))

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

(defn POST-v1-faucet [server-url {:keys [address amount] :as body}]
  (let [url (str server-url "/api/v1/faucet")
        body (json/write-str body)]
    (http/post url {:body body})))


