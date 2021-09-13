(ns convex-web.site.session
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.stack :as stack]

            [clojure.string :as str]

            [re-frame.core :as re-frame]
            [convex-web.site.db :as db]
            [convex-web.site.backend :as backend]))

(re-frame/reg-sub :session/?session
  (fn [db _]
    (:site/session db)))

(re-frame/reg-sub :session/?id
  :<- [:session/?session]
  (fn [{:convex-web.session/keys [id]} _]
    (or id "-")))

(re-frame/reg-sub :session/?state
  :<- [:session/?session]
  (fn [{:convex-web.session/keys [state]} _]
    state))

(re-frame/reg-event-db :session/!set-state
  (fn [db [_ f args]]
    (update-in db [:site/session :convex-web.session/state] (fn [state]
                                                              (apply f state args)))))

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

(re-frame/reg-sub :session/?status
  (fn [db _]
    (get-in db [:site/session :ajax/status])))

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

(re-frame/reg-event-fx :session/!env
  (fn [_ [_ {:keys [address sym]}]]
    {:fx [[:dispatch
           [:session/!set-state
            (fn [state]
              (assoc-in state [address :env sym] {:ajax/status :ajax.status/pending}))]]

          [:runtime.fx/do
           (fn []
             (backend/POST-env
               {:params {:address address
                         :sym sym}

                :handler
                (fn [value]
                  (disp :session/!set-state
                    (fn [state]
                      (assoc-in state [address :env sym] {:ajax/status :ajax.status/success
                                                          :value value}))))

                :error-handler
                (fn [error]
                  (disp :session/!set-state
                    (fn [state]
                      (assoc-in state [address :env sym] {:ajax/status :ajax.status/error
                                                          :error error}))))}))]]}))

(defn ?status []
  (sub :session/?status))

(defn ?active-address []
  (sub :session/?active-address))

(defn initialize []
  (db/transact! assoc-in [:site/session :ajax/status] :ajax.status/pending)

  (backend/GET-session
    {:handler
     (fn [session]
       (db/transact! update :site/session merge {:ajax/status :ajax.status/success} session))

     :error-handler
     (fn [error]
       (db/transact! update :site/session merge {:ajax/status :ajax.status/error
                                                 :ajax/error error}))}))

(defn add-account [account & [active?]]
  (disp :session/!add-account account {:active? active?}))

(defn ?session []
  (sub :session/?session))

(defn ?id []
  (sub :session/?id))

(defn ?state []
  (sub :session/?state))

(defn set-state [f & args]
  (disp :session/!set-state f args))

(defn ?accounts []
  (sub :session/?accounts))

(defn SessionPage [_ {:keys [convex-web.session/id]} set-state]
  [:div.flex.flex-1.justify-center.my-4.mx-10

   [:div.flex.flex-col.flex-1.space-y-10.max-w-screen-sm

    [:div.flex.flex-col.items-center
     [:span.text-base.text-gray-500
      "Current Wallet Key"]

     [:div.flex.items-center
      [:code.text-sm.mr-2 (?id)]
      [gui/ClipboardCopy (?id)]]]


    [:p
     "This is your current Wallet Key. Your Wallet gives you control over a
      group of Accounts on convex.world, which you can easily switch between."]


    [:p
     "You can copy the Walley Key if you want to access the same Wallet from
      another device, or come back later to the same Wallet. Wallets will be
      periodically refreshed as we develop the convex.world testnet. But don't
      worry, you can always create another for free!"]


    [:p
     "Enter a valid Wallet Key below to switch to a different Wallet."]


    [:input
     {:class gui/input-style
      :type "text"
      :value id
      :placeholder "Wallet Key"
      :on-change
      #(let [value (gui/event-target-value %)]
         (set-state assoc :convex-web.session/id value))}]

    [:div.flex.justify-center.mt-6
     [gui/PrimaryButton
      {:on-click #(stack/pop)}
      [gui/ButtonText
       {}
       "Cancel"]]

     [:div.mx-2]

     [gui/SecondaryButton
      {:disabled (str/blank? id)
       :on-click
       #(do
          (set! (.-cookie js/document) (str "ring-session=" id " ; path=/"))
          (.reload (.-location js/document)))}
      [gui/ButtonText
       {}
       "Restore"]]]]])

(def session-page
  #:page {:id :page.id/session
          :component #'SessionPage})
