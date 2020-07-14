(ns convex-web.site.comms
  (:require [convex-web.site.runtime :refer [disp]]
            [taoensso.sente :as sente]
            [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]))

(let [token (when-let [el (.getElementById js/document "__anti-forgery-token")]
              (.-value el))

      {:keys [chsk
              ch-recv
              send-fn
              state]}
      (sente/make-channel-socket-client! "/chsk" token {:type :auto
                                                        :packer :edn})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def send send-fn)
  (def state state))

(def sente-router-ref (atom (fn [] nil)))

(add-watch state :sente (fn [_ _ _ state]
                          (disp :comms/!state state)))

(defn handler [{:keys [id ?data]}]
  (try
    (let [[event data] ?data]
      (cond
        (= :site/dispatch event)
        (let [[id args] data]
          (disp id args))

        (= :convex-web.comms/!blocks event)
        (disp :convex/!blocks data)

        :else
        (log/info id ?data)))

    (catch js/Error error
      (log/error id {:error error}))))

(defn start []
  (reset! sente-router-ref (sente/start-client-chsk-router! ch-chsk handler)))

(defn stop []
  (when-let [f @sente-router-ref]
    (f)))

(re-frame/reg-event-db :comms/!state
  (fn [db [_ state]]
    (assoc-in db [:site/comms :comms/state] state)))

(re-frame/reg-sub :comms/?state
  (fn [db _]
    (get-in db [:site/comms :comms/state])))

