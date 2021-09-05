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

   ;; CVM
   ;; ==============
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

     [""
      {:name        :route-name/cvm.accounts
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.accounts}
                                               :title  "Accounts"}))}]}]
     ["/actors"
      {:name        :route-name/cvm.accounts.actors
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.accounts.actors}
                                               :title  "Actors"}))}]}]
     ["/callable-functions"
      {:name        :route-name/cvm.accounts.callable-functions
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.accounts.callable-functions}
                                               :title  "Callable functions"}))}]}]
     ]

    ["/basic-syntax"
     {:name        :route-name/cvm.basic-syntax
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :cvm.basic-syntax}
                                              :title  "Basic syntax"}))}]}]

    ["/building-blocks"

     [""
      {:name        :route-name/cvm.building-blocks
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks}
                                               :title  "Building blocks"}))}]}]
     ["/code-is-data"
      {:name        :route-name/cvm.building-blocks.code-is-data
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.code-is-data}
                                               :title  "Code is data"}))}]}]
     ["/definitions"
      {:name        :route-name/cvm.building-blocks.definitions
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.definitions}
                                               :title  "Definitions"}))}]}]
     ["/errors"
      {:name        :route-name/cvm.building-blocks.errors
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.errors}
                                               :title  "Errors"}))}]}]

     ["/functions"
      {:name        :route-name/cvm.building-blocks.functions
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.functions}
                                               :title  "Functions"}))}]}]

     ["/logic"
      {:name        :route-name/cvm.building-blocks.logic
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.logic}
                                               :title  "Logic"}))}]}]

     ["/loops"
      {:name        :route-name/cvm.building-blocks.loops
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.building-blocks.loops}
                                               :title  "Loops"}))}]}]
     ]

    ["/data-types"

     [""
      {:name        :route-name/cvm.data-types
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types}
                                               :title  "Data types"}))}]}]

     ["/address"
      {:name        :route-name/cvm.data-types.address
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.address}
                                               :title  "Address"}))}]}]
     
     ["/blob"
      {:name        :route-name/cvm.data-types.blob
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.blob}
                                               :title  "Blob"}))}]}]
     
     ["/blob-map"
      {:name        :route-name/cvm.data-types.blob-map
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.blob-map}
                                               :title  "Blob map"}))}]}]
     
     ["/boolean"
      {:name        :route-name/cvm.data-types.boolean
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.boolean}
                                               :title  "Boolean"}))}]}]
     
     ["/keyword"
      {:name        :route-name/cvm.data-types.keyword
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.keyword}
                                               :title  "Keyword"}))}]}]
     
     ["/list"
      {:name        :route-name/cvm.data-types.list
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.list}
                                               :title  "List"}))}]}]
     
     ["/map"
      {:name        :route-name/cvm.data-types.map
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.map}
                                               :title  "Map"}))}]}]
     
     ["/nil"
      {:name        :route-name/cvm.data-types.nil
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.nil}
                                               :title  "Nil"}))}]}]
     
     ["/numbers"
      {:name        :route-name/cvm.data-types.numbers
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.numbers}
                                               :title  "Numbers"}))}]}]

     ["/symbol"
      {:name        :route-name/cvm.data-types.symbol
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.symbol}
                                               :title  "Symbol"}))}]}]
     
     ["/set"
      {:name        :route-name/cvm.data-types.set
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.set}
                                               :title  "Set"}))}]}]
     
     ["/text"
      {:name        :route-name/cvm.data-types.text
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.text}
                                               :title  "Text"}))}]}]
     
     ["/vector"
      {:name        :route-name/cvm.data-types.vector
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown
                                              {:reset? true
                                               :state  {:id :cvm.data-types.vector}
                                               :title  "Vector"}))}]}]
     ]

    ["/execution-phases"
     {:name        :route-name/cvm.execution-phases
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :cvm.execution-phases}
                                              :title  "Execution phases"}))}]}]

    ["/macros"
     {:name        :route-name/cvm.macros
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :cvm.macros}
                                              :title  "Macros"}))}]}]

    ["/running-convex-lisp"

     [""
      {:name        :route-name/cvm.run-cvx
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown {:reset? true
                                                                 :state  {:id :cvm.run-cvx}
                                                                 :title  "Running Convex Lisp"}))}]}]
     ["/clients"
      {:name        :route-name/cvm.run-cvx.clients
       :controllers [{:identity identity
                      :start    (fn [_match]
                                  (stack/push :page.id/markdown {:reset? true
                                                                 :state  {:id :cvm.run-cvx.clients}
                                                                 :title  "Clients"}))}]}]

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



   ["reference"
    {:name        :route-name/reference
     :controllers [{:identity identity
                    :start    (fn [match]
                                 (let [library (get-in match [:parameters :query :library] "convex.core")
                                       symbol (get-in match [:parameters :query :symbol])]
                                   (stack/push :page.id/reference 
                                               (merge {:reset? true}
                                                      (when symbol
                                                        {:state {:selected-library library
                                                                 :symbol symbol}})))))}]}]


   ["rest-api"

    [""
     {:name        :route-name/rest-api
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  (merge {:id :rest-api}
                                                             (when-let [section (scroll-to match)]
                                                               {:scroll-to section}))
                                              :title  "REST API"}))}]}]

    ["/account-details"
     {:name        :route-name/rest-api.account-details
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.account-details}
                                                                :title  "Account details"}))}]}]
    ["/create-an-account"
     {:name        :route-name/rest-api.create-account
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.create-account}
                                                                :title  "Create an account"}))}]}]

    ["/prepare-transaction"
     {:name        :route-name/rest-api.prepare-transaction
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.prepare-transaction}
                                                                :title  "Prepare transaction"}))}]}]

    ["/query"
     {:name        :route-name/rest-api.query
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.query}
                                                                :title  "Query"}))}]}]

    ["/request-coins"
     {:name        :route-name/rest-api.request-coins
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.request-coins}
                                                                :title  "Request coins"}))}]}]
    ["/submit-transaction"
     {:name        :route-name/rest-api.submit-transaction
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown {:reset? true
                                                                :state  {:id :rest-api.submit-transaction}
                                                                :title  "Submit transaction"}))}]}]
    ]


   ["testnet"

    [""
     {:name        :route-name/testnet
      :controllers [{:identity identity
                     :start    (fn [_match]
                                 (stack/push :page.id/markdown
                                             {:reset? true
                                              :state  {:id :testnet}
                                              :title  "Testnet"}))}]}]

    ["/accounts"
     {:name        :route-name/testnet.accounts
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (stack/push :page.id/testnet.accounts
                                             (merge {:reset? true}
                                                    (when-let [range (query-range match)]
                                                      {:state range}))))}]}]

    ["/account/:address"
     {:name        :route-name/testnet.account
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (let [address (get-in match
                                                       [:parameters
                                                        :path
                                                        :address])]
                                   (stack/push :page.id/testnet.account
                                               {:reset? true
                                                :state  {:ajax/status        :ajax.status/pending
                                                         :convex-web/account {:convex-web.account/address address}}})))}]}]

    ["/blocks"
     {:name        :route-name/testnet.blocks
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (stack/push :page.id/testnet.blocks
                                             (merge {:reset? true}
                                                    (when-let [range (query-range match)]
                                                      {:state range}))))}]}]

    ["/block/:index"
     {:name        :route-name/testnet.block
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (let [index (js/parseInt (get-in match
                                                                  [:parameters
                                                                   :path
                                                                   :index]))]
                                   (stack/push :page.id/testnet.block
                                               {:reset? true
                                                :state  {:convex-web/block {:convex-web.block/index index}}})))}]}]

    ["/peers"
     ;; TODO
     {:name        :route-name/testnet.peers
      :controllers [{:start (fn [_]
                              (stack/push :page.id/testnet.peers
                                          {:reset? true}))}]}]

    ["/request-coins"
     {:name        :route-name/testnet.request-coins
      :controllers [{:identity identity
                     :start    (fn [_]
                                 (stack/push :page.id/testnet.request-coins
                                             {:reset? true
                                              :state  {:convex-web/faucet {:convex-web.faucet/amount 100000000}
                                                       :faucet-page/config {:faucet-page.config/my-accounts? true}}}))}]}]

    ["/status"
     {:name        :route-name/testnet.status
      :controllers [{:identity identity
                     :start    (fn [_]
                                 (stack/push :page.id/testnet.status
                                             {:reset? true}))}]}]
    ["/transactions"
     {:name        :route-name/testnet.transactions
      :controllers [{:identity identity
                     :start    (fn [match]
                                 (stack/push :page.id/testnet.transactions
                                             (merge {:reset? true}
                                                    (when-let [range (query-range match)]
                                                      {:state range}))))}]}]
   ["/transfer"
    {:name        :route-name/testnet.transfer
     :controllers [{:identity identity
                    :start    (fn [_]
                                (stack/push :page.id/testnet.transfer
                                            {:reset? true}))}]}]

   ["/wallet"
    {:name        :route-name/testnet.wallet
     :controllers [{:identity identity
                    :start    (fn [_]
                                (stack/push :page.id/testnet.wallet
                                            {:reset? true}))}]}]
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
