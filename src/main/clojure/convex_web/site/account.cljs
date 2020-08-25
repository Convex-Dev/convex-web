(ns convex-web.site.account
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.backend :as backend]
            [convex-web.site.session :as session]
            [convex-web.site.stack :as stack]
            [convex-web.site.command :as command]
            [convex-web.site.format :as format]
            [convex-web.site.runtime :refer [sub]]

            [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [reitit.frontend.easy :as rfe]
            [reagent.ratom :refer [make-reaction]]
            [re-frame.core :as re-frame]))

(defn balance
  "Account's balance."
  [account]
  (get-in account [:convex-web.account/status :convex-web.account-status/balance]))

;; --

(defn MyAccount [account]
  [:div.flex.flex-col.flex-1.justify-center.items-center
   ;; -- Account details
   [gui/Account account]

   ;; -- Actions
   [:div.flex.mt-6

    ;; -- Faucet
    [gui/DefaultButton
     {:on-click
      #(stack/push :page.id/faucet {:modal? true
                                    :state
                                    {:convex-web/faucet
                                     {:convex-web.faucet/amount 1000000
                                      :convex-web.faucet/target (get account :convex-web.account/address)}}})}
     [:span.text-xs.uppercase "Faucet"]]

    [:div.mx-1]

    ;; -- Transfer
    [gui/DefaultButton
     {:on-click
      #(stack/push :page.id/transfer {:modal? true
                                      :state
                                      {:convex-web/transfer
                                       {:convex-web.transfer/from (get account :convex-web.account/address)}}})}
     [:span.text-xs.uppercase "Transfer"]]]])

(defn MyAccountPage [_ {:keys [ajax/status convex-web/account]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [gui/Spinner]]

    :ajax.status/success
    [:div.flex.flex-col.flex-1.justify-center.my-4.mx-10
     [MyAccount account]]

    :ajax.status/error
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [:span "Sorry. Our servers failed to load your account."]]

    ;; Fallback
    [:div]))

(def my-account-page
  #:page {:id :page.id/my-account
          :title "My Account"
          :component #'MyAccountPage
          :state-spec
          (s/nonconforming
            (s/or :pending :ajax/pending-status
                  :success (s/merge :ajax/success-status (s/keys :req [:convex-web/account]))
                  :error :ajax/error-status))
          :on-push
          (fn [_ state set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (-> (get-in state [:convex-web/account :convex-web.account/address])
                (backend/GET-account
                  {:handler
                   (fn [account]
                     (set-state assoc
                                :ajax/status :ajax.status/success
                                :convex-web/account account))

                   :error-handler
                   (fn [error]
                     (set-state assoc
                                :ajax/status :ajax.status/error
                                :ajax/error error))})))
          :on-resume
          (fn [_ state set-state]
            ;; Don't change `ajax/status` on resume
            ;; to avoid showing the spinner - and don't bother about the error handler.
            (-> (get-in state [:convex-web/account :convex-web.account/address])
                (backend/GET-account {:handler
                                      (fn [account]
                                        (set-state assoc :convex-web/account account))})))})

(defn CreateAccountPage [_ {:keys [convex-web/account ajax/status]} set-state]
  [:div.w-full.flex.flex-col.items-center.justify-center

   (case status
     :ajax.status/pending
     [gui/Spinner]

     :ajax.status/success
     (let [address (get account :convex-web.account/address)]
       [:div.flex.flex-col.items-center
        [:code.text-sm.mb-2 (format/address-blob address)]

        [gui/DefaultButton
         {:on-click
          #(do
             (set-state
               (fn [state]
                 (assoc state :status :pending)))

             (backend/POST-confirm-account address {:handler
                                                    (fn [account]
                                                      (session/add-account account true)
                                                      (stack/pop))}))}
         [:span.text-xs.uppercase "Confirm"]]])

     :ajax.status/error
     [:span "Sorry. Our server failed to create your account. Please try again?"])])

(defn generate-account [_ _ set-state]
  (backend/POST-generate-account
    {:handler
     (fn [account]
       (set-state assoc
                  :ajax/status :ajax.status/success
                  :convex-web/account account))
     :error-handler
     (fn [_]
       (set-state assoc :ajax/status :ajax.status/error))}))

