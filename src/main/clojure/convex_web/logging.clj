(ns convex-web.logging
  (:require [clojure.java.io :as io]
            [com.brunobonacci.mulog.buffer :as rb]
            [com.brunobonacci.mulog :as u])
  (:import (com.google.auth.oauth2 GoogleCredentials)
           (com.google.cloud.logging LoggingOptions Severity LogEntry Payload$StringPayload Logging$WriteOption Logging HttpRequest$Builder HttpRequest$RequestMethod HttpRequest Payload$JsonPayload)
           (com.google.cloud MonitoredResource)
           (com.brunobonacci.mulog.publisher PPublisher)
           (java.io Closeable)))

(def logging-severity
  {:default Severity/DEFAULT
   :debug Severity/DEBUG
   :info Severity/INFO
   :notice Severity/NOTICE
   :warning Severity/WARNING
   :error Severity/ERROR})

(def logging-http-method
  {:get HttpRequest$RequestMethod/GET
   :post HttpRequest$RequestMethod/POST
   :put HttpRequest$RequestMethod/PUT
   :head HttpRequest$RequestMethod/HEAD})

(defn logging-http-request [{:logging.mdc/keys [http-request http-response]}]
  (let [{:keys [uri request-method remote-addr]} http-request
        {:keys [status]} http-response

        x-forwarded-for (get-in http-request [:headers "x-forwarded-for"])

        builder (doto (HttpRequest/newBuilder)
                  (.setRemoteIp (or x-forwarded-for remote-addr))
                  (.setRequestUrl uri)
                  (.setRequestMethod (logging-http-method request-method)))

        _ (when status
            (.setStatus builder status))]
    (.build builder)))

(defn log-entry [{:keys [severity resource payload labels http-request]}]
  (let [severity (or (logging-severity severity) Severity/DEFAULT)

        log-entry-builder (doto (LogEntry/newBuilder payload)
                            (.setSeverity severity)
                            (.setLabels (or labels {}))
                            (.setResource (.build (MonitoredResource/newBuilder (or resource "global")))))
        _ (when http-request
            (.setHttpRequest log-entry-builder http-request))]

    (.build log-entry-builder)))

(defn ^Logging cloud-logging []
  (with-open [stream (io/input-stream (io/resource "logging_service_account.json"))]
    (let [credentials (doto (GoogleCredentials/fromStream stream)
                        (.createScoped (into-array ["https://www.googleapis.com/auth/cloud-platform"])))

          logging-options (doto (LoggingOptions/newBuilder)
                            (.setCredentials credentials))]
      (-> logging-options
          (.build)
          (.getService)))))

(defn cloud-logging-write [^Logging logging entries & [{:keys [log-name]}]]
  (when (seq entries)
    (.write logging entries (into-array [(Logging$WriteOption/logName (or log-name "convex_web"))]))))

(defn default-labels [{:keys [mulog/event-name
                              mulog/namespace
                              logging.mdc/http-request]}]
  (merge {"eventName" (str event-name)
          "namespace" namespace}

         ;; Session is added by a Ring middleware using u/log `with-context`.
         ;; (But it might be nil and nil is not allowed.)
         (when-let [session (get-in http-request [:cookies "ring-session" :value])]
           {"session" session})))


;; -- Labels can be defined by event.

(defmulti logging-labels :mulog/event-name)

(defmethod logging-labels :default [item]
  (default-labels item))


;; -- Payload can also be defined by event.

(defn default-payload-data [{:keys [message exception]}]
  (merge {}
         (when message
           {"message" message})
         (when exception
           {"exception" (str exception)})))

(defmulti logging-json-payload :mulog/event-name)

(defmethod logging-json-payload :default [item]
  (Payload$JsonPayload/of (default-payload-data item)))

(defmethod logging-json-payload :logging.event/endpoint [item]
  (Payload$JsonPayload/of {"headers" (java.util.HashMap. (get-in item [:logging.mdc/http-request :headers]))}))

(defmethod logging-json-payload :logging.event/confirm-account [{:keys [address] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item) (when address
                                                               {"address" address}))))

(defmethod logging-json-payload :logging.event/faucet [{:keys [target amount] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when target
                                   {"target" target})
                                 (when amount
                                   {"amount" amount}))))

(defmethod logging-json-payload :logging.event/repl-user [{:keys [address mode source] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when address
                                   {"address" address})
                                 (when mode
                                   {"mode" (name mode)})
                                 (when source
                                   {"source" source}))))

(defmethod logging-json-payload :logging.event/repl-error [{:keys [address mode source] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when address
                                   {"address" address})
                                 (when mode
                                   {"mode" (name mode)})
                                 (when source
                                   {"source" source}))))


(defn mulog-item->log-entry [item]
  (try
    (log-entry (merge {:resource "gce_instance"
                       :labels (logging-labels item)
                       :payload (logging-json-payload item)
                       :http-request (logging-http-request item)}
                      (when-let [severity (get item :severity)]
                        {:severity severity})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message "Failed to create a LogEntry from a u/log item."
             :exception ex)

      nil)))

(deftype GoogleCloudLoggingPublisher
  [buffer logging]
  PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    500)

  (publish [_ buffer]
    (try
      ;;(println "u/log Publish" buffer)

      (let [entries (->> (rb/items buffer)
                         (map
                           (fn [[_ item]]
                             (mulog-item->log-entry item)))
                         (remove nil?))]
        (cloud-logging-write logging entries {:log-name "events"})

        (rb/clear buffer))
      (catch Exception ex
        ;; u/log will handle the error,
        ;; but it's printed so you know what happened.
        (.printStackTrace ex)

        (throw ex))))

  Closeable
  (close [_]
    (.flush logging)
    (.close logging)))

(defn google-cloud-logging-publisher [& _]
  (let [buffer (rb/agent-buffer 10000)
        logging (cloud-logging)]
    (GoogleCloudLoggingPublisher. buffer logging)))

(deftype DevLoggingPublisher
  [buffer]
  PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)

  (publish [_ buffer]
    (doseq [{:keys [message exception]} (->> (rb/items buffer)
                                             (map second)
                                             (filter :exception))]
      (.printStackTrace (ex-info message {} exception)))

    (rb/clear buffer)))

(defn dev-logging-publisher [& _]
  (DevLoggingPublisher. (rb/agent-buffer 10000)))

