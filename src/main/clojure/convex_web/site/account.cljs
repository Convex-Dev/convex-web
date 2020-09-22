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

(defn MyAccountPage [_ {:keys [ajax/status convex-web/account]} set-state]
  (if-let [active-address (session/?active-address)]
    ;; Fetch account if:
    ;; - it's nil - it hasn't been fetched yet;
    ;; - active address has changed - it's different from state account's address
    (let [fetch? (or (nil? account) (not= active-address (:convex-web.account/address account)))]

      (when fetch?
        (set-state assoc :ajax/status :ajax.status/pending)

        (backend/GET-account active-address {:handler
                                             (fn [account]
                                               (set-state assoc
                                                          :ajax/status :ajax.status/success
                                                          :convex-web/account account))

                                             :error-handler
                                             (fn [error]
                                               (set-state assoc
                                                          :ajax/status :ajax.status/error
                                                          :ajax/error error))}))

      (case status
        :ajax.status/pending
        [:div.flex.flex-col.flex-1.justify-center.items-center
         [gui/Spinner]]

        :ajax.status/success
        [gui/Account account]

        :ajax.status/error
        [:div.flex.flex-col.flex-1.justify-center.items-center
         [:span "Sorry. Our servers failed to load your account."]]

        ;; Fallback
        [:div]))

    ;; No active address
    [:div]))

(def my-account-page
  #:page {:id :page.id/my-account
          :title "Account Details"
          :component #'MyAccountPage
          :state-spec
          (s/nonconforming
            (s/or :pending :ajax/pending-status
                  :success (s/merge :ajax/success-status (s/keys :req [:convex-web/account]))
                  :error :ajax/error-status))
          :on-resume
          (fn [_ state set-state]
            ;; Don't change `ajax/status` on resume
            ;; to avoid showing the spinner - and don't bother about the error handler.
            (when-let [address (get-in state [:convex-web/account :convex-web.account/address])]
              (backend/GET-account address {:handler
                                            (fn [account]
                                              (set-state assoc :convex-web/account account))})))})