(def create-account-page
  #:page {:id :page.id/create-account
          :title "Create Account"
          :component #'CreateAccountPage
          :initial-state {:ajax/status :ajax.status/pending}
          :state-spec (s/or :pending :ajax/pending-status
                            :success (s/merge :ajax/success-status (s/keys :req [:convex-web/account]))
                            :error :ajax/error-status)
          :on-push #'generate-account})


;; -- Transfer Page

(defn get-transfer-account [{:keys [frame/uuid address state-key]}]
  (stack/set-state uuid assoc state-key {:ajax/status :ajax.status/pending})

  (backend/GET-account address {:handler
                                (fn [account]
                                  (let [f (fn [state]
                                            (assoc state state-key {:convex-web/account account
                                                                    :ajax/status :ajax.status/success}))]
                                    (stack/set-state uuid f)))

                                :error-handler
                                (fn [error]
                                  (let [f (fn [state]
                                            (assoc state state-key {:ajax/error error
                                                                    :ajax/status :ajax.status/error}))]
                                    (stack/set-state uuid f)))}))

(re-frame/reg-sub-raw ::?transfer-from-account
                      (fn [app-db [_ {:keys [frame/uuid address] :as m}]]
                        (get-transfer-account (merge m {:state-key :transfer-page/from}))

                        (make-reaction
                          (fn []
                            (when-let [frame (stack/find-frame @app-db uuid)]
                              (get-in frame [:frame/state :transfer-page/from]))))))

(re-frame/reg-sub-raw ::?transfer-to-account
                      (fn [app-db [_ {:keys [frame/uuid address] :as m}]]
                        (get-transfer-account (merge m {:state-key :transfer-page/to}))

                        (make-reaction
                          (fn []
                            (when-let [frame (stack/find-frame @app-db uuid)]
                              (get-in frame [:frame/state :transfer-page/to]))))))

(defn TransferProgress [{:convex-web/keys [command transfer] :as state} _]
  [:div.flex.flex-col.flex-1.items-center.justify-center
   (case (get command :convex-web.command/status)
     :convex-web.command.status/running
     [gui/Spinner]

     :convex-web.command.status/success
     [:div.flex.flex-col.items-center.text-sm
      [:span.text-lg
       "Success!"]

      ;; -- Transferred x to address y.
      [:span.my-4
       "Transferred "

       [:span.font-bold.text-indigo-500
        (format/format-number (get transfer :convex-web.transfer/amount))]

       " to "

       [:a.flex-1.underline.hover:text-indigo-500
        {:href (rfe/href :route-name/account-explorer {:address (get transfer :convex-web.transfer/to)})}
        [:code.text-xs (get transfer :convex-web.transfer/to)]]

       "."]

      ;; -- Your current balance is z.
      [:span.mb-4
       "Your current balance is "

       [:span.font-bold.text-indigo-500
        (let [from-account (get-in state [:transfer-page/from :convex-web/account])]
          (format/format-number
            (- (balance from-account) (get transfer :convex-web.transfer/amount))))]

       "."]

      [gui/DefaultButton
       {:on-click #(stack/pop)}
       [:span.text-xs.uppercase "Done"]]]

     :convex-web.command.status/error
     [:span.text-sm.text-black
      (if (s/valid? :ajax/error (get command :convex-web.command/error))
        (get-in command [:convex-web.command/error :response :error :message])
        "Sorry. Your transfer couldn't be completed. Please try again?")]

     "...")])

(defn TransferInput [frame {:keys [convex-web/transfer transfer-page/config] :as state} set-state]
  (let [{:convex-web.transfer/keys [from to amount]} transfer

        invalid-transfer? (cond
                            (not (s/valid? :convex-web/transfer transfer))
                            true

                            (= :ajax.status/error (get-in state [:transfer-page/from :ajax/status]))
                            true

                            (= :ajax.status/error (get-in state [:transfer-page/to :ajax/status]))
                            true)

        select-placeholder "Select"

        addresses (cons select-placeholder (map :convex-web.account/address (session/?accounts)))

        Caption (fn [caption]
                  [:span.text-xs.text-indigo-500.uppercase caption])]
    [:div.flex.flex-col.flex-1

     ;; -- From
     [:div.flex.flex-col
      [Caption "From"]
      [gui/Select
       {:value from
        :options addresses
        :on-change #(set-state assoc-in [:convex-web/transfer :convex-web.transfer/from] %)}]

      ;; -- Balance
      (when (s/valid? :convex-web/address from)
        (let [params (merge (select-keys frame [:frame/uuid]) {:address from})

              {:keys [convex-web/account ajax/status ajax/error]} (sub ::?transfer-from-account params)]
          (case status
            :ajax.status/pending
            [:div.flex.justify-end.mt-1
             [:span.text-xs.text-gray-600.uppercase.mr-1
              "Balance"]
             [gui/SpinnerSmall]]

            :ajax.status/success
            [:div.flex.justify-end.mt-1
             [:span.text-xs.text-gray-600.uppercase
              "Balance"]
             [:span.text-xs.font-bold.ml-1
              (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance]))]]

            :ajax.status/error
            [:div.flex.justify-end.mt-1
             [:span.text-xs.text-red-500
              (get-in error [:response :error :message])]]

            "")))]

     ;; -- To
     (let [to-my-accounts? (get config :transfer-page.config/my-accounts? false)]
       [:div.relative.w-full.flex.flex-col.mt-6
        [Caption "To"]

        ;; -- My Accounts checkbox
        [:div.absolute.top-0.right-0.flex.items-center
         [:input
          {:type "checkbox"
           :checked to-my-accounts?
           :on-change #(set-state update-in [:transfer-page/config :transfer-page.config/my-accounts?] not)}]

         [:span.text-xs.text-gray-600.uppercase.ml-2
          "My Accounts"]]

        ;; -- Select or Input text
        (if to-my-accounts?
          [gui/Select {:value to
                       :options addresses
                       :on-change
                       #(set-state assoc-in [:convex-web/transfer :convex-web.transfer/to] %)}]
          [:input.text-sm.text-right.border
           {:style {:height "26px"}
            :type "text"
            :value to
            :on-change
            #(let [value (gui/event-target-value %)]
               (set-state assoc-in [:convex-web/transfer :convex-web.transfer/to] value))}])

        ;; -- Balance
        (when (s/valid? :convex-web/address to)
          (let [params (merge (select-keys frame [:frame/uuid]) {:address to})

                {:keys [convex-web/account ajax/status ajax/error]} (sub ::?transfer-to-account params)]
            (case status
              :ajax.status/pending
              [:div.flex.justify-end.mt-1
               [:span.text-xs.text-gray-600.uppercase.mr-1
                "Balance"]
               [gui/SpinnerSmall]]

              :ajax.status/success
              [:div.flex.justify-end.mt-1
               [:span.text-xs.text-gray-600.uppercase
                "Balance"]
               [:span.text-xs.font-bold.ml-1
                (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance]))]]

              :ajax.status/error
              [:div.flex.justify-end.mt-1
               [:span.text-xs.text-red-500
                (get-in error [:response :error :message])]]

              "")))])

     ;; -- Transfer
     [:div.flex.flex-col.mt-6.mb-4
      [Caption "Amount"]
      [:input.border.px-1.mt-1.text-right
       {:type "number"
        :value amount
        :on-change
        (fn [event]
          (let [value (gui/event-target-value event)
                amount (js/parseInt value)]
            (set-state
              (fn [state]
                (cond
                  (str/blank? value)
                  (update state :convex-web/transfer dissoc :convex-web.transfer/amount)

                  (int? amount)
                  (assoc-in state [:convex-web/transfer :convex-web.transfer/amount] amount)

                  :else
                  state)))))}]]

     [:div.flex.justify-center.mt-6
      [gui/DefaultButton
       {:on-click #(stack/pop)}
       [:span.text-xs.uppercase "Cancel"]]

      [:div.mx-2]

      [gui/DefaultButton
       {:disabled invalid-transfer?
        :on-click #(let [transaction #:convex-web.transaction {:type :convex-web.transaction.type/transfer
                                                               :target to
                                                               :amount amount}

                         command #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                       :address from
                                                       :transaction transaction}]

                     (command/execute command (fn [command command']
                                                (set-state assoc :convex-web/command (merge command command')))))}
       [:span.text-xs.uppercase "Transfer"]]]]))

(defn TransferPage [frame {:keys [convex-web/command] :as state} set-state]
  [:div.flex-1.my-4.mx-10
   (if command
     [TransferProgress state set-state]
     [TransferInput frame state set-state])])

(s/def :transfer-page.config/my-accounts? boolean?)
(s/def :transfer-page/config (s/keys :opt [:transfer-page.config/my-accounts?]))

(s/def :transfer-page/state (s/keys :opt [:convex-web/transfer
                                          :convex-web/command
                                          :transfer-page/config]))

(def transfer-page
  #:page {:id :page.id/transfer
          :title "Transfer"
          :component #'TransferPage
          :state-spec :transfer-page/state})


;; -- Faucet

(defn faucet-get-target [address set-state]
  (backend/GET-account address {:handler
                                (fn [account]
                                  (set-state assoc :faucet-page/target {:ajax/status :ajax.status/success
                                                                        :convex-web/account account}))}))

(defn FaucetInput [{:keys [convex-web/faucet faucet-page/config] :as state} set-state]
  (let [{:convex-web.faucet/keys [target amount]} faucet

        to-my-accounts? (get config :faucet-page.config/my-accounts? false)

        select-placeholder "Select"

        addresses (cons select-placeholder (map :convex-web.account/address (session/?accounts)))

        Caption (fn [caption]
                  [:span.text-xs.text-indigo-500.uppercase caption])]
    [:div.flex.flex-col.flex-1

     ;; -- Target
     [:div.relative.w-full.flex.flex-col.mt-6
      [Caption "Address"]

      ;; -- My Accounts checkbox
      [:div.absolute.top-0.right-0.flex.items-center
       [:input
        {:type "checkbox"
         :checked to-my-accounts?
         :on-change #(set-state update-in [:faucet-page/config :faucet-page.config/my-accounts?] not)}]

       [:span.text-xs.text-gray-600.uppercase.ml-2
        "My Accounts"]]

      ;; -- Select or Input text
      (if to-my-accounts?
        [gui/Select {:value target
                     :options addresses
                     :on-change
                     #(do
                        (set-state assoc-in [:convex-web/faucet :convex-web.faucet/target] %)

                        (faucet-get-target % set-state))}]
        [:input.text-sm.p-1.border
         {:style {:height "26px"}
          :type "text"
          :value target
          :on-change
          #(let [value (gui/event-target-value %)]
             (set-state assoc-in [:convex-web/faucet :convex-web.faucet/target] value))}])]

     ;; -- Balance
     (let [account (get-in state [:faucet-page/target :convex-web/account])]
       [:div.flex.justify-end.mt-1
        [:span.text-xs.text-gray-600.uppercase
         "Balance"]
        [:span.text-xs.font-bold.ml-1
         (if-let [balance (balance account)]
           (format/format-number balance)
           [gui/SpinnerSmall])]])

     ;; -- Amount
     [:span.text-xs.text-indigo-500.uppercase.mt-6 "Amount"]
     [:input.text-sm.text-right.border
      {:style {:height "26px"}
       :type "number"
       :value amount
       :on-change
       #(let [value (gui/event-target-value %)
              amount (js/parseInt value)]
          (set-state assoc-in [:convex-web/faucet :convex-web.faucet/amount] amount))}]

     [:div.flex.justify-center.mt-6
      [gui/DefaultButton
       {:on-click #(stack/pop)}
       [:span.text-xs.uppercase "Cancel"]]

      [:div.mx-2]

      [gui/DefaultButton
       {:disabled (not (s/valid? :convex-web/faucet faucet))
        :on-click #(do
                     (set-state assoc :ajax/status :ajax.status/pending)

                     (backend/POST-faucet faucet {:handler
                                                  (fn [faucet]
                                                    (set-state assoc
                                                               :ajax/status :ajax.status/success
                                                               :convex-web/faucet faucet))

                                                  :error-handler
                                                  (fn [error]
                                                    (set-state assoc
                                                               :ajax/status :ajax.status/error
                                                               :ajax/error error))}))}
       [:span.text-xs.uppercase "Request"]]]]))

(defn FaucetSuccess [{:keys [convex-web/faucet faucet-page/target]}]
  [:div.flex.flex-col.items-center.text-sm
   [:span.text-lg
    "Success!"]

   ;; -- Your current balance is x.
   [:span.mb-4
    "Your current balance is "

    [:span.font-bold.text-indigo-500
     (format/format-number (+ (balance (get target :convex-web/account))
                              (get faucet :convex-web.faucet/amount)))]

    "."]

   [gui/DefaultButton
    {:on-click #(stack/pop)}
    [:span.text-xs.uppercase "Done"]]])

(defn FaucetError [{:keys [ajax/error]}]
  [:div.flex.flex-col.items-center.text-sm
   [:span.text-lg
    "Sorry"]

   [:span.mb-4
    (get-in error [:response :error :message])]

   [gui/DefaultButton
    {:on-click #(stack/pop)}
    [:span.text-xs.uppercase "Done"]]])

(defn FaucetPage [_ {:keys [ajax/status] :as state} set-state]
  [:div.flex.flex-1.justify-center.my-4.mx-10
   (case status
     :ajax.status/pending
     [gui/Spinner]

     :ajax.status/success
     [FaucetSuccess state]

     :ajax.status/error
     [FaucetError state]

     [FaucetInput state set-state])])

(def faucet-page
  #:page {:id :page.id/faucet
          :title "Faucet"
          :component #'FaucetPage
          :state-spec
          (fn [{:keys [convex-web/faucet]}]
            (s/valid? (s/keys :req [:convex-web.faucet/target]) faucet))
          :on-push
          (fn [_ state set-state]
            (let [address (get-in state [:convex-web/faucet :convex-web.faucet/target])]
              (set-state assoc :faucet-page/target {:ajax/status :ajax.status/pending})

              (faucet-get-target address set-state)))})