(ns convex-web.site.explorer
  (:require [convex-web.site.runtime :refer [sub disp]]
            [convex-web.site.runtime :as runtime]
            [convex-web.site.backend :as backend]
            [convex-web.site.gui :as gui]
            [convex-web.site.stack :as stack]
            [convex-web.site.format :as format]
            [convex-web.explorer :as explorer]
            [convex-web.site.markdown :as markdown]

            [cljs.spec.alpha :as s]

            [reitit.frontend.easy :as rfe]
            [convex-web.site.session :as session]
            [clojure.string :as str]
            [reagent.core :as reagent]))

(def blocks-polling-interval 5000)

(defn start-polling-blocks [_ _ set-state]
  (let [interval-callback (fn []
                            (backend/GET-blocks
                              {:handler
                               (fn [blocks]
                                 (set-state #(assoc % :ajax/status :ajax.status/success
                                                      :convex-web/blocks (or blocks []))))
                               :error-handler
                               (fn [error]
                                 (set-state #(assoc % :ajax/status :ajax.status/error
                                                      :ajax/error error)))}))

        interval-ref (runtime/set-interval interval-callback blocks-polling-interval)]
    (set-state #(assoc % :runtime/interval-ref interval-ref
                         :ajax/status :ajax.status/pending))))

(defn stop-polling-blocks [_ {:keys [runtime/interval-ref]} _]
  (when-let [id @interval-ref]
    (runtime/clear-interval id)))

(defn flatten-transactions [blocks]
  (let [block-index&transaction-index (juxt :convex-web.block/index (comp :convex-web.transaction/index :convex-web.signed-data/value))]
    (->> blocks
         (mapcat
           (fn [{:convex-web.block/keys [transactions] :as block}]
             (map-indexed
               (fn [index-transaction signed-data]
                 ;; Add index to Transaction
                 (let [signed-data (assoc-in signed-data [:convex-web.signed-data/value :convex-web.transaction/index] index-transaction)]
                   (merge block signed-data)))
               transactions)))
         (sort-by block-index&transaction-index)
         (reverse))))

(s/fdef flatten-transactions
  :args (s/cat :blocks :convex-web/blocks)
  :ret (s/merge :convex-web/block :convex-web/signed-data))

