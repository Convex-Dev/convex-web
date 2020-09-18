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

            [codemirror-reagent.core :as codemirror]
            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]))

(defn mode [state]
  (:convex-web.repl/mode state))

(defn language [state]
  (:convex-web.repl/language state))

(defn commands
  "Returns a collection of REPL Commands sorted by ID."
  [state]
  (:convex-web.repl/commands state))

(defn query?
  "Query Command?"
  [command]
  (= :convex-web.command.mode/query (:convex-web.command/mode command)))

(defn transaction?
  "Transaction Command?"
  [command]
  (= :convex-web.command.mode/transaction (:convex-web.command/mode command)))

(defn selected-tab [state]
  (get-in state [:convex-web.repl/sidebar :sidebar/tab]))

;; ---


(def convex-scrypt-examples
  [["Self Balance"
    "_balance_"]

   ["Self Address"
    "_address_"]

   ["Check Balance"
    "balance(\"7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f\")"]

   ["Transfer"
    "transfer(\"7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f\", 1000)"]])

(def convex-lisp-examples
  (let [make-example (fn [& form]
                       (str/join "\n" form))]
    [["Self Balance"
      (make-example '*balance*)]

     ["Self Address"
      (make-example '*address*)]

     ["Check Balance"
      (make-example
        '(balance "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f"))]

     ["Transfer"
      (make-example
        '(transfer "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f" 1000))]

     ["Library"
      (make-example
        '(def my-library-address
           (deploy
             '(defn identity [x]
                x)))

        '(import my-library-address :as my-library)

        '(my-library/identity "Hello, world!"))]

     ["Simple Storage Actor"
      (make-example
        '(def storage-example-address
           (deploy
             '(do
                (def stored-data nil)

                (defn get []
                  stored-data)

                (defn set [x]
                  (def stored-data x))

                (export get set)))))]

     ["Call Actor"
      (make-example
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

     ["Subcurrency Actor"
      (make-example
        '(deploy
           '(do
              (def owner *caller*)

              (defn contract-transfer [receiver amount]
                (assert (= owner *caller*))
                (transfer receiver amount))

              (defn contract-balance []
                *balance*)

              (export contract-transfer contract-balance))))]]))

(defn Examples [language]
  (let [Title (fn [title]
                [:span.text-sm title])

        convex-scrypt? (= :convex-scrypt language)]

    [:div.flex.flex-col.flex-1.pl-1.pr-4.overflow-auto
     (let [examples (if convex-scrypt?
                      convex-scrypt-examples
                      convex-lisp-examples)]
       (map
         (fn [[title source-code]]
           ^{:key title}
           [:div.flex.flex-col.py-2
            [:div.flex.justify-between.items-center
             [Title title]
             [gui/ClipboardCopy source-code]]

            [gui/Highlight source-code {:language language
                                        :pretty? true}]])
         examples))]))

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
                                                                  :source source
                                                                  :language (language state)}

                            query #:convex-web.query {:source source
                                                      :language (language state)}

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

                          (command/execute command (fn [command-previous-state command-new-state]
                                                     (set-state
                                                       (fn [state]
                                                         (let [{:convex-web.command/keys [id] :as command'} (merge command-previous-state command-new-state)

                                                               commands (or (commands state) [])

                                                               ;; Without checking for the ID a Command without an ID
                                                               ;; would be flagged since both values are nil.
                                                               should-update? (when id
                                                                                (some
                                                                                  (fn [{this-id :convex-web.command/id}]
                                                                                    (= id this-id))
                                                                                  commands))

                                                               commands' (if should-update?
                                                                           ;; Map over the existing Commands to update the matching one.
                                                                           (mapv
                                                                             (fn [{this-id :convex-web.command/id :as command}]
                                                                               (if (= id this-id)
                                                                                 (merge command command')
                                                                                 command))
                                                                             commands)
                                                                           ;; Don't need to update so we simply add the Command to the list.
                                                                           (conj commands command'))]

                                                           (assoc state :convex-web.repl/commands commands'))))))))))

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
         (let [enter-extra-key (fn []
                                 (if-let [editor @editor-ref]
                                   (let [^js pos (-> editor
                                                     (codemirror/cm-get-doc)
                                                     (codemirror/cm-get-cursor))

                                         last-line (-> editor
                                                       (codemirror/cm-get-doc)
                                                       (codemirror/cm-last-line))

                                         line (-> editor
                                                  (codemirror/cm-get-doc)
                                                  (codemirror/cm-get-line last-line))

                                         last-line? (= last-line (.-line pos))
                                         last-ch? (= (count line) (.-ch pos))]

                                     (if (and last-line? last-ch?)
                                       (execute)
                                       codemirror/pass))
                                   codemirror/pass))]
           [codemirror/CodeMirror
            [:div.overflow-scroll.flex-shrink-0.flex-1.resize-y
             {:style
              {:height "200px"}}]
            {:configuration {:lineNumbers false
                             :value @source-ref
                             :mode (case (language state)
                                     :convex-lisp
                                     "clojure"

                                     :convex-scrypt
                                     "javascript"

                                     "clojure")}

             :on-mount (fn [_ editor]
                         (->> (codemirror/extra-keys {:enter enter-extra-key
                                                      :shift-enter execute})
                              (codemirror/set-extra-keys editor))

                         (reset! editor-ref editor)

                         (codemirror/cm-focus editor))
             :on-update (fn [_ editor]
                          (->> (codemirror/extra-keys {:enter enter-extra-key
                                                       :shift-enter execute})
                               (codemirror/set-extra-keys editor))

                          (codemirror/cm-focus editor))

             :events {:editor {"change" (fn [editor _]
                                          (reset! source-ref (codemirror/cm-get-value editor)))

                               ;; -- Example of format on paste.
                               ;;"paste" (fn [editor event]
                               ;;          ;; Convex Lisp source if formated on paste.
                               ;;          (when (= :convex-lisp (language state))
                               ;;            (let [source (.getData (.-clipboardData event) "Text")
                               ;;                  source-pretty (zprint/zprint-str source {:parse-string? true})]
                               ;;              (codemirror/cm-set-value editor source-pretty)
                               ;;
                               ;;              (.preventDefault event))))

                               }}}])

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

(defmulti error-message :code)

(defmethod error-message :default
  [{:keys [code message]}]
  (let [code (some-> code
                     (name)
                     (str/capitalize)
                     (str ": "))]
    (str code message)))

(defmethod error-message :STATE
  [{:keys [message]}]
  message)

(defmethod error-message :UNDECLARED
  [{:keys [message]}]
  (str "'" message "' is undeclared."))

(defmethod error-message :CAST
  [{:keys [message]}]
  (str "Cast error: " message "."))

(defn ErrorOutput [{:convex-web.command/keys [error]}]
  [:code.text-xs.text-red-500
   (error-message error)])

(defmulti Output (fn [command]
                   (get-in command [:convex-web.command/metadata :type])))

(defmethod Output :default [{:convex-web.command/keys [object]}]
  (if (some? object)
    [gui/Highlight object]
    [:pre.bg-white.m-0.p-2.rounded.shadow
     [:code.text-xs "nil"]]))

(defmethod Output :string [{:convex-web.command/keys [object]}]
  [gui/Highlight (prn-str object)])

(defmethod Output :function [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :special [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :macro [{:convex-web.command/keys [metadata]}]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [gui/SymbolMeta2 (merge metadata output-symbol-metadata-options)]])

(defmethod Output :blob [{:convex-web.command/keys [object]}]
  (let [{:keys [length hex-string]} object]
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
  (reagent/with-let [account-ref (reagent/atom {:ajax/status :ajax.status/pending})

                     _ (backend/GET-account
                         (get object :checksum-hex)
                         {:handler
                          (fn [account]
                            (reset! account-ref {:account account
                                                 :ajax/status :ajax.status/success}))

                          :error-handler
                          (fn [error]
                            (js/console.error error)
                            (reset! account-ref {:ajax/status :ajax.status/error
                                                 :ajax/error error}))})]
    (let [{:keys [checksum-hex]} object]
      [:div.flex.flex-col.bg-white.rounded.shadow.p-2
       [:span.text-xs.text-indigo-500.uppercase "Address"]
       [:div.flex.items-center
        [:a.hover:underline.mr-2
         {:href (rfe/href :route-name/account-explorer {:address checksum-hex})}
         [:div.flex.items-center

          ;; *Important*
          ;; Display identicon if and only if address is an existing Account.
          (when (= :ajax.status/success (:ajax/status @account-ref))
            [gui/Identicon {:value checksum-hex :size gui/identicon-size-small}])

          [:code.text-xs (format/address-blob checksum-hex)]]]

        [gui/ClipboardCopy (format/address-blob checksum-hex)]

        [:a.ml-2
         {:href (rfe/href :route-name/account-explorer {:address checksum-hex})
          :target "_blank"}
         [gui/Tooltip
          "External link"
          [gui/IconExternalLink {:class "w-4 h-4 text-gray-500 hover:text-black"}]]]]

       (case (:ajax/status @account-ref)
         :ajax.status/pending
         [gui/SpinnerSmall]

         :ajax.status/error
         [:span.text-xs.text-red-500 (get-in @account-ref [:ajax/error :response :error :message])]

         :ajax.status/success
         [:<>
          [:span.text-xs.text-indigo-500.uppercase.mt-2 "Balance"]
          (let [balance (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/balance])]
            [:span.text-xs.uppercase (format/format-number balance)])

          [:span.text-xs.text-indigo-500.uppercase.mt-2 "Type"]
          (let [type (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/type])]
            [:span.text-xs.uppercase type])])])))

(defn Commands [commands]
  (into [:div] (for [{:convex-web.command/keys [id status query transaction] :as command} commands]
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
                      [gui/Highlight source {:pretty? true}]
                      [gui/ClipboardCopy source {:margin "ml-2"}]])]

                  [:div.my-3]

                  ;; -- Output
                  [:div.flex.flex-col
                   (let [type (get-in command [:convex-web.command/metadata :type])]
                     [:div.flex.mb-1
                      [:span.text-xs.uppercase.text-gray-600
                       (cond
                         (= type :error)
                         (str "Error (" (name (get-in command [:convex-web.command/error :code])) ")")

                         :else
                         "Result")]

                      (when (and type (not= :error type))
                        [gui/Tooltip
                         (str/capitalize (name type))
                         [gui/InformationCircleIcon {:class "w-4 h-4 text-black ml-1"}]])])

                   [:div.flex
                    (case status
                      :convex-web.command.status/running
                      [gui/SpinnerSmall]

                      :convex-web.command.status/success
                      [Output command]

                      :convex-web.command.status/error
                      [ErrorOutput command])]]])))

;; --

(defn SandboxPage [_ {:convex-web.repl/keys [reference] :as state} _]
  (let [active-address (session/?active-address)

        ;; It's better if we store our REPL's state somewhere in the db
        ;; that it isn't ephemeral as the frame's state - since we want
        ;; to keep the state between page changes.
        ;; So, we do a simple trick: we create a new version of `state` and `set-state`.
        ;; Since the the new version of `state` and `set-state` has the same interface,
        ;; nothing has to change in any call-site.
        ;;
        ;; Note that REPL's state is per address.
        state (merge state (get-in (session/?state) [:page.id/repl active-address]))
        set-state (fn [f & args]
                    ;; Session's state is shared, so we need to be careful to
                    ;; update only the state of the REPL (& address).
                    (session/set-state (fn [state]
                                         (update-in state [:page.id/repl active-address] (fn [repl-state]
                                                                                           (apply f repl-state args))))))]
    [:div.flex.flex-1.space-x-8.overflow-auto

     ;; -- REPL
     [:div.flex.flex-col {:class "w-3/5"}

      ;; -- Commands
      [:div.flex.bg-gray-100.border.rounded.mb-2.overflow-auto
       {:style
        {:height "70vh"}}
       [:div.flex.flex-col.flex-1
        [Commands (commands state)]]]

      ;; -- Input
      [Input state set-state]

      ;; -- Options
      [:div.flex.items-center.justify-between.mt-1

       [:div.flex
        ;; -- Mode
        [:div.flex.items-center

         [:span.text-xs.text-gray-700.mr-1
          "Mode"]

         [:div.flex.items-center.space-x-1
          [gui/Select2
           {:selected (mode state)
            :options
            [{:id :convex-web.command.mode/transaction
              :value "Transaction"}
             {:id :convex-web.command.mode/query
              :value "Query"}]
            :on-change #(set-state assoc :convex-web.repl/mode %)}]

          [gui/InfoTooltip "Select \"Transaction\" to execute code as a transaction on the Convex Network. Select \"Query\" to execute code just to compute the result (No on-chain effects will be applied)."]]]

        ;; -- Language
        [:div.flex.items-center.ml-6

         [:span.text-xs.text-gray-700.mr-1
          "Language"]

         [:div.flex.items-center.space-x-1
          [gui/Select2
           {:selected (language state)
            :options
            [{:id :convex-scrypt
              :value "Convex Scrypt"}
             {:id :convex-lisp
              :value "Convex Lisp"}]
            :on-change #(set-state assoc :convex-web.repl/language %)}]

          [gui/InfoTooltip "Select the programming language to use."]]]]

       [:span.text-xs.text-gray-700 "Press " [:code "Shift+Return"] " to run."]]]

     ;; -- Sidebar
     (let [selected-tab (selected-tab state)]
       [:div.flex.flex-col.ml-2.xl:ml-16.p-2.border-l
        {:class "w-2/5"}

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
          [Examples (language state)]

          :reference
          [Reference reference])])]))

(def sandbox-page
  #:page {:id :page.id/repl
          :title "Sandbox"
          :initial-state
          {:convex-web.repl/language :convex-lisp
           :convex-web.repl/mode :convex-web.command.mode/transaction
           :convex-web.repl/sidebar {:sidebar/tab :examples}}
          :state-spec (s/keys :req [:convex-web.repl/mode] :opt [:convex-web.repl/commands])
          :component #'SandboxPage
          :on-push
          (fn [_ _ set-state]
            (backend/GET-reference
              {:handler
               (fn [reference]
                 (set-state assoc :convex-web.repl/reference reference))}))})


