(ns convex-web.site.app
  (:require [convex-web.site.router :as router]
            [convex-web.site.devtools :as devtools]
            [convex-web.site.db]
            [convex-web.site.stack :as stack]
            [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.wallet :as wallet]
            [convex-web.site.explorer :as explorer]
            [convex-web.site.documentation :as documentation]
            [convex-web.site.welcome :as welcome]
            [convex-web.site.repl :as repl]
            [convex-web.site.session :as session]
            [convex-web.site.account :as account]
            [convex-web.site.store]

            [cljs.spec.test.alpha :as stest]

            [reagent.dom]
            [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]
            [lambdaisland.glogi.console :as glogi-console]
            [reitit.frontend.easy :as rfe]

            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure" :as hljs-clojure]))


(glogi-console/install!)

(log/set-levels {:glogi/root :debug})

;; ---

(defn BlankPage [_ _ _]
  [:div])

(def blank-page
  #:page {:id :page.id/blank
          :component #'BlankPage})

(def pages
  [blank-page
   ;; ---

   welcome/welcome-page

   ;; ---

   account/my-account-page
   account/create-account-page
   account/transfer-page
   account/faucet-page

   ;; ---

   repl/repl-page

   ;; ---

   explorer/accounts-page
   explorer/accounts-range-page
   explorer/account-page
   explorer/blocks-page
   explorer/blocks-range-page
   explorer/block-page
   explorer/peers-page
   explorer/transactions-page
   explorer/transactions-range-page

   ;; ---

   session/session-page

   ;; ---

   documentation/reference-page
   documentation/tutorial-page
   documentation/getting-started-page
   documentation/concepts-page

   ;; ---

   wallet/wallet-page])

;; ---

(re-frame/reg-event-db ::!init
  (fn [db [_ csrf-token]]
    (merge db #:site {:pages pages
                      :runtime {:runtime/csrf-token csrf-token}})))

;; ---

(defn nav []
  (let [active (fn [routes-set]
                 (comp routes-set router/route-name))]
    {:welcome
     ["Welcome"
      {:route-name :route-name/welcome
       :href (rfe/href :route-name/welcome)
       :active? (active #{:route-name/welcome
                          :route-name/create-account})}]

     :others
     [["Explorer"
       (->> [["Accounts"
              {:route-name :route-name/accounts-explorer
               :href (rfe/href :route-name/accounts-explorer)
               :active? (active #{:route-name/accounts-explorer
                                  :route-name/account-explorer})}]

             ["Blocks"
              {:route-name :route-name/block-coll-explorer
               :href (rfe/href :route-name/block-coll-explorer)
               :active? (active #{:route-name/block-coll-explorer
                                  :route-name/block-explorer})}]

             ["Transactions"
              {:route-name :route-name/transactions-explorer
               :href (rfe/href :route-name/transactions-explorer)}]

             ;; After initial release
             #_["Peers"
                {:route-name :route-name/peers-explorer
                 :href (rfe/href :route-name/peers-explorer)}]]
            (sort-by first))]

      ["REPL"
       {:route-name :route-name/repl
        :href (rfe/href :route-name/repl)}]

      ["Wallet"
       {:route-name :route-name/wallet
        :href (rfe/href :route-name/wallet)}]

      ["Documentation"
       [["Concepts"
         {:route-name :route-name/documentation-concepts
          :href (rfe/href :route-name/documentation-concepts)}]

        ["Getting Started"
         {:route-name :route-name/documentation-getting-started
          :href (rfe/href :route-name/documentation-getting-started)}]

        ["Tutorial"
         {:route-name :route-name/documentation-tutorial
          :href (rfe/href :route-name/documentation-tutorial)}]

        ["Reference"
         {:route-name :route-name/documentation-reference
          :href (rfe/href :route-name/documentation-reference)}]]]]}))

(defn NavItem [route [label attributes-or-children]]
  (let [attributes? (map? attributes-or-children)
        children? (sequential? attributes-or-children)]

    (cond
      attributes?
      (let [{:keys [active? href target route-name]} attributes-or-children

            active? (or (= route-name (router/route-name route))
                        (when active?
                          (active? route)))]
        [:a.self-start.hover:text-black.font-medium.mb-1.pl-2.border-l-2
         (merge {:href href
                 :class (if active?
                          "border-indigo-600 text-black"
                          "border-transparent text-gray-600")}
                (when target
                  {:target target}))

         (if target
           [:div.flex.justify-between.items-center
            [:span.mr-2 label]

            [gui/IconExternalLink {:class "w-6 h-6"}]]
           [:span label])])

      children?
      [:div.flex.flex-col.mt-2
       [:span.px-2.py-1.mb-1.text-gray-600 label]
       [:div.flex.flex-col.ml-2.items-start
        (for [[label _ :as child] attributes-or-children]
          ^{:key label} [NavItem route child])]])))

(defn Nav [active-route]
  (let [{:keys [welcome others]} (nav)]
    [:nav.bg-gray-100.flex.flex-col.pt-8.px-6.border-r {:class "w-1/6"}

     [:div.mb-6
      [NavItem active-route welcome]]

     (for [[label _ :as item] others]
       ^{:key label}
       [NavItem active-route item])]))