(defn TransactionsTable [blocks & [{:keys [modal?]}]]
  [:table.text-left.table-auto
   [:thead
    (let [th-class "text-xs uppercase text-gray-600 bg-gray-100 p-0 sticky top-0"]
      [:tr
       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Block"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Transaction"]]

       [:th
        {:class th-class}

        [:div.p-2.border-b.border-gray-300
         "Sequence"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Type"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Timestamp"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Signer"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Value"]]])]

   [:tbody.align-baseline
    (for [m (flatten-transactions blocks)]
      (let [block-index (get m :convex-web.block/index)
            transaction-index (get-in m [:convex-web.signed-data/value :convex-web.transaction/index])
            transaction-type (get-in m [:convex-web.signed-data/value :convex-web.transaction/type])

            td-class ["p-1 whitespace-no-wrap text-xs"]]
        ^{:key [block-index transaction-index]}
        [:tr.hover:bg-gray-100.cursor-default {:style {:height "34px"}}
         ;; -- Block Index
         [:td {:class td-class}
          [:div.flex.flex-1.justify-end
           (if modal?
             [:code.underline.cursor-pointer
              {:on-click #(stack/push :page.id/block-explorer {:modal? true
                                                               :state {:convex-web/block {:convex-web.block/index block-index}}})}
              block-index]
             [:a
              {:href (rfe/href :route-name/block-explorer {:index block-index})}
              [:code.underline block-index]])

           ;; External link
           [:a.ml-2
            {:href (rfe/href :route-name/block-explorer {:index block-index})
             :target "_blank"}
            [gui/IconExternalLink {:class "w-4 h4 text-gray-600 hover:text-gray-800"}]]]]

         ;; -- Transaction Index
         [:td {:class (cons "text-right" td-class)}
          [:span.text-xs
           transaction-index]]

         ;; -- Transaction Sequence
         [:td {:class (cons "text-right" td-class)}
          [:span.text-xs
           (get-in m [:convex-web.signed-data/value :convex-web.transaction/sequence])]]

         ;; -- Transaction Type
         [:td
          {:class
           (conj td-class (case transaction-type
                            :convex-web.transaction.type/transfer
                            "text-indigo-500"

                            :convex-web.transaction.type/invoke
                            "text-pink-500"

                            ""))}
          [:span.text-xs.uppercase
           transaction-type]]

         ;; -- Timestamp
         [:td {:class td-class}
          [:span
           (-> (get m :convex-web.block/timestamp)
               (format/date-time-from-millis)
               (format/date-time-to-string))]]

         ;; -- Signer
         [:td {:class td-class}
          (let [address (get m :convex-web.signed-data/address)]
            [:a.flex-1.underline.hover:text-indigo-500
             {:href (rfe/href :route-name/account-explorer {:address address})}
             [:code.text-xs address]])]

         ;; -- Value
         [:td
          {:class td-class}
          (case (get-in m [:convex-web.signed-data/value :convex-web.transaction/type])
            :convex-web.transaction.type/invoke
            [:div.flex
             [gui/Highlight
              (get-in m [:convex-web.signed-data/value :convex-web.transaction/source])]]

            :convex-web.transaction.type/transfer
            [:span
             "Transferred "
             [:span.font-bold.text-indigo-500
              (format/format-number
                (get-in m [:convex-web.signed-data/value :convex-web.transaction/amount]))]

             " to "
             (let [address (get-in m [:convex-web.signed-data/value :convex-web.transaction/target])]
               [:a.flex-1.underline.hover:text-indigo-500
                {:href (rfe/href :route-name/account-explorer {:address address})}
                [:code.text-xs address]])])]]))]])

(s/def :explorer.blocks.state/pending
  (s/merge :ajax/pending-status (s/keys :req [:runtime/interval-ref])))

(s/def :explorer.blocks.state/success
  (s/merge :ajax/success-status (s/keys :req [:runtime/interval-ref :convex-web/blocks])))

(s/def :explorer.blocks.state/error
  (s/merge :ajax/error-status (s/keys :req [:runtime/interval-ref])))

(s/def :explorer.blocks/state-spec
  (s/or :pending :explorer.blocks.state/pending
        :success :explorer.blocks.state/success
        :error :explorer.blocks.state/error))

(s/def :explorer.blocks-range/state-spec
  (s/nonconforming
    (s/or :pending :ajax/pending-status
          :success (s/merge :ajax/success-status (s/keys :req [:convex-web/blocks]))
          :error :ajax/error-status)))

;; ---

(defn Block [{:convex-web.block/keys [index timestamp] :as block}]
  [:div.flex.flex-col

   ;; -- Block
   [:div.flex.mb-4
    [:div.flex.items-center.justify-center.px-2.py-4
     [:span.text-4xl.text-center.text-gray-700.leading-none index]]

    [:div.flex.flex-col.ml-2
     [:code.text-xs.text-gray-700 timestamp]
     [:span.text-xs (.toISOString (js/Date. timestamp))]]]

   ;; -- Transactions
   [TransactionsTable (vector block)]])

