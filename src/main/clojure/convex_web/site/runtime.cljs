(ns convex-web.site.runtime
  (:require [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]))

(defn disp [id & args]
  (re-frame/dispatch (into [id] args)))

(defn sub [id & args]
  @(re-frame/subscribe (into [id] args)))

(re-frame/reg-fx :runtime.fx/do
  (fn [f]
    (f)))

(re-frame/reg-fx :runtime.fx/with-db
  (fn [[f db]]
    (f db)))

(re-frame/reg-event-fx :runtime/!with-db
  (fn [{:keys [db]} [_ f]]
    {:runtime.fx/with-db [f db]}))

(defn with-db
  "Invoke `f` with db (presumably for side-effects)."
  [f]
  (re-frame/dispatch [:runtime/!with-db f]))

(re-frame/reg-event-db :runtime/!register-interval
  (fn [db [_ id]]
    (update-in db [:site/runtime :runtime/intervals] (fnil conj #{}) id)))

(re-frame/reg-event-db :runtime/!unregister-interval
  (fn [db [_ id]]
    (update-in db [:site/runtime :runtime/intervals] disj id)))

(re-frame/reg-fx :runtime.fx/set-timeout
  (fn [[f delay registrar]]
    (let [id (js/setTimeout f delay)]
      (when registrar
        (registrar id)))))

(re-frame/reg-event-fx :runtime/!set-timeout
  (fn [_ [_ f delay registrar]]
    {:runtime.fx/set-timeout [f delay registrar]}))

(defn set-timeout [f delay & [registrar]]
  (disp :runtime/!set-timeout f delay registrar))

(re-frame/reg-fx :runtime.fx/set-interval
  (fn [[f delay registrar]]
    (let [id (js/setInterval f delay)]
      (log/debug "Set interval" id)

      (disp :runtime/!register-interval id)

      (when registrar
        (registrar id))

      ;; Invoke function once without delay
      (f))))

(re-frame/reg-fx :runtime.fx/clear-interval
  (fn [id]
    (js/clearInterval id)

    (log/debug "Clear interval" id)

    (disp :runtime/!unregister-interval id)))

(re-frame/reg-event-fx :runtime/!set-interval
  (fn [_ [_ f delay registrar]]
    {:runtime.fx/set-interval [f delay registrar]}))

(defn set-interval [f delay & [registrar]]
  (let [interval-ref (atom nil)]
    (disp :runtime/!set-interval f delay (fn [id]
                                           (reset! interval-ref id)

                                           (when registrar
                                             (registrar id))))

    interval-ref))

(re-frame/reg-event-fx :runtime/!clear-interval
  (fn [_ [_ id]]
    {:runtime.fx/clear-interval id}))

(defn clear-interval [id]
  (disp :runtime/!clear-interval id))
