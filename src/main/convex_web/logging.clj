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

(defn http-request [{:keys [uri request-method status remote-addr]}]
  (let [builder (doto (HttpRequest/newBuilder)
                  (.setRemoteIp remote-addr)
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

(defn default-labels [item]
  (merge {"eventName" (str (:mulog/event-name item))
          "namespace" (:mulog/namespace item)}

         ;; Session is added by a Ring middleware using u/log `with-context`.
         ;; (But it might be nil and nil is not allowed.)
         (when-let [session (get-in item [:context :ring-session])]
           {"session" session})))


;; -- Labels can be defined by event.

(defmulti labels :mulog/event-name)

(defmethod labels :default [item]
  (default-labels item))


;; -- Payload can also be defined by event.

(defn default-payload-data [{:keys [message exception]}]
  (merge {}
         (when message
           {"message" message})
         (when exception
           {"exception_stack_trace" (with-out-str (.printStackTrace exception))})))

(defmulti json-payload :mulog/event-name)

(defmethod json-payload :default [item]
  (Payload$JsonPayload/of (default-payload-data item)))

(defmethod json-payload :logging.event/endpoint [item]
  (Payload$JsonPayload/of {"headers" (java.util.HashMap. (get-in item [:context :request :headers]))}))

(defmethod json-payload :logging.event/confirm-account [{:keys [address] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item) (when address
                                                               {"address" address}))))

(defmethod json-payload :logging.event/faucet [{:keys [target amount] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when target
                                   {"target" target})
                                 (when amount
                                   {"amount" amount}))))

(defmethod json-payload :logging.event/repl-user [{:keys [address source] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when address
                                   {"address" address})
                                 (when source
                                   {"source" source}))))

(defmethod json-payload :logging.event/repl-error [{:keys [address source] :as item}]
  (Payload$JsonPayload/of (merge (default-payload-data item)
                                 (when address
                                   {"address" address})
                                 (when source
                                   {"source" source}))))


(defn mulog-item->log-entry [item]
  (try
    (log-entry {:resource "gce_instance"
                :labels (labels item)
                :payload (json-payload item)
                :http-request (http-request (:context item))})
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message (str "Failed to create a LogEntry from a u/log item. " (ex-message ex)))

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