(defn CreateAccountPage [_ {:keys [convex-web/account ajax/status]} set-state]
  [:div.w-full.flex.flex-col.items-center

   (case status
     :ajax.status/pending
     [gui/Spinner]

     :ajax.status/success
     (let [address (get account :convex-web.account/address)]
       [:div.flex.flex-col.items-start.max-w-screen-md.space-y-8.my-10.mx-10.text-gray-600
        [:p
         "This is your new Convex Account."]

        [:p
         "Accounts give you a psuedonymous identity on the Convex network, a
          personal environment where you can store code and data, a memory
          allowance, and a balance of Convex coins. Only you can execute
          transactions using your Account, although all information is public
          and you can see information stored in the Accounts of others."]

        [:p
         "For convenience this Account is managed on your behalf by the
          convex.world server, so you don't need to manage your own private
          keys. Accounts will be periodically refreshed on the Testnet server,
          so please consider as a temporary Account keep a backup of anything
          of value."]

        [:span.font-mono.text-base.text-black (format/address-blob address)]

        [:div.self-center
         [gui/BlackButton
          {:on-click
           #(do
              (set-state
                (fn [state]
                  (assoc state :status :pending)))

              (backend/POST-confirm-account address {:handler
                                                     (fn [account]
                                                       (session/add-account account true)
                                                       (stack/pop))}))}
          [:span.font-mono.text-base.text-white.uppercase
           "Confirm"]]]])

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

(defn TransferProgress [frame {:convex-web/keys [command transfer] :as state} set-state]
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
       {:on-click
        (fn []
          (if (:frame/modal? frame)
            (stack/pop)
            (set-state #(dissoc % :convex-web/command))))}
       [:span.text-xs.uppercase "Done"]]]

     :convex-web.command.status/error
     [:span.text-sm.text-black
      (if (s/valid? :ajax/error (get command :convex-web.command/error))
        (get-in command [:convex-web.command/error :response :error :message])
        "Sorry. Your transfer couldn't be completed. Please try again?")]

     "...")])

(defn TransferInput [frame {:keys [convex-web/transfer transfer-page/config] :as state} set-state]
  (let [active-address (session/?active-address)

        from-unselected? (nil? (get transfer :convex-web.transfer/from))

        ;; 'From' defaults to active account
        {:convex-web.transfer/keys [from to amount] :as transfer} (if (and active-address from-unselected?)
                                                                    (assoc transfer :convex-web.transfer/from active-address)
                                                                    transfer)

        invalid-transfer? (or (not (s/valid? :convex-web/transfer transfer))

                              (= :ajax.status/error (get-in state [:transfer-page/from :ajax/status]))

                              (= :ajax.status/error (get-in state [:transfer-page/to :ajax/status])))

        select-placeholder "Select"

        addresses (cons select-placeholder (map :convex-web.account/address (session/?accounts)))

        Caption (fn [caption]
                  [:span.text-base.text-gray-700 caption])]
    [:div.flex.flex-col.flex-1.max-w-screen-md.space-y-8

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
          "Show My Accounts"]]

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

     [:div.flex.mt-6
      (when (get frame :modal?)
        [:<>
         [gui/DefaultButton
          {:on-click #(stack/pop)}
          [:span.text-xs.uppercase "Cancel"]]

         [:div.mx-2]])

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
  [:div.flex-1
   (if command
     [TransferProgress frame state set-state]
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

(re-frame/reg-sub-raw ::?faucet-target
  (fn [app-db [_ frame-uuid address]]
    (let [{:frame/keys [state]} (stack/find-frame @app-db frame-uuid)
          state' (select-keys state [:convex-web/faucet :faucet-page/config])]
      (if (s/valid? :convex-web/address address)
        (do
          (stack/set-state frame-uuid (constantly (merge state' {:faucet-page/target {:ajax/status :ajax.status/pending}})))

          (backend/GET-account address {:handler
                                        (fn [account]
                                          (stack/set-state frame-uuid update :faucet-page/target merge {:ajax/status :ajax.status/success
                                                                                                        :convex-web/account account}))

                                        :error-handler
                                        (fn [error]
                                          (stack/set-state frame-uuid update :faucet-page/target merge {:ajax/status :ajax.status/error
                                                                                                        :ajax/error error}))}))
        (stack/set-state frame-uuid (constantly state'))))

    (make-reaction
      (fn []
        (let [frame (stack/find-frame @app-db frame-uuid)]
          (get-in frame [:frame/state :faucet-page/target]))))))


(defn FaucetPage [frame state set-state]
  (let [{:keys [frame/modal?]} frame

        {:keys [convex-web/faucet ajax/status faucet-page/config]} state

        active-address (session/?active-address)

        target-unselected? (nil? (get faucet :convex-web.faucet/target))
        target-invalid? (not (s/valid? :convex-web/address (get faucet :convex-web.faucet/target)))

        to-my-account? (get config :faucet-page.config/my-accounts? false)

        ;; Target address must be overridden if:
        ;; - 'Show my accounts' is checked
        ;; - there's an active address
        ;; - and the current target is either missing or invalid.
        override-target? (and to-my-account? active-address (or target-unselected? target-invalid?))

        ;; Target defaults to active account.
        {:convex-web.faucet/keys [target amount] :as faucet} (if override-target?
                                                               (assoc faucet :convex-web.faucet/target active-address)
                                                               faucet)

        addresses (map :convex-web.account/address (session/?accounts))

        invalid? (not (s/valid? :convex-web/faucet faucet))

        Caption (fn [caption]
                  [:span.text-base.text-gray-700
                   caption])

        SmallCaption (fn [caption]
                       [:span.text-sm.text-gray-700
                        caption])

        input-style "h-10 text-sm text-gray-600 px-2 border rounded bg-blue-100 bg-opacity-50"]
    [:div.flex.flex-col.max-w-screen-md.space-y-12

     ;; -- Target
     [:div.relative.w-full.flex.flex-col
      [Caption "Account"]

      ;; -- My Accounts checkbox
      [:div.absolute.top-0.right-0.flex.items-center
       [:input
        {:type "checkbox"
         :checked to-my-account?
         :on-change #(set-state update-in [:faucet-page/config :faucet-page.config/my-accounts?] not)}]

       [:span.text-xs.text-gray-600.uppercase.ml-2
        "Show My Accounts"]]

      ;; -- Select or Input text
      (if to-my-account?
        [gui/AccountSelect
         {:active-address target
          :addresses addresses
          :on-change (fn [address]
                       (set-state assoc-in [:convex-web/faucet :convex-web.faucet/target] address))}]
        [:input
         {:class input-style
          :type "text"
          :value target
          :on-change
          #(let [value (gui/event-target-value %)]
             (set-state assoc-in [:convex-web/faucet :convex-web.faucet/target] value))}])


      ;; -- Balance
      (let [frame-uuid (get frame :frame/uuid)
            faucet-target (sub ::?faucet-target frame-uuid target)]
        [:div.flex.justify-end.items-baseline.space-x-2
         (case (get faucet-target :ajax/status)
           :ajax.status/pending
           [:span.text-sm
            "Checking balance..."]

           :ajax.status/success
           (let [account (get faucet-target :convex-web/account)
                 balance (balance account)]
             [:<>
              [SmallCaption "Balance"]
              [:span.text-xs.font-bold
               (format/format-number balance)]])

           :ajax.status/error
           [:span.text-sm
            "Balance unavailable"]

           ;; No status; don't show anything.
           [:span.text-sm
            ""])])]


     ;; -- Amount
     [:div.flex.flex-col
      [Caption "Amount"]
      [:input.text-right
       {:class input-style
        :type "number"
        :value amount
        :on-change
        #(let [value (gui/event-target-value %)
               amount (js/parseInt value)]
           (set-state assoc-in [:convex-web/faucet :convex-web.faucet/amount] amount))}]]

     [:div.flex.mt-6

      (when modal?
        [:<>
         [gui/DefaultButton
          {:on-click #(stack/pop)}
          [:span.text-xs.uppercase "Cancel"]]

         [:div.mx-2]])

      [gui/BlueButton
       {:disabled invalid?
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
       [:span.text-sm.uppercase
        {:class (if invalid?
                  "text-gray-200"
                  "text-white")}
        "Request"]]]


     ;; -- Status
     (let [copy-style "text-base text-gray-700"]
       (case status
         :ajax.status/pending
         [:span {:class copy-style}
          "Processing..."]

         :ajax.status/error
         [:span {:class copy-style}
          (get-in state [:ajax/error :response :error :message])]

         :ajax.status/success
         [:span {:class copy-style}
          "Your updated balance is "

          [:span.font-bold
           (format/format-number (+ (balance (get-in state [:faucet-page/target :convex-web/account]))
                                    (get faucet :convex-web.faucet/amount)))]

          "."]

         ;; Unknown status
         [:div]))]))

(def faucet-page
  #:page {:id :page.id/faucet
          :title "Faucet"
          :component #'FaucetPage})