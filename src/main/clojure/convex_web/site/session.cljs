(ns convex-web.site.session
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.stack :as stack]

            [clojure.string :as str]

            [re-frame.core :as re-frame]))

(re-frame/reg-sub :session/?session
  (fn [{:site/keys [session]} _]
    session))

(re-frame/reg-sub :session/?id
  :<- [:session/?session]
  (fn [{:convex-web.session/keys [id]} _]
    (or id "-")))

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


(defn ?id []
  (sub :session/?id))

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

(defn SessionPage [_ {:keys [convex-web.session/id ajax/status]} set-state]
  [:div.flex.flex-1.justify-center.my-4.mx-10
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.justify-center.items-center
      [gui/Spinner]]

     :ajax.status/success
     [:div]

     :ajax.status/error
     [:div]

     [:div.flex.flex-col.flex-1

      [:span.text-xs.text-indigo-500.uppercase "Session"]
      [:div.flex.items-center
       [:code.text-sm.mr-2 (?id)]
       [gui/ClipboardCopy (?id)]]

      [:span.text-xs.text-indigo-500.uppercase.mt-10 "Restore Session"]
      [:input.text-sm.border
       {:style {:height "26px"}
        :type "text"
        :value id
        :on-change
        #(let [value (gui/event-target-value %)]
           (set-state assoc :convex-web.session/id value))}]


      [:div.flex.justify-center.mt-6
       [gui/DefaultButton
        {:on-click #(stack/pop)}
        [:span.text-xs.uppercase "Cancel"]]

       [:div.mx-2]

       [gui/DefaultButton
        {:disabled (str/blank? id)
         :on-click
         #(do
            (set-state assoc :ajax/status :ajax.status/pending)

            (set! (.-cookie js/document) (str "ring-session=" id))
            (.reload (.-location js/document)))}
        [:span.text-xs.uppercase "Restore"]]]])])

(def session-page
  #:page {:id :page.id/session
          :title "Session"
          :component #'SessionPage})
