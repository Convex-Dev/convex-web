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
            [convex-web.site.format :as format]

            [cljs.spec.test.alpha :as stest]

            [reagent.dom]
            [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]
            [lambdaisland.glogi.console :as glogi-console]
            [reitit.frontend.easy :as rfe]

            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure" :as hljs-clojure]
            ["highlight.js/lib/languages/javascript" :as hljs-javascript]))


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

   repl/sandbox-page

   ;; ---

   explorer/explorer-page
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

   documentation/documentation-page
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
     {:text "Welcome"
      :top-level? true
      :route-name :route-name/welcome
      :href (rfe/href :route-name/welcome)
      :active? (active #{:route-name/welcome
                         :route-name/create-account})}

     :others
     [{:text "Guides"
       :top-level? true
       :route-name :route-name/documentation
       :href (rfe/href :route-name/documentation)
       :children
       [{:text "Concepts"
         :route-name :route-name/documentation-concepts
         :href (rfe/href :route-name/documentation-concepts)}

        {:text "Getting Started"
         :route-name :route-name/documentation-getting-started
         :href (rfe/href :route-name/documentation-getting-started)}

        {:text "Tutorial"
         :route-name :route-name/documentation-tutorial
         :href (rfe/href :route-name/documentation-tutorial)}

        {:text "Reference"
         :route-name :route-name/documentation-reference
         :href (rfe/href :route-name/documentation-reference)}]}

      {:text "Sandbox"
       :top-level? true
       :route-name :route-name/sandbox
       :href (rfe/href :route-name/sandbox)}

      {:text "Wallet"
       :top-level? true
       :route-name :route-name/wallet
       :href (rfe/href :route-name/wallet)}

      {:text "Explorer"
       :top-level? true
       :route-name :route-name/explorer
       :href (rfe/href :route-name/explorer)
       :children
       (->> [{:text "Accounts"
              :route-name :route-name/accounts-explorer
              :href (rfe/href :route-name/accounts-explorer)
              :active? (active #{:route-name/accounts-explorer
                                 :route-name/account-explorer})}

             {:text "Blocks"
              :route-name :route-name/block-coll-explorer
              :href (rfe/href :route-name/block-coll-explorer)
              :active? (active #{:route-name/block-coll-explorer
                                 :route-name/block-explorer})}

             {:text "Transactions"
              :route-name :route-name/transactions-explorer
              :href (rfe/href :route-name/transactions-explorer)}]
            (sort-by first))}]}))

(defn NavItem [route {:keys [text top-level? active? href target route-name children]}]
  (let [active? (or (= route-name (router/route-name route))
                    (when active?
                      (active? route)))

        leaf? (empty? children)]
    [:div.flex.flex-col.justify-center
     (if leaf?
       {:style
        {:height "40px"}}
       {})
     ;; -- Item
     [:div.flex.justify-between
      [:a.self-start.font-medium.pl-2.border-l-2
       (merge {:href href
               :class [(if top-level?
                         "text-blue-400"
                         "text-black")
                       (if active?
                         "border-blue-400"
                         "border-transparent")]}
              (when target
                {:target target}))

       (if target
         [:div.flex.justify-between.items-center
          [:span.mr-2 text]

          [gui/IconExternalLink {:class "w-6 h-6"}]]
         [:span text])]

      (when-not leaf?
        [gui/IconChevronDown
         {:class
          ["w-6 h-6"
           "text-blue-400"]}])]

     ;; -- Children
     (when (seq children)
       [:div.flex.flex-col.ml-8
        (for [{:keys [text] :as child} children]
          ^{:key text} [NavItem route child])])]))

(defn SideNav [active-route]
  (let [{:keys [others]} (nav)]
    [:nav.flex.flex-col.flex-shrink-0.font-mono.w-64

     (for [{:keys [text] :as item} others]
       ^{:key text}
       [:div.mb-8
        [NavItem active-route item]])]))

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
      (format/address-blob address)])])

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

(defn TopNav []
  [:div.fixed.top-0.inset-x-0.z-100.h-16.border-b.border-gray-100.bg-white
   [:div.w-full.h-full.flex.items-center.justify-between.mx-auto.px-6

    [:a {:href (rfe/href :route-name/welcome)}
     [:div.flex.items-center
      [gui/ConvexLogo {:width "28px" :height "32px"}]
      [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]]

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

     (when (seq (session/?accounts))
       [gui/Tooltip
        {:title "Select Account to use"}
        [SelectAccount]])

     (when-let [active-address (session/?active-address)]
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

     (let [signed-in? (some? (session/?active-address))

           tooltip (if signed-in?
                     "View Session"
                     "Login")

           label (if signed-in?
                   "Session"
                   "Login")]
       [gui/Tooltip
        {:title tooltip}
        [:button
         {:class
          ["text-sm"
           "px-2 py-1"
           "rounded"
           "focus:outline-none"
           "hover:bg-gray-100 hover:shadow-md"]
          :on-click #(stack/push :page.id/session {:modal? true})}
         [:span.text-xs.uppercase label]]])]]])

(defn Scaffolding [{:frame/keys [uuid page state] :as active-page-frame}]
  (let [{Component :page/component} page

        set-state (stack/make-set-state uuid)]
    [:<>
     [TopNav]

     ;; Modal
     ;; ================
     (let [{:frame/keys [modal?] :as frame} (stack/?active-frame)]
       (when modal?
         [Modal frame]))

     ;; Main
     ;; ================
     [:div.w-full.mx-auto.px-6
      [:div.h-screen.flex.pt-24

       ;; -- Nav
       [SideNav (:route/match (router/?route))]

       ;; -- Page
       [:div.relative.flex.flex-col.flex-1.pl-24.overflow-auto
        (when active-page-frame
          [Component active-page-frame state set-state])]]]]))

(defn Page [{:frame/keys [uuid page state] :as active-page-frame}]
  (let [{Component :page/component} page

        set-state (stack/make-set-state uuid)]
    [Component active-page-frame state set-state]))

(defn Root []
  [:<>

   ;; Site
   ;; ================
   (when-let [active-page-frame (stack/?active-page-frame)]
     ;; Scaffolding adds a top and side nav interface - it's like a wrapper for the page.
     ;; Same pages might not want/need the scaffolding, e.g.: Welcome, in that case the page
     ;; will be rendered on its own.
     ;;
     ;; Scaffolding is enabled by default, but it can be explicitly set in the page metadata.
     (if (get-in active-page-frame [:frame/page :page/scaffolding?] true)
       [Scaffolding active-page-frame]
       [Page active-page-frame]))


   ;; Devtools
   ;; ================
   [:div.fixed.bottom-0.right-0.flex.items-center.mr-4.mb-4.p-2.shadow-md.rounded.bg-yellow-300.z-50

    [gui/IconAdjustments
     (let [bg-color (if (and (sub :devtools/?valid-db?) (sub :devtools/?stack-state-valid?))
                      "text-green-500"
                      "text-red-500")]
       {:class [bg-color "w-6 h-6"]})]

    [:input.ml-2 {:type "checkbox"
                  :checked (sub :devtools/?enabled?)
                  :on-change #(disp :devtools/!toggle)}]]

   (when (sub :devtools/?enabled?)
     [devtools/Inspect])])

(defn mount []
  (reagent.dom/render [Root] (.getElementById js/document "app")))

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
  (.registerLanguage hljs "javascript" hljs-javascript)

  (re-frame/dispatch-sync [::!init (.-value (.getElementById js/document "__anti-forgery-token"))])

  (start))