(defn BlockPage [_ {:keys [ajax/status ajax/error convex-web/block]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    :ajax.status/success
    [:div.flex.mt-4.mx-10
     [Block block]]

    :ajax.status/error
    [:span (get-in error [:response :error :message])]

    ;; Fallback
    [:div]))


(s/def :block-page.state/pending (s/merge :ajax/pending-status (s/keys :req [:convex-web/block])))
(s/def :block-page.state/success (s/merge :ajax/success-status (s/keys :req [:convex-web/block])))
(s/def :block-page.state/error :ajax/error-status)
(s/def :block-page/state-spec (s/or :pending :block-page.state/pending
                                    :success :block-page.state/success
                                    :error :block-page.state/error))

(defn- get-block [_ state set-state]
  (set-state assoc :ajax/status :ajax.status/pending)

  (let [index (get-in state [:convex-web/block :convex-web.block/index])]
    (backend/GET-block
      index
      {:handler
       (fn [block]
         (when block
           (set-state #(assoc % :convex-web/block block
                                :ajax/status :ajax.status/success))))
       :error-handler
       (fn [error]
         (set-state #(assoc % :ajax/status :ajax.status/error
                              :ajax/error error)))})))

(def block-page
  #:page {:id :page.id/block-explorer
          :title "Block"
          :component #'BlockPage
          :state-spec :block-page/state-spec
          :on-push #'get-block})

;; --- Account

(defn AccountPage [_ {:keys [convex-web/account ajax/status ajax/error]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [gui/Spinner]]

    :ajax.status/error
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [:span (get-in error [:response :error :message])]]

    :ajax.status/success
    [:div.flex.flex-1.justify-center.my-4.mx-10
     [gui/Account account]]))

(defn- get-account [_ state set-state]
  (let [address (get-in state [:convex-web/account :convex-web.account/address])]
    (backend/GET-account
      address
      {:handler
       (fn [account]
         (set-state #(assoc % :convex-web/account account
                              :ajax/status :ajax.status/success)))
       :error-handler
       (fn [error]
         (set-state #(assoc % :ajax/status :ajax.status/error
                              :ajax/error error)))})))

