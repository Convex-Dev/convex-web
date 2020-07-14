(ns convex-web.site.session
  (:require [convex-web.site.runtime :refer [disp sub]]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub :session/?session
  (fn [{:site/keys [session]} _]
    session))

(re-frame/reg-sub :session/?accounts
  :<- [:session/?session]
  (fn [{:convex-web.session/keys [accounts]} _]
    accounts))

(re-frame/reg-sub :session/?default-address
  :<- [:session/?accounts]
  (fn [[{:convex-web.account/keys [address]}] _]
    address))

(re-frame/reg-sub :session/?selected-address
  :<- [:session/?session]
  (fn [{:convex-web.session/keys [selected-address]} _]
    selected-address))

(re-frame/reg-sub :session/?active-address
  :<- [:session/?default-address]
  :<- [:session/?selected-address]
  (fn [[default-address selected-address] _]
    (or selected-address default-address)))


(re-frame/reg-event-db :session/!create
  (fn [db [_ session]]
    (assoc db :site/session session)))

(re-frame/reg-event-db :session/!pick-address
  (fn [db [_ address]]
    (assoc-in db [:site/session :convex-web.session/selected-address] address)))

(defn pick-address [address]
  (disp :session/!pick-address address))

(re-frame/reg-event-fx :session/!add-account
  (fn [{:keys [db]} [_ account {:keys [active?]}]]
    (merge {:db (update-in db [:site/session :convex-web.session/accounts] conj account)}
           (when active?
             {:dispatch [:session/!pick-address (get account :convex-web.account/address)]}))))


(defn ?active-address []
  (sub :session/?active-address))

(defn create [session]
  (disp :session/!create session))

(defn add-account [account & [active?]]
  (disp :session/!add-account account {:active? active?}))

(defn ?session []
  (sub :session/?session))

(defn ?accounts []
  (let [{:convex-web.session/keys [accounts]} (?session)]
    accounts))

