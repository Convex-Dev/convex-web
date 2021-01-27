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

(defn CheckingBalance []
  [:span.text-gray-700.text-base.animate-pulse "Checking balance..."])

(defn BalanceUnavailable [_]
  [:span.text-gray-700.text-base "Balance unavailable"])

(defn BalanceIs [_ balance]
  [:span.text-gray-700.text-base "Balance " [:span.font-bold balance]])

(defn AddressUpdatedBalanceIs [address balance]
  [:span.inline-flex.text-gray-700.text-base.space-x-1
   [:a.inline-flex.items-center.space-x-1.w-40
    {:href (rfe/href :route-name/account-explorer {:address address})}
    [gui/AIdenticon {:value address :size gui/identicon-size-small}]

    [:span.font-mono.text-sm.truncate
     {:class gui/hyperlink-hover-class}
     (format/prefix-# address)]]

   [:span " updated balance is "]
   [:span.font-bold balance] "."])

(defn ShowBalance2 [{:keys [convex-web/account ajax/status ajax/error]} {:keys [Pending Error Success]}]
  (case status
    :ajax.status/pending
    [Pending]

    :ajax.status/error
    [Error error]

    :ajax.status/success
    [Success (:convex-web.account/address account) (format/format-number (balance account))]

    ;; Fallback
    [:span]))

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
          :description "These are the details for an Account on the convex.world test network."
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
     [:div.flex.justify-center.items-center.p-10
      [gui/Spinner]]

     :ajax.status/success
     (let [address (get account :convex-web.account/address)]
       [:div.flex.flex-col.max-w-screen-md.space-y-8.my-10.mx-10.text-gray-600
        [:p.text-left
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

        [:span.font-mono.text-base.text-black (format/prefix-# address)]

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
     [:span.text-base.px-6.py-10
      "Sorry. Our server failed to create your account. Please try again?"])])

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

(defn TransferPage [frame state set-state]
  (let [{:keys [convex-web/transfer convex-web/command transfer-page/config] :as state} state

        active-address (session/?active-address)

        from-unselected? (nil? (get transfer :convex-web.transfer/from))

        ;; 'From' defaults to active account
        {:convex-web.transfer/keys [from to amount] :as transfer} (if (and active-address from-unselected?)
                                                                    (assoc transfer :convex-web.transfer/from active-address)
                                                                    transfer)

        invalid-transfer? (or (not (s/valid? :convex-web/transfer transfer))

                              (= :ajax.status/error (get-in state [:transfer-page/from :ajax/status]))

                              (= :ajax.status/error (get-in state [:transfer-page/to :ajax/status])))

        addresses (map :convex-web.account/address (session/?accounts))

        Caption (fn [caption]
                  [:span.text-base.text-gray-700 caption])]
    [:div.flex.flex-col.flex-1.max-w-screen-md.space-y-12

     ;; From
     ;; ===========
     [:div.relative.w-full.flex.flex-col
      [Caption "From"]
      [gui/AccountSelect
       {:active-address from
        :addresses addresses
        :on-change (fn [address]
                     (set-state (fn [state]
                                  (-> state
                                      (dissoc :convex-web/command)
                                      (assoc-in [:convex-web/transfer :convex-web.transfer/from] address)))))}]

      ;; -- Balance
      (when (s/valid? :convex-web/address from)
        (let [params {:frame/uuid (get frame :frame/uuid)
                      :address from}]
          [:div.flex.justify-end
           [ShowBalance2 (sub ::?transfer-from-account params)
            {:Pending CheckingBalance
             :Error BalanceUnavailable
             :Success BalanceIs}]]))]


     ;; To
     ;; ===========
     (let [to-my-accounts? (get config :transfer-page.config/my-accounts? false)]
       [:div.w-full.flex.flex-col
        [:div.flex.justify-between
         [Caption "To"]

         ;; -- Show My Accounts
         [:div.flex.items-center
          [:input
           {:type "checkbox"
            :checked to-my-accounts?
            :on-change
            (fn []
              (set-state
                (fn [state]
                  (let [;; Toogle my account
                        state (update-in state [:transfer-page/config :transfer-page.config/my-accounts?] not)
                        ;; Always remove to
                        state (update state :convex-web/transfer dissoc :convex-web.transfer/to)]
                    state))))}]

          [:span.text-xs.text-gray-600.uppercase.ml-2
           "Show My Accounts"]]]


        ;; -- Select or Input text
        (if to-my-accounts?
          [gui/AccountSelect
           {:active-address (format/trim-0x to)
            :addresses addresses
            :on-change (fn [address]
                         (set-state (fn [state]
                                      (-> state
                                          (dissoc :convex-web/command)
                                          (assoc-in [:convex-web/transfer :convex-web.transfer/to] address)))))}]
          [:input
           {:class gui/input-style
            :type "text"
            :value to
            :on-change
            #(let [value (gui/event-target-value %)]
               (set-state (fn [state]
                            (-> state
                                (dissoc :convex-web/command)
                                (assoc-in [:convex-web/transfer :convex-web.transfer/to] value)))))}])

        ;; -- Balance
        (when (s/valid? :convex-web/address to)
          (let [params {:frame/uuid (get frame :frame/uuid)
                        :address to}]
            [:div.flex.justify-end
             [ShowBalance2 (sub ::?transfer-to-account params)
              {:Pending CheckingBalance
               :Error BalanceUnavailable
               :Success BalanceIs}]]))])


     ;; Amount
     ;; ===========
     [:div.flex.flex-col
      [Caption "Amount"]
      [:input.text-right
       {:class gui/input-style
        :type "number"
        :value amount
        :on-change
        (fn [event]
          (let [value (gui/event-target-value event)]
            (set-state
              (fn [state]
                (let [state (dissoc state :convex-web/command)]
                  (cond
                    (or (str/blank? value) (= (js/parseInt value) js/NaN))
                    (update state :convex-web/transfer dissoc :convex-web.transfer/amount)

                    :else
                    (assoc-in state [:convex-web/transfer :convex-web.transfer/amount] (js/parseInt value))))))))}]]


     ;; Transfer
     ;; ===========
     [:div.flex
      [gui/PrimaryButton
       {:disabled invalid-transfer?
        :on-click #(let [transaction #:convex-web.transaction {:type :convex-web.transaction.type/transfer
                                                               :target to
                                                               :amount amount}

                         command #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                       :address from
                                                       :transaction transaction}]

                     (set-state assoc :convex-web/command {:convex-web.command/status :convex-web.command.status/running})

                     (command/execute command (fn [command command']
                                                (cond
                                                  (= :convex-web.command.status/success (:convex-web.command/status command'))
                                                  (do
                                                    (set-state
                                                      (fn [state]
                                                        (let [state' (assoc state :convex-web/command (merge command command'))

                                                              ;; Set status to pending because we need to check the updated balance.
                                                              state' (assoc-in state' [:transfer-page/from :ajax/status] :ajax.status/pending)
                                                              state' (assoc-in state' [:transfer-page/to :ajax/status] :ajax.status/pending)]
                                                          state')))


                                                    (get-transfer-account {:frame/uuid (get frame :frame/uuid)
                                                                           :address from
                                                                           :state-key :transfer-page/from})

                                                    (get-transfer-account {:frame/uuid (get frame :frame/uuid)
                                                                           :address to
                                                                           :state-key :transfer-page/to}))

                                                  :else
                                                  (set-state assoc :convex-web/command (merge command command'))))))}
       [:span.block.text-sm.uppercase
        {:class [gui/button-child-large-padding
                 (if invalid-transfer?
                   "text-gray-200"
                   "text-white")]}
        "Transfer"]]]


     ;; Status
     ;; ===========
     (case (:convex-web.command/status command)
       :convex-web.command.status/running
       [:span.text-base.text-gray-700.animate-pulse
        "Processing..."]

       :convex-web.command.status/success
       [:span.inline-flex.items-center.space-x-2
        [:span "Transferred "]

        [:span.font-bold.text-black
         (format/format-number (get transfer :convex-web.transfer/amount))]

        [:span " from "]

        (let [address-or-blob (get transfer :convex-web.transfer/from)
              address (format/trim-0x address-or-blob)]
          [:a.inline-flex.items-center.space-x-1.w-40
           {:href (rfe/href :route-name/account-explorer {:address address})}
           [gui/AIdenticon {:value address :size gui/identicon-size-small}]

           [:span.font-mono.text-sm.truncate
            {:class gui/hyperlink-hover-class}
            (format/prefix-# (get transfer :convex-web.transfer/to))]])

        [:span " to "]

        (let [address-or-blob (get transfer :convex-web.transfer/to)
              address (format/trim-0x address-or-blob)]
          [:a.inline-flex.items-center.space-x-1.w-40
           {:href (rfe/href :route-name/account-explorer {:address address})}
           [gui/AIdenticon {:value address :size gui/identicon-size-small}]

           [:span.font-mono.text-sm.truncate
            {:class gui/hyperlink-hover-class}
            (format/prefix-# (get transfer :convex-web.transfer/to))]])]

       :convex-web.command.status/error
       [:span.text-base.text-black
        "Sorry. Your transfer couldn't be completed. Please try again?"]

       [:div])]))

(s/def :transfer-page.config/my-accounts? boolean?)
(s/def :transfer-page/config (s/keys :opt [:transfer-page.config/my-accounts?]))

(s/def :transfer-page/state (s/keys :opt [:convex-web/transfer
                                          :convex-web/command
                                          :transfer-page/config]))

(def transfer-page
  #:page {:id :page.id/transfer
          :title "Transfer"
          :description "Use this tool to make transfers from your Accounts to any other Accounts."
          :component #'TransferPage
          :state-spec :transfer-page/state})


;; -- Faucet

(defn get-faucet-target-account [frame-uuid address]
  (backend/GET-account address {:handler
                                (fn [account]
                                  (stack/set-state frame-uuid update :faucet-page/target merge {:ajax/status :ajax.status/success
                                                                                                :convex-web/account account}))

                                :error-handler
                                (fn [error]
                                  (stack/set-state frame-uuid update :faucet-page/target merge {:ajax/status :ajax.status/error
                                                                                                :ajax/error error}))}))

(re-frame/reg-sub-raw ::?faucet-target
  (fn [app-db [_ frame-uuid address]]
    ;; On subscription creation - whenever frame-uuid or address change.
    (let [{:frame/keys [state]} (stack/find-frame @app-db frame-uuid)
          state' (select-keys state [:convex-web/faucet :faucet-page/config])]
      (if (s/valid? :convex-web/address address)
        (do
          (stack/set-state frame-uuid (constantly (merge state' {:faucet-page/target {:ajax/status :ajax.status/pending}})))
          (get-faucet-target-account frame-uuid address))
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
                   caption])]
    [:div.flex.flex-col.max-w-screen-md.space-y-12

     ;; -- Target
     [:div.relative.w-full.flex.flex-col
      [Caption "Account"]

      ;; -- My Accounts checkbox
      [:div.absolute.top-0.right-0.flex.items-center
       [:input
        {:type "checkbox"
         :checked to-my-account?
         :on-change
         (fn []
           (set-state
             (fn [state]
               (let [;; Toogle my account
                     state (update-in state [:faucet-page/config :faucet-page.config/my-accounts?] not)
                     ;; Always remove target
                     state (update state :convex-web/faucet dissoc :convex-web.faucet/target)]
                 state))))}]

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
         {:class gui/input-style
          :type "text"
          :value target
          :on-change
          #(let [value (gui/event-target-value %)]
             (set-state assoc-in [:convex-web/faucet :convex-web.faucet/target] (format/trim-0x value)))}])


      ;; -- Balance
      [:div.flex.justify-end
       [ShowBalance2 (sub ::?faucet-target (get frame :frame/uuid) target)
        {:Pending CheckingBalance
         :Error BalanceUnavailable
         :Success BalanceIs}]]]


     ;; -- Amount
     [:div.flex.flex-col
      [Caption "Amount"]
      [:input.text-right
       {:class gui/input-style
        :type "number"
        :value amount
        :on-change
        #(let [value (gui/event-target-value %)]
           (set-state
             (fn [state]
               (cond
                 (or (str/blank? value) (= (js/parseInt value) js/NaN))
                 (update state :convex-web/faucet dissoc :convex-web.faucet/amount)

                 :else
                 (assoc-in state [:convex-web/faucet :convex-web.faucet/amount] (js/parseInt value))))))}]]

     [:div.flex.mt-6

      (when modal?
        [:<>
         [gui/DefaultButton
          {:on-click #(stack/pop)}
          [:span.text-xs.uppercase "Cancel"]]

         [:div.mx-2]])

      [gui/PrimaryButton
       {:disabled invalid?
        :on-click #(do
                     (set-state assoc :ajax/status :ajax.status/pending)

                     (backend/POST-faucet faucet {:handler
                                                  (fn [faucet]
                                                    (set-state
                                                      (fn [state]
                                                        (let [state' (assoc state
                                                                       :ajax/status :ajax.status/success
                                                                       :convex-web/faucet faucet)

                                                              ;; Set status to pending because we need to check the updated balance.
                                                              state' (assoc-in state' [:faucet-page/target :ajax/status] :ajax.status/pending)]
                                                          state')))

                                                    ;; Get updated target Account - timeout is important to "give some time" to Convex
                                                    ;; to process the transaction, otherwise we might get the account without the updated balance.
                                                    (js/setTimeout
                                                      (fn []
                                                        (get-faucet-target-account (get frame :frame/uuid) target))
                                                      500))

                                                  :error-handler
                                                  (fn [error]
                                                    (set-state assoc
                                                               :ajax/status :ajax.status/error
                                                               :ajax/error error))}))}
       [:span.block.text-sm.uppercase
        {:class [gui/button-child-large-padding
                 (if invalid?
                   "text-gray-200"
                   "text-white")]}
        "Request"]]]


     ;; -- Status
     (let [copy-style "text-base text-gray-700"]
       (case status
         :ajax.status/pending
         [:span.animate-pulse {:class copy-style}
          "Processing..."]

         :ajax.status/error
         [:span {:class copy-style}
          (get-in state [:ajax/error :response :error :message])]

         :ajax.status/success
         [ShowBalance2 (sub ::?faucet-target (get frame :frame/uuid) target)
          {:Pending CheckingBalance
           :Error BalanceUnavailable
           :Success AddressUpdatedBalanceIs}]

         ;; Unknown status
         [:div]))]))

(def faucet-page
  #:page {:id :page.id/faucet
          :title "Faucet"
          :description "The Faucet lets you request free Convex coins for your Accounts. You can make a request once every 5 minutes."
          :component #'FaucetPage})