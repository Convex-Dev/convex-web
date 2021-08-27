(ns convex-web.site.router
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.stack :as stack]
            
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as re-frame]))

(defn route [db]
  (:site/route db))

(defn route-spec [db]
  (get-in db [:site/route :route/match :data :spec]))

(defn route-state [db]
  (get-in db [:site/route :route/state]))

(defn route-controllers [db]
  (get-in db [:site/route :route/match :controllers]))

;; ---

(re-frame/reg-fx :push-history-state
  (fn [route]
    (apply rfe/push-state route)))

(re-frame/reg-sub :router/?route
  (fn [db _]
    (route db)))

(re-frame/reg-sub :router/?route-spec
  (fn [db _]
    (route-spec db)))

(re-frame/reg-sub :router/?route-state
  (fn [db _]
    (route-state db)))

(re-frame/reg-event-fx :router/!push
  (fn [_ [_ & route]]
    {:push-history-state route}))

(re-frame/reg-event-fx :router/!navigate
  (fn [{:keys [db]} [_ match history]]
    (if match
      (let [controllers (route-controllers db)

            controllers (rfc/apply-controllers controllers match)

            new-route (assoc match :controllers controllers)]
        {:db (update db :site/route merge #:route {:match new-route
                                                   :history history})})
      {:db (assoc db :site/route #:route {})})))

;; ---

(defn ?route []
  (sub :router/?route))

(defn query-range [match]
  (let [start (get-in match [:parameters :query :start])
        start (when start
                {:start start})

        end (get-in match [:parameters :query :end])
        end (when end
              {:end end})]
    (merge start end)))

(defn scroll-to [match]
  (get-in match [:parameters :query :section]))

;; ----

(defonce routes
  ["/"
   {:controllers
    [{:identity identity
      :start (fn [{:keys [path]}]               
               (when-not goog.DEBUG
                 (js/gtag "event" "page_view" #js {:page_path path
                                                   :send_to "UA-179518463-1"})))}]}
   
   ;; Welcome
   ;; ==============
   [""
    {:name :route-name/welcome
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/welcome {:reset? true}))}]}]
   
   
   ;; Technology
   ;; ==============
   ["technology"
    {:name :route-name/technology
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown-marketing {:title "Technology"
                                                         :state (merge {:id :technology}
                                                                  (when-let [section (scroll-to match)]
                                                                    {:scroll-to section}))
                                                         :reset? true}))}]}]
   
   ;; Use Cases
   ;; ==============
   ["use-cases"
    {:name :route-name/use-cases
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown-marketing {:title "Use Cases"
                                                         :state (merge {:id :use-cases}
                                                                  (when-let [section (scroll-to match)]
                                                                    {:scroll-to section}))
                                                         :reset? true}))}]}]
   
   ;; Ecosystem
   ;; ==============
   ["ecosystem"
    {:name :route-name/ecosystem
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown-marketing {:title "Ecosystem"
                                                         :state (merge {:id :ecosystem}
                                                                  (when-let [section (scroll-to match)]
                                                                    {:scroll-to section}))
                                                         :reset? true}))}]}]
   
   ;; About
   ;; ==============
   ["about"
    {:name :route-name/about
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown-marketing {:title "About"
                                                         :state (merge {:id :about}
                                                                  (when-let [section (scroll-to match)]
                                                                    {:scroll-to section}))
                                                         :reset? true}))}]}]
   
   ;; Developer
   ;; ==============
   ["developer"
    {:name :route-name/developer
     :controllers
     [{:start (fn [_]
                (stack/push :page.id/markdown {:title "Welcome"
                                               :state {:id :developer}
                                               :reset? true}))}]}]

   ["cvm"

    [""
     {:controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :cvm}
                                              :title  "Convex Virtual Machine"}))}]
      :name        :route-name/cvm}]
   
    ["/accounts"
     {:name        :route-name/cvm.accounts
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :cvm.accounts}
                                              :title  "Accounts"}))}]}]
    
    ["/running-convex-lisp"

     [""
      {:name        :route-name/cvm.run-cvx
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown {:reset? true
                                                                 :state  {:id :cvm.run-cvx}
                                                                 :title  "Running Convex Lisp"}))}]}]

     ["/runner"
      {:name        :route-name/cvm.run-cvx.runner
       :controllers  [{:identity identity
                       :start    (fn [_match]
                                   (stack/push :page.id/markdown {:reset? true
                                                                  :state  {:id :cvm.run-cvx.runner}
                                                                  :title  "Convex Lisp Runner"}))}]}]

     ["/sandbox"
      {:name        :route-name/cvm.run-cvx.sandbox
       :controllers  [{:identity identity
                       :start    (fn [_match]
                                   (stack/push :page.id/markdown {:reset? true
                                                                  :state  {:id :cvm.run-cvx.sandbox}
                                                                  :title  "Sandbox"}))}]}]]
    ]
   
   
   ;; Concepts
   ;; ==============
   ["concepts"
    {:name :route-name/concepts
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown {:title "Concepts"
                                               :state (merge {:id :concepts}
                                                        (when-let [section (scroll-to match)]
                                                          {:scroll-to section}))
                                               :reset? true}))}]}]
   
   
   ;; Vision
   ;; ==============
   ["vision"
    {:name :route-name/vision
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown {:title "Vision"
                                               :state (merge {:id :vision}
                                                        (when-let [section (scroll-to match)]
                                                          {:scroll-to section}))
                                               :reset? true}))}]}]
   
   ;; Glossary
   ;; ==============
   ["glossary"
    {:name :route-name/glossary
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown {:title "Glossary"
                                               :state (merge {:id :glossary}
                                                        (when-let [section (scroll-to match)]
                                                          {:scroll-to section}))
                                               :reset? true}))}]}]
   
   ;; FAQ
   ;; ==============
   ["faq"
    {:name :route-name/faq
     :controllers
     [{:start (fn [match]
                (stack/push :page.id/markdown {:title "FAQ"
                                               :state (merge {:id :faq}
                                                        (when-let [section (scroll-to match)]
                                                          {:scroll-to section}))
                                               :reset? true}))}]}]
   
   
   ;; Create account
   ;; ==============
   ["create-account"
    {:name :route-name/create-account
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/create-account {:reset? true}))}]}]
   
   
   ;; Sandbox
   ;; ==============
   ["sandbox"
    {:name :route-name/sandbox
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/repl {:reset? true}))}]}]
   
   
   ;; My Account (account details)
   ;; ==============
   ["account-details"
    {:name :route-name/my-account
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/my-account {:reset? true}))}]}]
   
   
   ;; Tools
   ;; ==============
   ["tools"
    {:name :route-name/tools
     :controllers
     [{:identity identity
       :start (fn [match]
                (stack/push :page.id/markdown {:title "Tools"
                                               :state (merge {:id :tools}
                                                        (when-let [section (scroll-to match)]
                                                          {:scroll-to section}))
                                               :reset? true}))}]}]
   
   ;; Wallet
   ;; ==============
   ["wallet"
    {:name :route-name/wallet
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/wallet {:reset? true}))}]}]
   
   
   ;; Faucet
   ;; ==============
   ["faucet"
    {:name :route-name/faucet
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/faucet {:reset? true
                                             :state {:convex-web/faucet {:convex-web.faucet/amount 100000000}
                                                     :faucet-page/config {:faucet-page.config/my-accounts? true}}}))}]}]
   
   
   ;; Transfer
   ;; ==============
   ["transfer"
    {:name :route-name/transfer
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/transfer {:reset? true}))}]}]
   
   
   ;; Explorer
   ;; ==============
   ["explorer"
    [""
     {:name :route-name/explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/markdown {:title "Explorer"
                                                :state {:id :explorer}
                                                :reset? true}))}]}]
    
    ["/state"
     {:name :route-name/state
      :controllers
      [{:identity identity
        :start (fn [_]
                 (stack/push :page.id/state {:reset? true}))}]}]
    
    ["/accounts"
     {:name :route-name/accounts-explorer
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/accounts-range-explorer (merge {:reset? true}
                                                                (when-let [range (query-range match)]
                                                                  {:state range}))))}]}]
    
    ["/accounts/:address"
     {:name :route-name/account-explorer
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [address (get-in match [:parameters :path :address])]
                   (stack/push :page.id/account-explorer {:reset? true
                                                          :state
                                                          {:ajax/status :ajax.status/pending
                                                           :convex-web/account {:convex-web.account/address address}}})))}]}]
    
    ["/blocks"
     {:name :route-name/blocks
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/blocks-range-explorer (merge {:reset? true}
                                                              (when-let [range (query-range match)]
                                                                {:state range}))))}]}]
    
    ["/blocks/:index"
     {:name :route-name/block-explorer
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [index (js/parseInt (get-in match [:parameters :path :index]))]
                   (stack/push :page.id/block-explorer {:reset? true
                                                        :state {:convex-web/block {:convex-web.block/index index}}})))}]}]
    
    ["/peers"
     {:name :route-name/peers-explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/peers-explorer {:reset? true}))}]}]
    
    ["/transactions"
     {:name :route-name/transactions
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/transactions-range-explorer (merge {:reset? true}
                                                                    (when-let [range (query-range match)]
                                                                      {:state range}))))}]}]]
   
   
   ;; Documentation
   ;; ==============
   ["documentation"
    [""
     {:name :route-name/documentation
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Documentation"
                                                :state (merge {:id :documentation}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    
    ["/reference"
     {:name :route-name/documentation-reference
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [library (get-in match [:parameters :query :library] "convex.core")
                       symbol (get-in match [:parameters :query :symbol])]
                   (stack/push :page.id/documentation-reference 
                     (merge {:reset? true}
                       (when symbol
                         {:state 
                          {:selected-library library
                           :symbol symbol}})))))}]}]
    
    ["/getting-started"
     {:name :route-name/documentation-getting-started
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Getting Started"
                                                :state (merge {:id :getting-started}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/tutorial"
     {:name :route-name/documentation-tutorial
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Lisp Guide"
                                                :state (merge {:id :tutorials}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/advanced-topics"
     {:name :route-name/advanced-topics
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Advanced Topics"
                                                :state (merge {:id :advanced-topics}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/client-api"
     {:name :route-name/client-api
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Client API"
                                                :state (merge {:id :client-api}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]]
   
   
   ;; About
   ;; ==============
   ["about"
    
    ["/team"
     {:name :route-name/team
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/team {:reset? true}))}]}]
    
    
    ["/white-paper"
     {:name :route-name/white-paper
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:state (merge {:id :white-paper}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/get-involved"
     {:name :route-name/get-involved
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:title "Get Involved"
                                                :state (merge {:id :get-involved}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/roadmap"
     {:name :route-name/roadmap
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:state (merge {:id :under-construction}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]
    
    ["/convex-foundation"
     {:name :route-name/convex-foundation
      :controllers
      [{:identity identity
        :start (fn [match]
                 (stack/push :page.id/markdown {:state (merge {:id :under-construction}
                                                         (when-let [section (scroll-to match)]
                                                           {:scroll-to section}))
                                                :reset? true}))}]}]]])

(def router
  (rf/router routes))

(defn start []
  (let [on-navigate (fn [match history]
                      (if match
                        (disp :router/!navigate match history)
                        ;; If Reitit can't match a route,
                        ;; we push a Not Found page instead.
                        ;; Note that pushing a page to the Stack
                        ;; is exactly what the Controller of a Route does,
                        ;; so there's nothing special about it.
                        (stack/push :page.id/not-found {:reset? true})))]

    (rfe/start! router on-navigate {:use-fragment false})))

(defn route-name [route]
  (get-in route [:data :name]))
