(ns convex-web.site.app
  (:require [convex-web.site.router :as router]
            [convex-web.site.devtools :as devtools]
            [convex-web.site.db]
            [convex-web.site.stack :as stack]
            [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.gui.marketing :as marketing]
            [convex-web.site.wallet :as wallet]
            [convex-web.site.explorer :as explorer]
            [convex-web.site.documentation :as documentation]
            [convex-web.site.welcome :as welcome]
            [convex-web.site.repl :as repl]
            [convex-web.site.session :as session]
            [convex-web.site.account :as account]
            [convex-web.site.store]
            [convex-web.site.format :as format]
            [convex-web.site.markdown :as markdown]
            [convex-web.site.team :as team]
            [convex-web.site.theme :as theme]

            [cljs.spec.test.alpha :as stest]

            [reagent.dom]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [lambdaisland.glogi :as log]
            [lambdaisland.glogi.console :as glogi-console]
            [reitit.frontend.easy :as rfe]

            ["react" :as react]
            ["@headlessui/react" :as headlessui]
            ["@heroicons/react/solid" :refer [MenuIcon XIcon ChevronRightIcon]]
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


(defn NotFoundPage [_ _ _]
  [:div.h-screen.flex.justify-center
   [:div.flex.flex-col.items-center.mt-6.space-y-10
    [:a
     {:href (rfe/href :route-name/welcome)
      :alt "Back to Convex : The Internet of Value"}
     [gui/ConvexLogo {:width "120px" :height "120px"}]]

    [:span.font-mono.text-lg.text-gray-500
     "Page not found."]]])

(def not-found-page
  #:page {:id :page.id/not-found
          :component #'NotFoundPage
          :scaffolding? false})

(def pages
  [blank-page
   not-found-page
   
   team/team-page

   ;; ---

   markdown/markdown-page
   markdown/markdown-marketing-page

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
   explorer/state-page

   ;; ---

   session/session-page

   ;; ---

   documentation/reference-page

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
      :route-name :route-name/developer
      :href (rfe/href :route-name/developer)}
     
     :others
     [{:text "Welcome"
       :top-level? true
       :route-name :route-name/developer
       :href (rfe/href :route-name/developer)}
      
      ;; Concepts
      ;; ==============
      {:text "Concepts"
       :top-level? true
       :route-name :route-name/concepts
       :href (rfe/href :route-name/concepts)
       :children
       [{:text "Vision"
         :route-name :route-name/vision
         :href (rfe/href :route-name/vision)}
        
        {:text "Glossary"
         :route-name :route-name/glossary
         :href (rfe/href :route-name/glossary)}
        
        {:text "FAQ"
         :route-name :route-name/faq
         :href (rfe/href :route-name/faq)}]}

      ;; CVM
      ;; ===
      {:text       "Convex Virtual Machine"
       :top-level? true
       :route-name :route-name/cvm
       :href       (rfe/href :route-name/cvm)
       :children   [{:text        "Running Convex Lisp"
                     :route-name   :route-name/cvm.run-cvx
                     :href        (rfe/href :route-name/cvm.run-cvx)
                     :children    [{:text       "Sandbox"
                                    :route-name :route-name/cvm.run-cvx.sandbox
                                    :href       (rfe/href :route-name/cvm.run-cvx.sandbox)}
                                   {:text       "Clients"
                                    :route-name :route-name/cvm.run-cvx.clients
                                    :href       (rfe/href :route-name/cvm.run-cvx.clients)}
                                   {:text       "Runner"
                                    :route-name :route-name/cvm.run-cvx.runner
                                    :href       (rfe/href :route-name/cvm.run-cvx.runner)}]}
                    {:text       "Basic syntax"
                     :route-name :route-name/cvm.basic-syntax
                     :href       (rfe/href :route-name/cvm.basic-syntax)}
                    {:text       "Data types"
                     :route-name :route-name/cvm.data-types
                     :href       (rfe/href :route-name/cvm.data-types)
                     :children   [{:text       "Nil"
                                   :route-name :route-name/cvm.data-types.nil
                                   :href       (rfe/href :route-name/cvm.data-types.nil)}
                                  {:text       "Boolean"
                                   :route-name :route-name/cvm.data-types.boolean
                                   :href       (rfe/href :route-name/cvm.data-types.boolean)}
                                  {:text       "Numbers"
                                   :route-name :route-name/cvm.data-types.numbers
                                   :href       (rfe/href :route-name/cvm.data-types.numbers)}
                                  {:text       "Text"
                                   :route-name :route-name/cvm.data-types.text
                                   :href       (rfe/href :route-name/cvm.data-types.text)}
                                  {:text       "Keyword"
                                   :route-name :route-name/cvm.data-types.keyword
                                   :href       (rfe/href :route-name/cvm.data-types.keyword)}
                                  {:text       "Symbol"
                                   :route-name :route-name/cvm.data-types.symbol
                                   :href       (rfe/href :route-name/cvm.data-types.symbol)}
                                  {:text       "Blob"
                                   :route-name :route-name/cvm.data-types.blob
                                   :href       (rfe/href :route-name/cvm.data-types.blob)}
                                  {:text       "Address"
                                   :route-name :route-name/cvm.data-types.address
                                   :href       (rfe/href :route-name/cvm.data-types.address)}
                                  {:text       "List"
                                   :route-name :route-name/cvm.data-types.list
                                   :href       (rfe/href :route-name/cvm.data-types.list)}
                                  {:text       "Vector"
                                   :route-name :route-name/cvm.data-types.vector
                                   :href       (rfe/href :route-name/cvm.data-types.vector)}
                                  {:text       "Map"
                                   :route-name :route-name/cvm.data-types.map
                                   :href       (rfe/href :route-name/cvm.data-types.map)}
                                  {:text       "Blob map"
                                   :route-name :route-name/cvm.data-types.blob-map
                                   :href       (rfe/href :route-name/cvm.data-types.blob-map)}
                                  {:text       "Set"
                                   :route-name :route-name/cvm.data-types.set
                                   :href       (rfe/href :route-name/cvm.data-types.set)}]}
                    {:text       "Definitions"
                     :route-name :route-name/cvm.definitions
                     :href       (rfe/href :route-name/cvm.definitions)}
                    {:text       "Logic"
                     :route-name :route-name/cvm.logic
                     :href       (rfe/href :route-name/cvm.logic)}
                    {:text       "Errors"
                     :route-name :route-name/cvm.errors
                     :href       (rfe/href :route-name/cvm.errors)}
                    {:text       "Functions"
                     :route-name :route-name/cvm.functions
                     :href       (rfe/href :route-name/cvm.functions)}
                    {:text       "Loops"
                     :route-name :route-name/cvm.loops
                     :href       (rfe/href :route-name/cvm.loops)}
                    {:text       "Code is data"
                     :route-name :route-name/cvm.code-is-data
                     :href       (rfe/href :route-name/cvm.code-is-data)}
                    {:text       "Accounts"
                     :route-name :route-name/cvm.accounts
                     :href       (rfe/href :route-name/cvm.accounts)}
                    {:text       "Callable functions"
                     :route-name :route-name/cvm.callable-functions
                     :href       (rfe/href :route-name/cvm.callable-functions)}
                    {:text       "Actors"
                     :route-name :route-name/cvm.actors
                     :href       (rfe/href :route-name/cvm.actors)}
                    {:text       "Execution phases"
                     :route-name :route-name/cvm.execution-phases
                     :href       (rfe/href :route-name/cvm.execution-phases)}
                    {:text       "Macros"
                     :route-name :route-name/cvm.macros
                     :href       (rfe/href :route-name/cvm.macros)}
                    ]}

      ;; Reference
      ;; =============
      {:text       "Reference"
       :top-level? true
       :route-name :route-name/reference
       :href       (rfe/href :route-name/reference)}

  
      ;; REST API
      ;; ========
      {:text       "REST API"
       :top-level? true
       :route-name :route-name/rest-api
       :href       (rfe/href :route-name/rest-api)
       :children   [{:text       "Create an account"
                     :route-name :route-name/rest-api.create-account
                     :href       (rfe/href :route-name/rest-api.create-account)}
                    {:text       "Account details"
                     :route-name :route-name/rest-api.account-details
                     :href       (rfe/href :route-name/rest-api.account-details)}
                    {:text       "Request coins"
                     :route-name :route-name/rest-api.request-coins
                     :href       (rfe/href :route-name/rest-api.request-coins)}
                    {:text       "Query"
                     :route-name :route-name/rest-api.query
                     :href       (rfe/href :route-name/rest-api.query)}
                    {:text       "Prepare transaction"
                     :route-name :route-name/rest-api.prepare-transaction
                     :href       (rfe/href :route-name/rest-api.prepare-transaction)}
                    {:text       "Submit transaction"
                     :route-name :route-name/rest-api.submit-transaction
                     :href       (rfe/href :route-name/rest-api.submit-transaction)}
                    ]}

      
      
      ;; Sandbox
      ;; ==============
      {:text       "Sandbox"
       :top-level? true
       :route-name :route-name/sandbox
       :href       (rfe/href :route-name/sandbox)}


      ;; Testnet
      ;; ==============
      {:text       "Testnet"
       :top-level? true
       :route-name :route-name/testnet
       :href       (rfe/href :route-name/testnet)
       :children   [{:text       "Accounts"
                     :route-name :route-name/testnet.accounts
                     :href       (rfe/href :route-name/testnet.accounts)
                     :active?    (active #{:route-name/testnet.account
                                           :route-name/testnet.accounts})}
                    {:text       "Blocks"
                     :route-name :route-name/testnet.blocks
                     :href       (rfe/href :route-name/testnet.blocks)
                     :active?    (active #{:route-name/testnet.block
                                           :route-name/testnet.blocks})}
                    {:text       "Request coins"
                     :route-name :route-name/testnet.request-coins
                     :href       (rfe/href :route-name/testnet.request-coins)}
                    {:text       "Status"
                     :route-name :route-name/testnet.status
                     :href       (rfe/href :route-name/testnet.status)}
                    {:text       "Transactions"
                     :route-name :route-name/testnet.transactions
                     :href       (rfe/href :route-name/testnet.transactions)}
                    {:text       "Transfer"
                     :route-name :route-name/testnet.transfer
                     :href       (rfe/href :route-name/testnet.transfer)}
                    {:text       "Wallet"
                     :route-name :route-name/testnet.wallet
                     :href       (rfe/href :route-name/testnet.wallet)}]}

      
      ;; About
      ;; ==============
      {:text "About"
       :top-level? true
       :href (rfe/href :route-name/get-involved)
       :children
       [#_{:text "White Paper"
           :route-name :route-name/white-paper
           :href (rfe/href :route-name/white-paper)}
        
        {:text "Get Involved"
         :route-name :route-name/get-involved
         :href (rfe/href :route-name/get-involved)}
        
        #_{:text "Roadmap"
           :route-name :route-name/roadmap
           :href (rfe/href :route-name/roadmap)}
        
        #_{:text "Convex Foundation"
           :route-name :route-name/convex-foundation
           :href (rfe/href :route-name/convex-foundation)}]}]}))

