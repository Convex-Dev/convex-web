(ns convex-web.site.repl
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.command :as command]
            [convex-web.site.session :as session]
            [convex-web.site.stack :as stack]
            [convex-web.site.backend :as backend]
            [convex-web.site.format :as format]

            [clojure.string :as str]
            [cljs.spec.alpha :as s]

            [fipp.clojure :as fipp]
            [codemirror-reagent.core :as codemirror]
            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]))

(defn toggle-examples [state]
  (update state :convex-web.repl/show-examples? not))

(defn show-examples? [state]
  (:convex-web.repl/show-examples? state))

(defn mode [state]
  (:convex-web.repl/mode state))

(defn set-query-mode [state]
  (assoc state :convex-web.repl/mode :convex-web.command.mode/query))

(defn set-transaction-mode [state]
  (assoc state :convex-web.repl/mode :convex-web.command.mode/transaction))

(defn update-command [state {:convex-web.command/keys [id] :as command}]
  (assoc-in state [:convex-web.repl/commands-by-id id] command))

(defn commands
  "Returns a collection of REPL Commands sorted by ID."
  [state]
  (->> (vals (:convex-web.repl/commands-by-id state))
       (sort-by :convex-web.command/id)))

(defn query?
  "Query Command?"
  [command]
  (= :convex-web.command.mode/query (:convex-web.command/mode command)))

(defn transaction?
  "Transaction Command?"
  [command]
  (= :convex-web.command.mode/transaction (:convex-web.command/mode command)))

(defn running?
  "Status is running?"
  [command]
  (= :convex-web.command.status/running (:convex-web.command/status command)))

;; ---


(defn Examples []
  (let [Title (fn [title]
                [:span.text-sm title])

        make-example (fn [& form]
                       (->> form
                            (map #(with-out-str (fipp/pprint % {:width 50})))
                            (str/join "\n")))]
    [:div.flex.flex-col.flex-1.px-1.overflow-auto
     (let [example (make-example '*balance*)]
       [:div.flex.flex-col.flex-1
        [:div.flex.justify-between.items-center
         [Title "Self Balance"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example '*address*)]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Self Address"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(balance "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f"))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Check Balance"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(transfer "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f" 1000))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Make a Transfer"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(def my-library-address
                        (deploy
                          '(defn identity [x]
                             x)))

                     '(import my-library-address :as my-library)

                     '(my-library/identity "Hello, world!"))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Library"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(def storage-example-address
                        (deploy
                          '(do
                             (def stored-data nil)

                             (defn get []
                               stored-data)

                             (defn set [x]
                               (def stored-data x))

                             (export get set)))))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Simple Storage Actor"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(def storage-example-address
                        (deploy
                          '(do
                             (def stored-data nil)

                             (defn get []
                               stored-data)

                             (defn set [x]
                               (def stored-data x))

                             (export get set))))

                     '(call storage-example-address (set 1))
                     '(call storage-example-address (get)))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center
         [Title "Call Actor"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])

     [:hr.my-2]

     (let [example (make-example
                     '(deploy
                        '(do
                           (def owner *caller*)

                           (defn contract-transfer [receiver amount]
                             (assert (= owner *caller*))
                             (transfer receiver amount))

                           (defn contract-balance []
                             *balance*)

                           (export contract-transfer contract-balance))))]
       [:div.flex.flex-col.flex-1.py-2
        [:div.flex.justify-between.items-center.mt-4
         [Title "Subcurrency Actor"]
         [gui/ClipboardCopy example]]

        [gui/Highlight example]])]))

(defn Reference [reference]
  [:div.flex.flex-col.overflow-auto
   (for [metadata reference]
     (let [symbol (get-in metadata [:doc :symbol])]
       ^{:key symbol}
       [:<>
        [gui/SymbolMeta2 metadata]

        [:hr.my-2]]))])