(s/def :account-page.state/pending (s/and (s/keys :req [:ajax/status :convex-web/account])
                                          #(= :ajax.status/pending (:ajax/status %))))

(s/def :account-page.state/success (s/and (s/keys :req [:ajax/status :convex-web/account])
                                          #(= :ajax.status/success (:ajax/status %))))

(s/def :account-page.state/error (s/and (s/keys :req [:ajax/status :ajax/error])
                                        #(= :ajax.status/error (:ajax/status %))))

(s/def :account-page/state-spec (s/or :pending :account-page.state/pending
                                      :success :account-page.state/success
                                      :error :account-page.state/error))

(def account-page
  #:page {:id :page.id/account-explorer
          :title "Account"
          :state-spec :account-page/state-spec
          :component #'AccountPage
          :on-push #'get-account})


;; -- Accounts Commons

(defn AccountsTable [accounts & [{:keys [modal?]}]]
  [:table.text-left.table-auto
   [:thead
    (let [th-class "text-xs uppercase text-gray-600 bg-gray-100 p-0 sticky top-0"]
      [:tr
       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Address"]]

       [:th
        {:class th-class}
        [:div.p-2.border-b.border-gray-300
         "Type"]]

       [:th
        {:class th-class
         :colSpan "2"}

        [:div.p-2.border-b.border-gray-300
         "Amount"]]])]

   (let [active-address (session/?active-address)

         my-addresses (->> (session/?accounts)
                           (map (comp str/upper-case :convex-web.account/address))
                           (into #{}))]
     [:tbody.align-baseline
      (for [{:convex-web.account/keys [address status]} (sort-by :convex-web.account/address accounts)]
        (let [td-class "p-2 font-mono text-xs text-gray-700 whitespace-no-wrap"

              me? (contains? my-addresses (str/upper-case address))

              address-blob (format/address-blob address)]
          ^{:key address}
          [:tr.hover:bg-gray-100.cursor-default
           [:td.flex.items-center {:class td-class}
            [:div.flex.items-center
             (if modal?
               [:code.underline.cursor-pointer.mr-2
                {:on-click #(stack/push :page.id/account-explorer {:state
                                                                   {:ajax/status :ajax.status/pending
                                                                    :convex-web/account {:convex-web.account/address address}}
                                                                   :modal? true})}
                address-blob]
               [:a.flex-1.underline.hover:text-indigo-500.mr-2
                {:href (rfe/href :route-name/account-explorer {:address address})}
                [:code.text-xs address-blob]])

             [gui/ClipboardCopy address-blob]

             ;; External link
             [:a.ml-2
              {:href (rfe/href :route-name/account-explorer {:address address})
               :target "_blank"}
              [gui/IconExternalLink {:class "w-4 h-4 text-gray-500 hover:text-black"}]]]

            (when (and me? (not= address active-address))
              [gui/Tooltip
               {:title "Switch to this account"}
               [:span.uppercase.text-gray-500.hover:text-black.ml-4.mr-4.cursor-pointer
                {:on-click #(session/pick-address address)}
                "Switch"]])

            (when (and me? (= address active-address))
              [gui/Tooltip
               {:title "This account is active"}
               [gui/CheckIcon {:class "w-4 h-4 text-green-500 ml-4"}]])]

           [:td {:class td-class}
            (let [[label style tooltip] (cond
                                          (get status :convex-web.account-status/library?)
                                          ["library" "bg-purple-500" "Library's Address"]

                                          (get status :convex-web.account-status/actor?)
                                          ["actor" "bg-indigo-500" "Actor's Address"]

                                          :else
                                          ["user" "bg-green-400" "User's Address"])]
              [gui/Tooltip
               {:title tooltip}
               [:span.text-white.rounded-full.py-2.px-4.ml-4.cursor-default
                {:class style}
                label]])]

           [:td {:class td-class}
            [:div.flex.justify-end
             [:span.text-xs.font-bold.text-indigo-500
              (format/format-number (str (:convex-web.account-status/balance status)))]]]]))])])

(defn- get-accounts-range [set-state & [{:keys [start end]}]]
  (backend/GET-accounts
    (merge {:handler
            (fn [{:keys [meta convex-web/accounts]}]
              (set-state assoc
                         :ajax/status :ajax.status/success
                         :convex-web/accounts (or accounts [])
                         :meta meta))
            :error-handler
            (fn [error]
              (set-state #(assoc % :ajax/status :ajax.status/error
                                   :ajax/error error)))}
           (when start
             {:start start})
           (when end
             {:end end}))))

;; -- Accounts Range

(defn AccountsRangePage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/accounts meta]} set-state]
  (let [{:keys [start end total] :as range} (select-keys meta [:start :end :total])]
    [:div.flex.flex-col.flex-1.mt-4.mx-10

     ;; -- Pagination
     [gui/RangeNavigation
      (merge range
             {:on-previous-click
              (fn []
                (set-state #(assoc % :ajax/status :ajax.status/pending))

                (get-accounts-range set-state (explorer/previous-range start)))

              :on-next-click
              (fn []
                (set-state #(assoc % :ajax/status :ajax.status/pending))

                (get-accounts-range set-state (explorer/next-range end total)))})]

     ;; -- Body
     (case status
       :ajax.status/pending
       [:div.flex.flex-1.justify-center.items-center
        [gui/Spinner]]

       [AccountsTable accounts {:modal? modal?}])]))

(def accounts-range-page
  #:page {:id :page.id/accounts-range-explorer
          :title "All Accounts"
          :component #'AccountsRangePage
          :state-spec :accounts-page/state-spec
          :on-push
          (fn [_ _ set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (get-accounts-range set-state))})


;; -- Accounts

(defn AccountsPage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/accounts]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col.mt-4.mx-10
     [AccountsTable accounts {:modal? modal?}]

     [:div.my-4
      [gui/Tooltip
       "View all Accounts"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/accounts-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(s/def :accounts-page.state/pending (s/and (s/keys :req [:ajax/status])
                                           #(= :ajax.status/pending (:ajax/status %))))

(s/def :accounts-page.state/success (s/and (s/keys :req [:ajax/status :convex-web/accounts])
                                           #(= :ajax.status/success (:ajax/status %))))

(s/def :accounts-page.state/error (s/and (s/keys :req [:ajax/status :ajax/error])
                                         #(= :ajax.status/error (:ajax/status %))))

(s/def :accounts-page/state-spec (s/or :pending :accounts-page.state/pending
                                       :success :accounts-page.state/success
                                       :error :accounts-page.state/error))

(def accounts-page
  #:page {:id :page.id/accounts-explorer
          :title "Accounts"
          :state-spec :accounts-page/state-spec
          :component #'AccountsPage
          :on-push
          (fn [_ _ set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (get-accounts-range set-state))
          :on-resume
          (fn [_ _ set-state]
            (get-accounts-range set-state))})

;; -- Blocks

(defn BlocksTable [blocks & [{:keys [modal?]}]]
  (let [sorting-ref (reagent/atom {:keyfn :convex-web.block/index
                                   :ascending? false})]
    (fn [blocks & [{:keys [modal?]}]]
      (let [{:keys [keyfn ascending?]} @sorting-ref

            {:keys [SortIcon comparator]} (if ascending?
                                            {:SortIcon gui/SortAscendingIcon
                                             :comparator #(compare %1 %2)}
                                            {:SortIcon gui/SortDescendingIcon
                                             :comparator #(compare %2 %1)})

            blocks (sort-by keyfn comparator blocks)

            SortableColumn (fn [{keyfn' :keyfn label :label}]
                             [:div.flex.p-2.border-b.border-gray-300.hover:bg-gray-300.cursor-pointer
                              {:on-click #(reset! sorting-ref {:keyfn keyfn'
                                                               :ascending? (not ascending?)})}
                              [:span label]

                              [SortIcon {:class ["w-4 h-4 ml-1" (when-not (= keyfn' keyfn)
                                                                  "invisible")]}]])]
        [:table.text-left.table-auto
         [:thead
          (let [th-class "text-xs uppercase text-gray-600 bg-gray-100 p-0 sticky top-0"]
            [:tr.select-none
             [:th {:class th-class}
              [SortableColumn
               {:label "Index"
                :keyfn :convex-web.block/index}]]

             [:th
              {:class th-class}
              [SortableColumn
               {:label "Timestamp"
                :keyfn :convex-web.block/timestamp}]]

             [:th
              {:class th-class}
              [SortableColumn
               {:label "Peer"
                :keyfn :convex-web.block/peer}]]])]

         [:tbody.align-baseline
          (for [{:convex-web.block/keys [index peer timestamp] :as block} blocks]
            (let [td-class ["text-xs text-gray-700 whitespace-no-wrap px-2"]]
              ^{:key index}
              [:tr.hover:bg-gray-100.cursor-default
               ;; -- Index
               [:td {:class td-class}
                [:div.flex.flex-1.justify-end
                 (if modal?
                   [:code.underline.cursor-pointer
                    {:on-click #(stack/push :page.id/block-explorer {:modal? true
                                                                     :state {:convex-web/block {:convex-web.block/index index}}})}
                    index]
                   [:a
                    {:href (rfe/href :route-name/block-explorer {:index index})}
                    [:code.underline index]])

                 ;; External link
                 [:a.ml-2
                  {:href (rfe/href :route-name/block-explorer {:index index})
                   :target "_blank"}
                  [gui/IconExternalLink {:class "w-4 h4 text-gray-600 hover:text-gray-800"}]]]]

               ;; -- Timestamp
               [:td {:class td-class}
                [:div.flex.justify-between
                 [:span.text-xs
                  (-> timestamp
                      (format/date-time-from-millis)
                      (format/date-time-to-string))]]]

               ;; -- Peer
               [:td {:class td-class}
                [:a
                 {:href (rfe/href :route-name/account-explorer {:address peer})}
                 [:code.underline peer]]]]))]]))))

(defn BlocksPage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/blocks]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col.flex-1.items-start.mt-4.mx-10
     [BlocksTable blocks {:modal? modal?}]

     [:div.my-4
      [gui/Tooltip
       "View all Blocks"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/blocks-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(def blocks-page
  #:page {:id :page.id/blocks-explorer
          :title "Latest Blocks"
          :component #'BlocksPage
          :state-spec :explorer.blocks/state-spec
          :on-push #'start-polling-blocks
          :on-pop #'stop-polling-blocks})


;; -- Blocks Range

(defn get-blocks-range [{:keys [start end set-state]}]
  (set-state assoc :ajax/status :ajax.status/pending)

  (backend/GET-blocks-range
    (merge {:handler
            (fn [{:keys [meta convex-web/blocks]}]
              (set-state #(assoc % :ajax/status :ajax.status/success
                                   :convex-web/blocks blocks
                                   :meta meta)))
            :error-handler
            (fn [error]
              (set-state #(assoc % :ajax/status :ajax.status/error
                                   :ajax/error error)))}
           (when start
             {:start start})
           (when end
             {:end end}))))

(defn BlocksRangeNavigation [state set-state]
  (let [{:keys [start end total] :as range} (get state :meta)]
    [gui/RangeNavigation
     (merge range {:on-previous-click
                   (fn []
                     (set-state #(assoc % :ajax/status :ajax.status/pending))

                     (get-blocks-range
                       (merge (explorer/previous-range start) {:set-state set-state})))

                   :on-next-click
                   (fn []
                     (set-state #(assoc % :ajax/status :ajax.status/pending))

                     (get-blocks-range
                       (merge (explorer/next-range end total) {:set-state set-state})))})]))

(defn BlocksRangePage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/blocks] :as state} set-state]
  [:div.flex.flex-col.flex-1.mt-4.mx-10

   ;; -- Pagination
   [BlocksRangeNavigation state set-state]

   ;; -- Body
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.justify-center.items-center
      [gui/Spinner]]

     [BlocksTable blocks {:modal? modal?}])])

(defn get-blocks-on-push [_ _ set-state]
  (get-blocks-range {:set-state set-state}))

(def blocks-range-page
  #:page {:id :page.id/blocks-range-explorer
          :title "All Blocks"
          :component #'BlocksRangePage
          :state-spec :explorer.blocks-range/state-spec
          :on-push #'get-blocks-on-push})


;; -- Peers

(defn PeersPage [_ _ _]
  [:div.flex.flex-1.justify-center.items-center
   [:span.text-4xl "TODO"]])

(def peers-page
  #:page {:id :page.id/peers-explorer
          :title "Peers"
          :component #'PeersPage})


;; -- Transactions

(defn TransactionsRangePage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/blocks] :as state} set-state]
  [:div.flex.flex-col.flex-1.mt-4.mx-10

   ;; -- Pagination
   [BlocksRangeNavigation state set-state]

   ;; -- Body
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.justify-center.items-center
      [gui/Spinner]]

     [TransactionsTable (or blocks []) {:modal? true}])])

(def transactions-range-page
  #:page {:id :page.id/transactions-range-explorer
          :title "All Transactions"
          :component #'TransactionsRangePage
          :state-spec :explorer.blocks-range/state-spec
          :on-push #'get-blocks-on-push})

(defn TransactionsPage [_ {:keys [ajax/status convex-web/blocks]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col.flex-1.mt-4.mx-10
     [TransactionsTable (or blocks [])]

     [:div.my-4
      [gui/Tooltip
       "View all Transactions"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/transactions-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(def transactions-page
  #:page {:id :page.id/transactions-explorer
          :title "Latest Transactions"
          :component #'TransactionsPage
          :state-spec :explorer.blocks/state-spec
          :on-push #'start-polling-blocks
          :on-pop #'stop-polling-blocks})

(defn ExplorerPage [_ state _]
  [markdown/Markdown (assoc-in state [:markdown :toc?] false)])

(def explorer-page
  #:page {:id :page.id/explorer
          :title "Explorer"
          :component #'ExplorerPage
          :on-push (markdown/hook-fn :explorer)})