(defn nav-item-selected? 
  "Returns true if item's route match active route."
  [route {:keys [active? route-name]}]
  (or (= route-name (router/route-name route))
    (when active?
      (active? route))))

(defn nav-item-expanded?
  "Returns true if item or any of its children is selected."
  [route {:keys [children] :as item}]
  (or (nav-item-selected? route item)
    (some #(nav-item-expanded? route %) children)))

(defn NavItem [route {:keys [text top-level? active? href target route-name children] :as item}]
  (let [active? (nav-item-selected? route item)
        
        expanded? (nav-item-expanded? route item)
        
        leaf? (empty? children)]
    
    [:div.flex.flex-col.justify-center.space-y-1
     (if leaf?
       {:class "h-6 xl:h-7"}
       {})
     
     ;; -- Item
     [:a.py-1.px-2.transition.duration-200.ease-in-out.rounded
      (let [border (if active?
                     "bg-gray-200 hover:bg-gray-200"
                     "hover:bg-gray-100")
            
            text-color (cond
                         top-level?
                         "text-blue-500"
                         
                         active?
                         "text-gray-800"
                         
                         :else
                         "text-gray-500")]
        (merge {:href href
                :class [border text-color]}
          (when target
            {:target target})))
      
      [:div.flex.justify-between
       (if target
         [:div.space-x-2
          [:span text]
          [gui/IconExternalLink {:class "w-6 h-6"}]]
         [:span text])
       
       ;; Chevron right & down
       (when-not leaf?
         (let [style "w-6 h-6 text-gray-400"]
           (if expanded?
             [gui/IconChevronDown
              {:class style}]
             [:> ChevronRightIcon
              {:className style}])))]]
     
     ;; -- Children
     (when (and (seq children) expanded?)
       [:div.flex.flex-col.space-y-1.ml-4
        (for [{:keys [text] :as child} children]
          ^{:key text}
          [NavItem route child])])]))

(defn SideNav [active-route]
  (let [{:keys [others]} (nav)]
    [:nav.text-sm.overflow-auto
     {:class [;; Mobile
              "hidden"
              
              ;; Desktop
              "md:flex flex-col flex-shrink-0 md:w-[200px]"]}
     
     (for [{:keys [text] :as item} others]
       ^{:key text}
       [:div.mb-2
        [NavItem active-route item]])]))

(defn Modal [{:frame/keys [uuid page state] :as frame}]
  (let [set-state (stack/make-set-state uuid)

        {:page/keys [title component]} page]

    [:<>

     ;; Overlay
     [:div.fixed.inset-0.bg-gray-500.opacity-75.z-50]

     ;; Modal
     [:div.fixed.inset-0.flex.justify-center.items-center.z-50
      [:div.flex.flex-col.max-w-screen-md.xl:max-w-screen-xl.rounded-lg.shadow-2xl.bg-white.border

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
       [:div.flex.flex-1.max-h-full.overflow-auto
        {:style
         {:max-height "600px"}}
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

            [:div.flex.items-center.space-x-2
             [gui/AIdenticon {:value selected :size 40}]
             [:span.block.ml-2.text-sm
              (format/prefix-# selected)]]

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

                [:span.text-base.block
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

                  [gui/AIdenticon {:value address :size 40}]

                  [:span.block.ml-2
                   (format/prefix-# address)]]])]]]]]]))))


(defn MenuButton
  "Hamburger menu button."
  [{:keys [on-click]}]
  [:button.bg-blue-50.active:bg-blue-200.rounded.p-2.shadow-md.transition.ease-in-out.duration-150
   {:on-click on-click}
   [:> MenuIcon 
    {:className "h-5 w-5 text-blue-800"}]])

(defn CloseButton
  [{:keys [on-click]}]
  [:button.bg-blue-50.active:bg-blue-200.rounded.p-2.shadow-md.transition.ease-in-out.duration-150
   {:on-click on-click}
   [:> XIcon 
    {:className "h-5 w-5 text-blue-800"}]])

(defn Menu
  "Hamburger menu used on mobile."
  []
  (reagent/with-let [open?-ref (reagent/atom false)
                     
                     toggle-visibility #(swap! open?-ref not)]
    
    (let [active-route (:route/match (router/?route))]
      [:<> 
       [MenuButton
        {:on-click toggle-visibility}]
       
       [:> headlessui/Dialog
        {:as "div"
         :className "z-50 fixed inset-0 overflow-auto"
         :open @open?-ref
         :onClose toggle-visibility}
        
        (reagent/as-element
          [:div.fixed.inset-0.overflow-auto
           
           [:> headlessui/Dialog.Overlay
            {:className "fixed inset-0 bg-gray-100"}]
           
           [:div.fixed.inset-y-0.w-full
            
            [:div.h-16.flex.justify-end.items-center.px-6
             [CloseButton
              {:on-click toggle-visibility}]]
            
            (let [{:keys [others]} (nav)]
              [:nav.text-sm.overflow-auto.px-6
               {:class ["flex flex-col flex-shrink-0"]}
               
               (for [{:keys [text] :as item} others]
                 ^{:key text}
                 [:div.mb-2
                  [NavItem active-route item]])])]])]])))

(defn TopNav []
  (let [link-style "text-gray-800 hover:text-gray-500 active:text-black"]
    [:nav.fixed.top-0.inset-x-0.h-16.border-b.border-gray-100.bg-white.z-10
     [:div.w-full.h-full.flex.items-center.justify-between.mx-auto.px-6
      {:class theme/bg-blue-01052A}
      
      [gui/ConvexWhite]
      
      ;; -- Items
      [:div.flex.items-center.justify-end.space-x-4
       
       ;; -- Active account / Create Account
       (cond
         (= :ajax.status/pending (session/?status))
         [gui/Spinner]
         
         (session/?active-address)
         ;; -- Select account
         [AccountSelect]
         
         :else
         ;; -- Create Account
         [:div.hidden.md:block
          [gui/PrimaryButton
           {:on-click #(stack/push :page.id/create-account {:modal? true})}
           [:span.block.font-mono.text-xs.md:text-sm.text-white.uppercase
            {:class gui/button-child-small-padding}
            "Create Account"]]])
       
       ;; -- Mobile menu
       [:div.md:hidden
        [Menu]]]]]))

(defn DeveloperPage [{:frame/keys [uuid page state] :as active-page-frame}]
  (let [{Component :page/component
         title :page/title
         description :page/description
         style :page/style} page
        
        set-state (stack/make-set-state uuid)]
    [:<>
     [TopNav]
     
     ;; Main
     ;; ================
     [:div.w-full.max-w-7xl.mx-auto
      [:div.h-screen.flex.pt-24
       {:class 
        [;; Mobile
         "px-6"
         
         ;; Desktop
         "md:space-x-24 md:px-0"]}
       
       ;; -- Nav
       [SideNav (:route/match (router/?route))]
       
       ;; -- Page
       (when active-page-frame
         [:div.relative.flex.flex-col.flex-1.space-y-10.overflow-auto
          
          (when title
            [:div
             [:h1
              {:class ["inline"
                       "text-gray-900 text-3xl md:text-4xl"
                       "leading-none"
                       "border-b-2 border-blue-500"
                       "pb-2"]}
              title]])
          
          (when description
            [:p.text-gray-500.text-base.max-w-screen-sm description])
          
          [Component active-page-frame state set-state]])]]]))

(defn MarketingPage [{:frame/keys [uuid page state] :as active-page-frame}]
  (let [{Component :page/component
         title :page/title
         description :page/description} page
        
        set-state (stack/make-set-state uuid)]
    [:<>
     
     ;; Top nav
     ;; =========================
     [marketing/Nav]
     
     ;; Page
     ;; =========================
     (when active-page-frame
       [:div.px-6.py-6.w-full.max-w-screen-xl.mx-auto.space-y-10.mb-20
        {:style {:min-height "100vh"}}
        
        (when title
          [:h1
           {:class ["inline"
                    "text-gray-900 text-3xl md:text-4xl"
                    "leading-none"
                    "border-b-2 border-blue-500"
                    "pb-2"]}
           title])
        
        (when description
          [:p.text-gray-500.text-base.max-w-screen-sm description])
        
        [Component active-page-frame state set-state]])
     
     ;; Bottom nav
     ;; =========================
     [:div.w-full.flex.justify-center.bg-gray-900
      [marketing/Sitemap]]
     
     ;; Copyright
     ;; =========================
     [marketing/Copyrigth]]))

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
     (cond 
       (= :marketing (get-in active-page-frame [:frame/page :page/template]))
       [MarketingPage active-page-frame]
       
       (= :developer (get-in active-page-frame [:frame/page :page/template]))
       [DeveloperPage active-page-frame]
       
       ;; Deprecated. We need to update pages manifest to use template instead.
       (get-in active-page-frame [:frame/page :page/scaffolding?] true)
       [DeveloperPage active-page-frame]
       
       :else
       [Page active-page-frame]))
   
   
   ;; Modal
   ;; ================
   (let [{:frame/keys [modal?] :as frame} (stack/?active-frame)]
     (when modal?
       [Modal frame]))
   
   
   ;; Devtools
   ;; ================
   (when goog.DEBUG
     [:div.fixed.bottom-0.left-0.flex.items-center.ml-6.mb-4.p-2.shadow-md.rounded.bg-yellow-300.z-50
      
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