(defn Input [state set-state]
  ;; `source-ref` is a regular Atom
  ;; because the component doesn't need
  ;; to update when the Atom's value changes.
  (reagent/with-let [editor-ref (atom nil)
                     source-ref (atom "")]
    (let [active-address (session/?active-address)

          execute (fn []
                    (when-let [editor @editor-ref]
                      (let [source (codemirror/cm-get-value editor)

                            transaction #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                                                  :source source}

                            query #:convex-web.query {:source source}

                            command (merge #:convex-web.command {:mode (mode state)}
                                           (case (mode state)
                                             :convex-web.command.mode/query
                                             (merge #:convex-web.command {:query query}
                                                    ;; Address is optional in query mode.
                                                    (when active-address
                                                      #:convex-web.command {:address active-address}))

                                             :convex-web.command.mode/transaction
                                             #:convex-web.command {:address active-address
                                                                   :transaction transaction}))]

                        (when-not (str/blank? (codemirror/cm-get-value editor))
                          (codemirror/cm-set-value editor "")

                          (command/execute command (fn [command command']
                                                     (set-state #(update-command % (merge command command')))))))))

          input-disabled? (and (nil? active-address) (= :convex-web.command.mode/transaction (mode state)))]
      (if input-disabled?
        [:div.bg-gray-200.rounded.flex.items-center.justify-center
         {:style
          {:height "100px"}}
         [gui/Tooltip
          {:title "You need an Account to use transactions"}
          [gui/DefaultButton
           {:on-click #(stack/push :page.id/create-account {:modal? true})}
           [:span.text-xs.uppercase "Create Account"]]]]
        [:div.flex.border.rounded

         [codemirror/CodeMirror
          [:div.overflow-scroll.flex-shrink-0.flex-1
           {:style
            {:height "100px"}}]
          {:configuration {:lineNumbers false
                           :value @source-ref}

           :on-mount (fn [_ editor]
                       (->> (codemirror/extra-keys {:shift-enter execute})
                            (codemirror/set-extra-keys editor))

                       (reset! editor-ref editor)

                       (codemirror/cm-focus editor))
           :on-update (fn [_ editor]
                        (->> (codemirror/extra-keys {:shift-enter execute})
                             (codemirror/set-extra-keys editor))

                        (codemirror/cm-focus editor))

           :events {:editor {"change" (fn [editor _]
                                        (reset! source-ref (codemirror/cm-get-value editor)))}}}]

         [:div.flex.flex-col.justify-center.p-1.bg-gray-100
          [gui/Tooltip
           "Run"
           [gui/PlayIcon
            {:class
             ["w-6 h-6"
              "text-green-500"
              "hover:shadow-lg hover:text-green-600 hover:bg-green-100"
              "rounded-full"
              "cursor-pointer"]
             :on-click execute}]]]]))))

(def output-symbol-metadata-options
  {:show-examples? false})

(defmulti Output (fn [command]
                   (get-in command [:convex-web.command/metadata :doc :type])))

(defmethod Output :default [{:convex-web.command/keys [object]}]
  [gui/Highlight object])

(defmethod Output :nil [_]
  [:pre.bg-white.m-0.p-2.rounded.shadow
   [:code.text-xs "nil"]])

(defmethod Output :function [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :special [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :macro [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :blob [{:convex-web.command/keys [metadata]}]
  (let [{:keys [length hex-string]} metadata]
    [:div.flex.flex-1.bg-white.rounded.shadow
     [:div.flex.flex-col.p-2
      [:span.text-xs.text-indigo-500.uppercase
       "Length"]
      [:code.text-xs
       length]

      [:span.text-xs.text-indigo-500.uppercase.mt-2
       "HEX"]
      [:div.flex
       [:code.text-xs.mr-2
        hex-string]

       [gui/ClipboardCopy (str "0x" hex-string)]]]]))

(defmethod Output :address [{:convex-web.command/keys [object]}]
  (reagent/with-let [account-ref (reagent/atom nil)

                     _ (backend/GET-account object {:handler
                                                    (fn [account]
                                                      (reset! account-ref account))})]
    [:div.flex.flex-col.bg-white.rounded.shadow.p-2
     [:span.text-xs.text-indigo-500.uppercase "Address"]
     [:div.flex.items-center
      [:a.hover:underline.mr-2
       {:href (rfe/href :route-name/account-explorer {:address object})}
       [:code.text-xs (format/address-blob object)]]

      [gui/ClipboardCopy (format/address-blob object)]

      [:a.ml-2
       {:href (rfe/href :route-name/account-explorer {:address object})
        :target "_blank"}
       [gui/Tooltip
        "External link"
        [gui/IconExternalLink {:class "w-4 h-4 text-gray-500 hover:text-black"}]]]]

     [:span.text-xs.text-indigo-500.uppercase.mt-2 "Balance"]
     [:span.text-xs.uppercase (get-in @account-ref [:convex-web.account/status :convex-web.account-status/balance] "-")]

     [:span.text-xs.text-indigo-500.uppercase.mt-2 "Type"]
     [:span.text-xs.uppercase (get-in @account-ref [:convex-web.account/status :convex-web.account-status/type])]]))

(defn Commands [commands]
  [:<>
   (for [{:convex-web.command/keys [id status query transaction error] :as command} commands]
     ^{:key id}
     [:div.w-full.border-b.p-4.transition-colors.duration-500.ease-in-out
      {:ref
       (fn [el]
         (when el
           (.scrollIntoView el #js {"behavior" "smooth"
                                    "block" "center"})))
       :class
       (case status
         :convex-web.command.status/running
         "bg-yellow-100"
         :convex-web.command.status/success
         ""
         :convex-web.command.status/error
         "bg-red-100"

         "")}

      ;; -- Input
      [:div.flex.flex-col.items-start
       [:span.text-xs.uppercase.text-gray-600.block.mb-1
        "Source"]

       (let [source (or (get query :convex-web.query/source)
                        (get transaction :convex-web.transaction/source))]
         [:div.flex.items-center
          [gui/Highlight source]
          [gui/ClipboardCopy source {:margin "ml-2"}]])]

      [:div.my-3]

      ;; -- Output
      [:div.flex.flex-col
       [:div.flex.mb-1
        [:span.text-xs.uppercase.text-gray-600
         "Result"]

        (when-let [type (get-in command [:convex-web.command/metadata :doc :type])]
          [gui/Tooltip
           (str/capitalize (name type))
           [gui/InformationCircleIcon {:class "w-4 h-4 text-black ml-1"}]])]

       (case status
         :convex-web.command.status/running
         [:code.text-yellow-500 "..."]

         :convex-web.command.status/error
         [:pre.bg-white.m-0.p-2.rounded.shadow.border.border-red-500
          [:code.text-xs.text-red-500
           error]]

         :convex-web.command.status/success
         [:div.flex
          [Output command]])]])])

(defn QueryRadio [state set-state]
  [:label
   [:input
    {:type "radio"
     :name "query"
     :value "query"
     :checked (= :convex-web.command.mode/query (mode state))
     :on-change #(set-state set-query-mode)}]

   [:span.text-xs.text-gray-700.ml-1
    "Query"]])

(defn TransactionRadio [state set-state]
  [:label
   [:input
    {:type "radio"
     :name "transaction"
     :value "transaction"
     :checked (= :convex-web.command.mode/transaction (mode state))
     :on-change #(set-state set-transaction-mode)}]

   [:span.text-xs.text-gray-700.ml-1
    "Transaction"]])

(defn SandboxPage [_ {:convex-web.repl/keys [reference] :as state} set-state]
  [:div.flex.flex-1

   ;; -- REPL
   [:div.flex.flex-col.flex-1.my-4.mx-10
    {:style
     {:width "40vw"}}

    ;; -- Commands
    [:div.flex.bg-gray-100.border.rounded.mb-2.overflow-auto
     {:style
      {:height "70vh"}}
     [:div.flex.flex-col.flex-1
      [Commands (commands state)]]]

    ;; -- Input
    [Input state set-state]

    ;; -- Options
    [:div.flex.items-center.justify-between
     [:div.flex.items-center
      [gui/Tooltip
       {:title "Run without changing Convex's state"}
       [QueryRadio state set-state]]

      [:div.mx-1]

      [gui/Tooltip
       {:title "Run and update Convex's state"}
       [TransactionRadio state set-state]]]

     [:span.text-xs.text-gray-700 "Press " [:code "Shift+Return"] " to run."]]]

   ;; -- Sidebar
   (let [selected-tab (get-in state [:convex-web.repl/sidebar :sidebar/tab])]
     [:div.flex.flex-col.ml-16.p-2.border-l
      {:style {:width "420px"}}

      ;; -- Tabs
      [:div.flex.mb-5

       ;; -- Examples Tab
       [:span.text-sm.font-bold.leading-none.uppercase.p-1.cursor-pointer
        {:class
         (if (= :examples selected-tab)
           "border-b border-indigo-500"
           "text-black text-opacity-50")
         :on-click #(set-state assoc-in [:convex-web.repl/sidebar :sidebar/tab] :examples)}
        "Examples"]

       ;; -- Reference Tab
       [:span.text-sm.font-bold.leading-none.uppercase.p-1.cursor-pointer.ml-4
        {:class
         (if (= :reference selected-tab)
           "border-b border-indigo-500"
           "text-black text-opacity-50")
         :on-click #(set-state assoc-in [:convex-web.repl/sidebar :sidebar/tab] :reference)}
        "Reference"]]

      (case selected-tab
        :examples
        [Examples]

        :reference
        [Reference reference])])])

(def sandbox-page
  #:page {:id :page.id/repl
          :title "Sandbox"
          :initial-state #:convex-web.repl {:mode :convex-web.command.mode/transaction
                                            :sidebar {:sidebar/tab :examples}}
          :state-spec (s/keys :req [:convex-web.repl/mode] :opt [:convex-web.repl/commands-by-id])
          :component #'SandboxPage
          :on-push
          (fn [_ _ set-state]
            (backend/GET-reference
              {:handler
               (fn [reference]
                 (set-state assoc :convex-web.repl/reference reference))}))})


