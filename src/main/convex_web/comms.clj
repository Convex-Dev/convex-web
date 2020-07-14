(ns convex-web.comms
  (:require [clojure.tools.logging :as log]

            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :as sente.server-adapters.http-kit]))

(def adapter
  (sente.server-adapters.http-kit/get-sch-adapter))

(def socket-server
  (sente/make-channel-socket-server! adapter {:packer :edn}))

(def ch-recv
  "core.async channel to receive `event-msg`s (internal or from clients).*

   *Copied from Sente's documentation."
  (:ch-recv socket-server))

(def send-fn
  "(fn [user-id ev] ...) for server>user push.*

   *Copied from Sente's documentation."
  (:send-fn socket-server))

(def connected-uids
  "Watchable, read-only (atom {:ws #{_} :ajax #{_} :any #{_}}).*

   *Copied from Sente's documentation."
  (:connected-uids socket-server))

(def ajax-post-fn
  "(fn [ring-req]) for Ring CSRF-POST + chsk URL.*

   *Copied from Sente's documentation."
  (:ajax-post-fn socket-server))

(def ajax-get-or-ws-handshake-fn
  "(fn [ring-req]) for Ring GET + chsk URL.

   *Copied from Sente's documentation."
  (:ajax-get-or-ws-handshake-fn socket-server))

(add-watch connected-uids :web-socket-uids (fn [_ _ _ uids]
                                             (log/debug "Connected UIDs " uids)))

(defn uids
  "Connected UIDs."
  []
  (:any @connected-uids))


(defmulti handler
  (fn [context {:keys [id]}]
    id))

(defmethod handler :default [_ _]
  nil)

(defn web-socket-handler [context {:keys [id event uid ?reply-fn] :as message}]
  (try
    (let [[_ body] event]
      (log/debug (str id " " uid " " body)))

    (handler context message)

    (catch Exception ex
      (log/error ex "Server error."))))