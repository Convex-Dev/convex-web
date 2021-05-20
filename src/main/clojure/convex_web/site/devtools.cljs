(ns convex-web.site.devtools
  (:require [convex-web.specs]
            [convex-web.site.stack :as stack]
            [convex-web.site.runtime :refer [sub disp] :as runtime]
            [convex-web.site.gui :as gui]
            [convex-web.site.command :as command]
            [convex-web.site.session :as session]
            [convex-web.site.backend :as backend]

            [cljs.spec.alpha :as s]
            [cljs.pprint :as pprint]

            [codemirror-reagent.core :as codemirror]
            [lambdaisland.glogi :as log]
            [re-frame.core :as re-frame]
            [expound.alpha :as expound]))

(re-frame/reg-sub :devtools/?db
  (fn [db _]
    db))

(re-frame/reg-sub :devtools/?enabled?
  (fn [db _]
    (boolean (get-in db [:site/devtools :devtools/enabled?]))))

(re-frame/reg-event-db :devtools/!toggle
  (fn [db _]
    (update-in db [:site/devtools :devtools/enabled?] not)))

(re-frame/reg-event-db :devtools/!select-panel
  (fn [db [_ panel]]
    (assoc-in db [:site/devtools :devtools/selected-panel] panel)))

(re-frame/reg-sub :devtools/?selected-panel
  (fn [db _]
    (get-in db [:site/devtools :devtools/selected-panel])))

(re-frame/reg-event-db :devtools/!select-tab
  (fn [db [_ tab]]
    (assoc-in db [:site/devtools :devtools/selected-tab] tab)))

(re-frame/reg-sub :devtools/?selected-tab
  (fn [db _]
    (get-in db [:site/devtools :devtools/selected-tab])))

(re-frame/reg-sub :devtools/?valid-db?
  (fn [db _]
    (s/valid? :site/app-db db)))

(re-frame/reg-sub :devtools/?stack-state-check
  :<- [:stack/?stack]
  (fn [stack]
    (reduce
      (fn [check {:frame/keys [uuid page state]}]
        (let [{:page/keys [state-spec]} page]
          (if state-spec
            (assoc check uuid #:devtools.stack-state-check {:frame {:frame/uuid uuid}
                                                            :state state
                                                            :spec state-spec
                                                            :valid? (s/valid? state-spec state)})
            check)))
      {}
      stack)))

(re-frame/reg-sub :devtools/?stack-state-valid?
  :<- [:devtools/?stack-state-check]
  (fn [check]
    (->> (vals check)
         (map :devtools.stack-state-check/valid?)
         (every? true?))))

;; ---

