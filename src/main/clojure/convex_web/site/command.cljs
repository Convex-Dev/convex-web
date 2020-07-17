(ns convex-web.site.command
  (:require [convex-web.site.backend :as backend]
            [convex-web.site.runtime :as runtime :refer [disp]]

            [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]))

(def default-command-timeout 100)
(def default-command-retries 10)

(re-frame/reg-fx :command.fx/poll
  (fn [{:keys [id timeout retries watch command]}]
    (if (pos-int? retries)
      (runtime/set-timeout
        (fn []
          (backend/GET-command id {:handler
                                   (fn [{:convex-web.command/keys [status] :as command'}]
                                     (let [completed? (#{:convex-web.command.status/success
                                                         :convex-web.command.status/error}
                                                       status)]

                                       (watch command command')

                                       ;; The command took longer than `timeout` to complete,
                                       ;; that's fine, but we need to try again - and increase the timeout.
                                       (when-not completed?
                                         (disp :command/!poll {:id id
                                                               :timeout (* timeout 1.3)
                                                               :retries (dec retries)
                                                               :command (merge command command')
                                                               :watch watch}))))
                                   :error-handler
                                   (fn [error]
                                     (log/error :command.fx/poll {:error error})

                                     (watch command #:convex-web.command {:status :convex-web.command.status/error
                                                                          :error error}))}))
        timeout)
      (watch command #:convex-web.command {:status :convex-web.command.status/error
                                           :error "Exhausted retries."}))))

(re-frame/reg-event-fx :command/!poll
  (fn [_ [_ m]]
    {:command.fx/poll m}))

(re-frame/reg-fx :command.fx/execute
  (fn [{:keys [command watch]}]
    (backend/POST-command command {:handler
                                   (fn [command']
                                     (watch command command')

                                     ;; Makes another trip to the server to get the command.
                                     ;; It's very likely that by the time we reach the server
                                     ;; the command is ready - either succeeded or failed.
                                     ;;
                                     ;; We can also adjust the timeout knob of the request;
                                     ;; increase the timeout and increase the chance of
                                     ;; doing a single round-trip to the server.
                                     (disp :command/!poll {:id (:convex-web.command/id command')
                                                           :timeout default-command-timeout
                                                           :retries default-command-retries
                                                           :command (merge command command')
                                                           :watch watch}))

                                   :error-handler
                                   (fn [error]
                                     (log/error :command.fx/execute {:error error})

                                     (watch command #:convex-web.command {:status :convex-web.command.status/error
                                                                          :error error}))})))

(re-frame/reg-event-fx :command/!execute
  (fn [_ [_ command watch]]
    {:command.fx/execute {:command command
                          :watch watch}}))

(defn execute [command & [watch]]
  (disp :command/!execute command (or watch identity)))


