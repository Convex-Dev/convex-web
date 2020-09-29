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
            [convex-web.site.environment :as environment]

            [clojure.string :as str]
            [cljs.spec.test.alpha :as stest]

            [reagent.dom]
            [reagent.core :as reagent]
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

   explorer/code-page
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
   explorer/transaction-page

   ;; ---

   session/session-page

   ;; ---

   documentation/under-construction-page
   documentation/documentation-page
   documentation/reference-page
   documentation/tutorial-page
   documentation/getting-started-page
   documentation/concepts-page
   documentation/faq-page
   documentation/white-paper-page
   documentation/about-page
   documentation/vision-page
   documentation/advanced-topics-page
   documentation/client-api-page

   ;; ---

   environment/entry-page

   ;; ---

   wallet/wallet-page])

;; ---

(re-frame/reg-event-fx ::!init
  (fn [{:keys [db]} [_ csrf-token]]
    {:db (merge db #:site {:pages pages
                           :runtime {:runtime/csrf-token csrf-token}})

     :runtime.fx/do session/initialize}))

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
     [;; Guides
      ;; ==============
      {:text "Documentation"
       :top-level? true
       :route-name :route-name/documentation
       :href (rfe/href :route-name/documentation)
       :children
       [{:text "Getting Started"
         :route-name :route-name/documentation-getting-started
         :href (rfe/href :route-name/documentation-getting-started)}

        {:text "Lisp Guide"
         :route-name :route-name/documentation-tutorial
         :href (rfe/href :route-name/documentation-tutorial)}

        {:text "Advanced Topics"
         :route-name :route-name/advanced-topics
         :href (rfe/href :route-name/advanced-topics)}

        {:text "Reference"
         :route-name :route-name/documentation-reference
         :href (rfe/href :route-name/documentation-reference)}

        {:text "Client API"
         :route-name :route-name/client-api
         :href (rfe/href :route-name/client-api)}]}


      ;; Sandbox
      ;; ==============
      {:text "Sandbox"
       :top-level? true
       :route-name :route-name/sandbox
       :href (rfe/href :route-name/sandbox)}


      ;; Tools
      ;; ==============
      {:text "Tools"
       :top-level? true
       :route-name :route-name/tools
       :href (rfe/href :route-name/tools)
       :children
       [{:text "Wallet"
         :route-name :route-name/wallet
         :href (rfe/href :route-name/wallet)}

        {:text "Faucet"
         :route-name :route-name/faucet
         :href (rfe/href :route-name/faucet)}

        {:text "Transfer"
         :route-name :route-name/transfer
         :href (rfe/href :route-name/transfer)}]}


      ;; Explorer
      ;; ==============
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
              :route-name :route-name/blocks
              :href (rfe/href :route-name/blocks)
              :active? (active #{:route-name/blocks
                                 :route-name/block-explorer})}

             {:text "Transactions"
              :route-name :route-name/transactions
              :href (rfe/href :route-name/transactions)}]
            (sort-by first))}


      ;; About
      ;; ==============
      {:text "About"
       :top-level? true
       :route-name :route-name/about
       :href (rfe/href :route-name/about)
       :children
       [#_{:text "Vision"
           :route-name :route-name/vision
           :href (rfe/href :route-name/vision)}

        {:text "FAQ"
         :route-name :route-name/faq
         :href (rfe/href :route-name/faq)}

        #_{:text "Concepts"
           :route-name :route-name/concepts
           :href (rfe/href :route-name/concepts)}

        #_{:text "White Paper"
           :route-name :route-name/white-paper
           :href (rfe/href :route-name/white-paper)}

        #_{:text "Get Involved"
           :route-name :route-name/get-involved
           :href (rfe/href :route-name/get-involved)}

        #_{:text "Roadmap"
           :route-name :route-name/roadmap
           :href (rfe/href :route-name/roadmap)}

        #_{:text "Convex Foundation"
           :route-name :route-name/convex-foundation
           :href (rfe/href :route-name/convex-foundation)}]}]}))