(defn AppDBPanel []
  (let [db (sub :devtools/?db)

        tabs (->> (keys db)
                  (map
                    (fn [k]
                      [(name k) k]))
                  (sort-by first))

        [_ inspect-key] (sub :devtools/?selected-tab)

        Tab (fn [[label k :as tab]]
              [:button.border-b-2.border-yellow-300.focus:outline-none
               {:class (if (= k inspect-key) "border-white" "border-green-500")
                :on-click #(disp :devtools/!select-tab tab)}
               [:code.block.text-xs.p-2.text-white label]])]

    [:div.flex.flex-col.flex-1.max-w-full

     [:div.flex.bg-green-500
      [:button.border-b-2.border-yellow-300.focus:outline-none
       {:class (if (nil? inspect-key) "border-white" "border-green-500")
        :on-click #(disp :devtools/!select-tab nil)}
       [:code.block.text-xs.p-2.text-white.bg-green-500.border-green-500
        "app-db"]]

      (for [[label :as tab] tabs]
        ^{:key label} [Tab tab])]

     ;; -- App DB
     [codemirror/CodeMirror
      [:div.flex-1]
      {:configuration
       {:readOnly true
        :value (with-out-str (pprint/pprint (if inspect-key (get db inspect-key) db)))}

       :on-mount
       (fn [_ editor]
         (->> (codemirror/extra-keys {:shift-enter #(log/debug :db db)})
              (codemirror/set-extra-keys editor)))

       :on-update
       (fn [_ editor]
         nil)}]

     ;; -- Active Frame State
     [:code.block.text-xs.p-2.text-white.font-bold
      {:class "bg-green-500 border-green-500"}
      "State"]
     [codemirror/CodeMirror
      [:div.flex-1]
      {:configuration
       {:readOnly true
        :value (with-out-str (pprint/pprint (stack/?active-frame-state)))}}]

     ;; -- DB & Stack spec check
     (let [valid-db? (sub :devtools/?valid-db?)
           valid-stack-state? (sub :devtools/?stack-state-valid?)
           valid? (and valid-db? valid-stack-state?)]
       (when-not valid?
         [:div.h-40.flex.flex-col.border-t.border-red-500.bg-red-100.overflow-scroll

          (when-not valid-db?
            [:pre.text-xs.text-red-500.p-2.m-0 (expound/expound-str :site/app-db db)])

          [:div.my-2]

          (for [[frame-uuid {:devtools.stack-state-check/keys [spec state valid?]}] (sub :devtools/?stack-state-check)]
            (when-not valid?
              ^{:key frame-uuid}
              [:div.flex.flex-col.text-xs.text-red-500.p-2
               [:code.font-bold.mb-2 (str :frame/uuid " ") frame-uuid]
               [:pre.m-0 (expound/expound-str spec state)]]))]))]))

;; ---

(re-frame/reg-sub :devtools.stress-test/?get
  (fn [db [_ k not-found]]
    (get-in db [:site/devtools :devtools/stress-test k] not-found)))

(re-frame/reg-event-db :devtools.stress-test/!set
  (fn [db [_ k v]]
    (if (nil? v)
      (update-in db [:site/devtools :devtools/stress-test] dissoc k)
      (assoc-in db [:site/devtools :devtools/stress-test k] v))))

(re-frame/reg-event-db :devtools.stress-test/!update-command
  (fn [db [_ {:convex-web.command/keys [id] :as command}]]
    (assoc-in db [:site/devtools :devtools/stress-test :stress-test/commands-by-id id] command)))

(re-frame/reg-event-db :devtools.stress-test/!reset
  (fn [db _]
    (update db :site/devtools dissoc :devtools/stress-test)))

(defn ?command-mode []
  (sub :devtools.stress-test/?get :stress-test/command-mode :convex-web.command.mode/transaction))

(defn set-command-mode [mode]
  (disp :devtools.stress-test/!set :stress-test/command-mode mode))

(defn ?account-address []
  (sub :devtools.stress-test/?get :stress-test/account-address (session/?active-address)))

(defn ?timeout []
  (sub :devtools.stress-test/?get :stress-test/timeout 50))

(defn ?number-of-commands []
  (sub :devtools.stress-test/?get :stress-test/number-of-commands 50))

;; ---

(defn PickMode [mode]
  [:label
   [:input
    {:type "radio"
     :value "query"
     :checked (= mode (?command-mode))
     :on-change #(set-command-mode mode)}]

   [:span.text-xs.text-gray-700.uppercase.ml-1 (name mode)]])

(defn StressTestPanel []
  (let [account-address (?account-address)
        timeout (?timeout)
        number-of-commands (?number-of-commands)]
    [:div.flex.flex-col.flex-1.justify-center.p-4

     [:span.text-xs.uppercase "Account"]

     [gui/Select
      {:options (map :convex-web.account/address (session/?accounts))
       :value account-address
       :on-change #(disp :devtools.stress-test/!set :stress-test/account-address %)}]

     [:div.flex.flex-col.my-2
      [:div.flex.items-center

       ;; -- Mode
       [:div.flex.flex-col.my-2
        [:span.text-xs.uppercase "Mode"]

        [:div.flex
         [PickMode :convex-web.command.mode/query]

         [:div.mx-2]

         [PickMode :convex-web.command.mode/transaction]]]

       ;; -- Number of Commands
       [:div.flex.flex-col.ml-4
        [:span.text-xs.uppercase "Number of Commands"]

        [:input.text-xs.border.rounded.px-1.mr-2
         {:style {:width "80px"}
          :type "number"
          :value number-of-commands
          :on-change #(disp :devtools.stress-test/!set :stress-test/number-of-commands (.-value (.-target %)))}]]

       ;; -- Timeout
       [:div.flex.flex-col.ml-4
        [:span.text-xs.uppercase "Timeout (milliseconds)"]

        [:input.text-xs.border.rounded.px-1.mr-2
         {:style {:width "80px"}
          :type "number"
          :value timeout
          :on-change #(disp :devtools.stress-test/!set :stress-test/timeout (.-value (.-target %)))}]]]


      (let [transaction? (= :convex-web.command.mode/transaction (?command-mode))
            disabled? (and transaction? (nil? account-address))]
        [:<>
         [:button
          {:disabled disabled?
           :class
           ["text-sm"
            "px-2 py-1 mt-2"
            "rounded"
            "focus:outline-none"
            "hover:shadow-md"
            (if disabled?
              "bg-gray-300 pointer-events-none"
              "bg-blue-500")]
           :on-click #(dotimes [n number-of-commands]
                        (runtime/set-timeout
                          (fn []
                            (let [source (str "(inc " n ")")

                                  transaction #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                                                        :source source
                                                                        :language :convex-lisp}

                                  query #:convex-web.query {:source source
                                                            :language :convex-lisp}

                                  mode (?command-mode)

                                  command (merge #:convex-web.command {:mode mode}
                                                 (case mode
                                                   :convex-web.command.mode/query
                                                   (merge {:convex-web.command/query query}
                                                          (when account-address
                                                            {:convex-web.command/address account-address}))

                                                   :convex-web.command.mode/transaction
                                                   #:convex-web.command {:address account-address
                                                                         :transaction transaction}))]
                              (command/execute command (fn [command command']
                                                         (disp :devtools.stress-test/!update-command (merge command command'))))))
                          (* timeout n)))}
          [:span.text-xs.text-white.uppercase
           "Dispatch"]]

         [:span.text-sm.text-red-500
          {:class (if disabled? "visible" "invisible")}
          "You need to select an account to make transactions."]])]

     ;; -- List of Commands
     (let [commands-by-id (sub :devtools.stress-test/?get :stress-test/commands-by-id)]
       [:<>
        [:span.text-xs.uppercase.mt-4 "Output"]

        [:ul.list-none.text-xs.px-2.border.rounded.overflow-auto
         {:style {:height "200px"}}
         (for [{:convex-web.command/keys [id status]} (vals commands-by-id)]
           ^{:key id}
           [:li
            [:code id " " (case status
                            :convex-web.command.status/running
                            [:span.text-yellow-500 (name status)]

                            :convex-web.command.status/success
                            [:span.text-green-500 (name status)]

                            [:span.text-red-500 (name status)])]])]

        [:span.text-xs.uppercase.my-1 "Count: " [:span.font-bold (count commands-by-id)]]])

     [:div.my-2]

     [:button
      {:class
       ["text-sm"
        "px-2 py-1"
        "bg-yellow-400"
        "rounded"
        "focus:outline-none"
        "hover:shadow-md"]
       :on-click #(disp :devtools.stress-test/!reset)}
      [:span.text-xs.uppercase
       "Reset"]]]))


(def default-panel
  {:devtools.panel/id :devtools.panel.id/app-db
   :devtools.panel/component #'AppDBPanel})

(defn InspectTab [{tab-id :id
                   tab-name :name
                   tab-panel :panel}]
  (let [{active-panel-id :devtools.panel/id} (or (sub :devtools/?selected-panel) default-panel)]
    [:button.border-b-2.border-yellow-300.focus:outline-none
     {:class (if (= tab-id active-panel-id) "border-white" "border-green-600")
      :on-click #(disp :devtools/!select-panel #:devtools.panel {:id tab-id
                                                                 :component tab-panel})}
     [:span.block.text-xs.p-2.text-white.font-bold.uppercase
      tab-name]]))

(defn TransactionPanel []
  (let [address (session/?active-address)]
    [:div.flex.flex-col.flex-1.items-center.p-2
     [:span.text-xs.uppercase "Account"]
     [:code.text-xs address]

     [:button
      {:class
       ["text-sm"
        "px-2 py-1 mt-2"
        "rounded"
        "focus:outline-none"
        "hover:shadow-md"
        "bg-blue-500"]
       :on-click
       #(backend/POST-transaction-prepare
          {:address address
           :source "(inc 1)"}

          {:handler
           (fn [{:strs [hash] :as response}]
             (js/console.log response)

             (backend/POST-transaction-submit
               {:address address
                :hash hash
                :sig ""}

               {:handler
                (fn [response']
                  (js/console.log response'))}))})}
      [:span.text-xs.text-white.uppercase
       "Prepare"]]]))

(defn Inspect []
  (let [{Panel :devtools.panel/component} (or (sub :devtools/?selected-panel) default-panel)]
    [:div
     {:class
      ["flex flex-col"
       "fixed top-0 bottom-0 left-0 z-50"
       "mt-4 ml-4 mb-20"
       "w-2/5"
       "border rounded shadow-2xl"
       "border-green-500"]}

     ;; -- Tabs
     [:div.flex.bg-green-600.border-b.border-green-500.px-1
      [InspectTab
       {:id :devtools.panel.id/app-db
        :name "App-DB"
        :panel #'AppDBPanel}]

      [:div.mx-2]

      ;;[InspectTab
      ;; {:id :devtools.panel.id/transaction
      ;;  :name "Transaction"
      ;;  :panel #'TransactionPanel}]
      ;;
      ;;[:div.mx-2]

      [InspectTab
       {:id :devtools.panel.id/stress-test
        :name "Stress Test"
        :panel #'StressTestPanel}]]

     ;; -- Panel
     [:div.flex.flex-1.bg-white.overflow-auto
      [Panel]]]))