(defn SelectAccount []
  [:select
   {:class
    ["text-sm"
     "p-1"
     "rounded"
     "focus:outline-none"
     "hover:bg-gray-100 hover:shadow-md"]
    :value (or (session/?active-address) "")
    :on-change
    (fn [event]
      (let [value (.-value (.-target event))]
        (session/pick-address value)))}
   (for [{:convex-web.account/keys [address]} (session/?accounts)]
     ^{:key address}
     [:option {:value address}
      address])])

(defn Modal [{:frame/keys [uuid page state] :as frame}]
  (let [set-state (stack/make-set-state uuid)

        {:page/keys [title component]} page]
    [:div.flex.justify-center.items-stretch.fixed.top-0.left-0.w-screen.h-screen.py-32.z-10
     {:style {:background-color "rgba(0,0,0,0.1"}}

     [:div.flex.flex-col.w-full.max-w-screen-md.rounded-lg.shadow-2xl.bg-white.border

      ;; -- Header
      [:div.border-b.border-gray-400.bg-gray-100.rounded-t-lg
       [:div.relative.flex.justify-between.p-4

        [:span.font-rubik.text-lg.leading-none.uppercase title]

        [gui/Tooltip
         {:title "Close"}
         [gui/IconXCircle
          {:class
           ["w-6 h-6"
            "absolute top-0 right-0"
            "mt-2 mr-2"
            "text-gray-600 hover:text-gray-700"
            "cursor-pointer"]
           :on-click #(stack/pop)}]]]]

      ;; -- Body
      [:div.flex.flex-1.overflow-auto
       [component frame state set-state]]]]))

(defn App []
  (let [{:frame/keys [uuid page state] :as active-page-frame} (stack/?active-page-frame)

        {page-title :page/title page-component :page/component} page

        set-state (stack/make-set-state uuid)]
    [:<>

     ;; -- Modal
     (let [{:frame/keys [modal?] :as frame} (stack/?active-frame)]
       (when modal?
         [Modal frame]))


     [:div.h-screen.flex

      ;; -- Nav
      [Nav (:route/match (router/?route))]

      [:div.flex.flex-col {:class "w-5/6"}

       ;; -- Account
       [:div.flex.items-center.justify-between.py-2.px-4.border-b.bg-white

        [:span.font-rubik.text-lg.leading-none.uppercase page-title]

        [:div.flex.items-center.justify-end
         [gui/Tooltip
          {:title "Create a new Account"}
          [:button
           {:class
            ["text-sm"
             "px-2 py-1"
             "mr-6"
             "rounded"
             "focus:outline-none"
             "hover:bg-gray-100 hover:shadow-md"]
            :on-click #(stack/push :page.id/create-account {:modal? true})}
           [:span.text-xs.uppercase "Create Account"]]]

         [gui/Tooltip
          {:title "Select Account to use"}
          [SelectAccount]]

         (let [active-address (session/?active-address)]
           [gui/Tooltip
            {:title "Account details"}
            [:button.focus:outline-none.text-gray-700.hover:text-black.mx-4.w-6.h-6.rounded
             (merge {:on-click #(stack/push :page.id/my-account {:modal? true
                                                                 :state
                                                                 {:convex-web/account
                                                                  {:convex-web.account/address active-address}}})}
                    (when-not active-address
                      {:class "text-gray-400 pointer-events-none"}))

             [gui/IconUser]]])

         [gui/Tooltip
          {:title "View Session"}
          [:button
           {:class
            ["text-sm"
             "px-2 py-1"
             "rounded"
             "focus:outline-none"
             "hover:bg-gray-100 hover:shadow-md"]
            :on-click #(stack/push :page.id/session {:modal? true})}
           [:span.text-xs.uppercase "Session"]]]]]

       ;; -- Page
       [:div.relative.flex.flex-1.overflow-auto
        (when active-page-frame
          [page-component active-page-frame state set-state])]]]


     ;; -- Devtools
     [:div.fixed.bottom-0.right-0.flex.items-center.mr-4.mb-4.p-2.shadow-md.rounded.bg-yellow-400.z-50

      [gui/IconAdjustments
       (let [bg-color (if (and (sub :devtools/?valid-db?) (sub :devtools/?stack-state-valid?))
                        "text-green-500"
                        "text-red-500")]
         {:class [bg-color "w-6 h-6"]})]

      [:input.ml-2 {:type "checkbox"
                    :checked (sub :devtools/?enabled?)
                    :on-change #(disp :devtools/!toggle)}]]

     (when (sub :devtools/?enabled?)
       [devtools/Inspect])]))


;; ---


(defn mount []
  (reagent.dom/render [App] (.getElementById js/document "app")))

(defn start []
  (when goog.DEBUG
    (stest/instrument))

  (router/start)

  (mount))

(defn ^:dev/after-load re-render []
  (re-frame/clear-subscription-cache!)

  (start))

(defn ^:export init []
  (.registerLanguage hljs "clojure" hljs-clojure)

  (re-frame/dispatch-sync [::!init (.-value (.getElementById js/document "__anti-forgery-token"))])

  (start))