(defn NavItem [route {:keys [text top-level? active? href target route-name children]}]
  (let [active? (or (= route-name (router/route-name route))
                    (when active?
                      (active? route)))

        leaf? (empty? children)]
    [:div.flex.flex-col.justify-center
     (if leaf?
       {:class "h-6 xl:h-8"}
       {})
     ;; -- Item
     [:div.flex.justify-between
      [:a.self-start.font-medium.pl-2.border-l-2
       (merge {:href href
               :class [(if top-level?
                         "text-blue-500"
                         "text-gray-black")
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

      #_(when-not leaf?
          [gui/IconChevronDown
           {:class
            ["w-6 h-6"
             "text-blue-400"]}])]

     ;; -- Children
     (when (seq children)
       [:div.flex.flex-col.ml-4.xl:ml-8
        (for [{:keys [text] :as child} children]
          ^{:key text} [NavItem route child])])]))

(defn SideNav [active-route]
  (let [{:keys [others]} (nav)]
    [:nav.flex.flex-col.flex-shrink-0.font-mono.mr-4.overflow-auto

     (for [{:keys [text] :as item} others]
       ^{:key text}
       [:div.mb-2.xl:mb-6
        [NavItem active-route item]])]))

(defn Modal [{:frame/keys [uuid page state] :as frame}]
  (let [set-state (stack/make-set-state uuid)

        {:page/keys [title component]} page]

    [:div {:class "fixed flex justify-center z-10 inset-0 overflow-y-auto"}

     ;; -- Background
     [:div.fixed.inset-0.transition-opacity
      [:div.absolute.inset-0.bg-gray-500.opacity-75]]

     ;; -- Content
     [:div
      {:class "inline-block px-4 pt-20 pb-4 transform transition-all"}

      [:div.flex.flex-col.flex-1.max-w-screen-md.xl:max-w-screen-xl.rounded-lg.shadow-2xl.bg-white.border

       ;; -- Header
       [:div.bg-blue-100.bg-opacity-25.border-b.rounded-t-lg
        [:div.h-20.relative.flex.justify-between.items-center.px-6

         [:span.font-mono.text-lg.leading-none title]

         [gui/Tooltip
          {:title "Close"}
          [gui/IconXCircle
           {:class
            ["w-6 h-6"
             "text-gray-600 hover:text-gray-700"
             "cursor-pointer"]
            :on-click #(stack/pop)}]]]]

       ;; -- Body
       [:div.flex.flex-1.overflow-auto
        [component frame state set-state]]]]]))

;; TODO This should be extracted into a "generic" component, so it can be used in other parts of the site.
(defn AccountSelect []
  (let [*state (reagent/atom {:show? false})]
    (fn []
      (let [{:keys [show? selected]} @*state

            selected (or selected (session/?active-address))

            item-style ["inline-flex w-full h-16 relative py-2 pl-3 pr-9"
                        "cursor-default select-none"
                        "text-gray-900 text-xs"
                        "hover:bg-blue-100 hover:bg-opacity-50 active:bg-blue-200"]]
        [:div.space-y-1

         [:div.relative

          ;; Selected
          [:span.inline-block.w-full
           {:on-click #(swap! *state update :show? not)}
           [:button.cursor-default.relative.w-full.rounded-md.bg-white.pr-9.text-left.focus:outline-none.focus:shadow-outline-blue.focus:border-blue-300.transition.ease-in-out.duration-150.sm:text-sm.sm:leading-5 {:type "button" :aria-haspopup "listbox" :aria-expanded "true" :aria-labelledby "listbox-label"}

            [gui/Identicon {:value selected :size 40}]

            [:span.absolute.inset-y-0.right-0.flex.items-center.pr-2.pointer-events-none
             [:svg.h-5.w-5.text-gray-400 {:viewBox "0 0 20 20" :fill "none" :stroke "currentColor"}
              [:path {:d "M7 7l3-3 3 3m0 6l-3 3-3-3" :stroke-width "1.5" :stroke-linecap "round" :stroke-linejoin "round"}]]]]]

          [gui/Transition
           (merge gui/dropdown-transition {:show? show?})
           [gui/Dismissible
            {:on-dismiss #(swap! *state update :show? (constantly false))}
            [:div.origin-top-right.absolute.right-0.mt-2.rounded-md.shadow-lg.bg-white
             [:ul.max-h-60.rounded-md.py-1.text-base.leading-6.shadow-xs.overflow-auto.focus:outline-none.sm:text-sm.sm:leading-5

              ;; -- Create Account
              [:li
               {:class item-style
                :on-click
                #(do
                   (reset! *state {:show? false})

                   (stack/push :page.id/create-account {:modal? true}))}

               [:div.flex.items-center
                [:div.h-5.w-5.mr-2
                 [gui/PlusIcon {:class "h-5 w-5"}]]

                [:span.font-mono.text-base.block
                 "Create Account"]]]

              ;; -- Accounts
              (for [{:convex-web.account/keys [address]} (session/?accounts)]
                ^{:key address}
                [:li
                 {:class item-style
                  :on-click
                  #(do
                     (reset! *state {:show? false :selected address})

                     (session/pick-address address))}

                 [:div.flex.items-center
                  [:div.h-5.w-5.mr-2
                   (when (= address selected)
                     [gui/CheckIcon {:class "h-5 w-5"}])]

                  [gui/Identicon {:value address :size 40}]

                  [:span.font-mono.block.ml-2
                   (format/prefix-0x address)]]])]]]]]]))))


(defn TopNav []
  (let [link-style "font-mono text-gray-800 hover:text-gray-500 active:text-black"]
    [:div.fixed.top-0.inset-x-0.h-16.border-b.border-gray-100.bg-white.z-10
     [:div.w-full.h-full.flex.items-center.justify-between.mx-auto.px-10

      ;; Logo
      ;; ===================
      [:a {:href (rfe/href :route-name/welcome)}
       [:div.flex.items-center
        [gui/ConvexLogo {:width "28px" :height "32px"}]
        [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]]

      ;; Items
      ;; ===================
      [:div.flex.items-center.justify-end.space-x-8

       (cond
         (session/?active-address)
         [:<>
          ;; -- Wallet
          [:a
           {:href (rfe/href :route-name/wallet)}
           [:span {:class link-style}
            "Wallet"]]

          ;; -- Faucet
          [:a
           {:href (rfe/href :route-name/faucet)}
           [:span {:class link-style}
            "Faucet"]]

          ;; -- Transfer
          [:a
           {:href (rfe/href :route-name/transfer)}
           [:span {:class link-style}
            "Transfer"]]

          ;; -- Details
          [:a
           {:href (rfe/href :route-name/my-account)}
           [:span {:class link-style}
            "Details"]]

          ;; -- Select account
          [AccountSelect]]

         (= :ajax.status/pending (session/?status))
         [gui/Spinner]

         :else
         ;; -- Create Account
         [gui/PrimaryButton
          {:on-click #(stack/push :page.id/create-account {:modal? true})}
          [:span.block.font-mono.text-sm.text-white.uppercase
           {:class gui/button-child-small-padding}
           "Create Account"]])]]]))

(defn Scaffolding [{:frame/keys [uuid page state] :as active-page-frame}]
  (let [{Component :page/component
         title :page/title
         description :page/description
         style :page/style} page

        set-state (stack/make-set-state uuid)]
    [:<>
     [TopNav]

     ;; Main
     ;; ================
     [:div.w-full.mx-auto.px-6
      [:div.h-screen.flex.pt-24

       ;; -- Nav
       [SideNav (:route/match (router/?route))]

       ;; -- Page
       [:div.relative.flex.flex-col.flex-1.xl:pl-24.space-y-4.overflow-auto
        (when active-page-frame
          [:<>

           [:div.flex.flex-col.space-y-4
            (when title
              (let [title-size (case (get style :page-style/title-size)
                                 :large "text-3xl"
                                 :small "text-base"
                                 ;; Default
                                 "text-3xl")]
                [:span.font-mono.text-gray-900
                 {:class [title-size "leading-none"]}
                 title]))

            (when description
              [:p.text-gray-500.text-base.max-w-screen-sm description])

            (when title
              [:div.w-32.h-2.bg-blue-500.mb-8])]

           [Component active-page-frame state set-state]])]]]]))

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


   ;; Modal
   ;; ================
   (let [{:frame/keys [modal?] :as frame} (stack/?active-frame)]
     (when modal?
       [Modal frame]))


   ;; Devtools
   ;; ================
   (when goog.DEBUG
     [:div.fixed.bottom-0.right-0.flex.items-center.mr-4.mb-4.p-2.shadow-md.rounded.bg-yellow-300.z-50

      [gui/IconAdjustments
       (let [bg-color (if (and (sub :devtools/?valid-db?) (sub :devtools/?stack-state-valid?))
                        "text-green-500"
                        "text-red-500")]
         {:class [bg-color "w-6 h-6"]})]

      [:input.ml-2 {:type "checkbox"
                    :checked (sub :devtools/?enabled?)
                    :on-change #(disp :devtools/!toggle)}]])

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