(ns convex-web.site.welcome
  (:require 
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]

   [convex-web.site.theme :as theme]
   [convex-web.site.gui :as gui]
   [convex-web.site.gui.marketing :as marketing]
   
   ["@heroicons/react/solid" :as icon :refer [ChevronDownIcon]]))

(defn KeyAdvantages []
  [:div.grid.grid-cols-1.md:grid-cols-3.gap-4
   
   (for [{:keys [title image link copy]} [{:title "Instant Transactions"}
                                          
                                          {:title "Global State"}
                                          
                                          {:title "Maxium Secutiry"}
                                          
                                          {:title "Front Running Resistance"}
                                          
                                          {:title "100% Green"}
                                          
                                          {:title "Lambda Calculus"}]]
     ^{:key title}
     [:div.flex.flex-col.items-center.space-y-2
      [:span.font-medium.underline
       title]])])

(defn Roadmap []
  (r/with-let [selected-version-ref (r/atom :beta)]

    (let [selected-version @selected-version-ref

          roadmap [{:id :genesis
                    :title "Genesis"
                    :status :completed
                    :body
                    [:div
                     [:p
                      "Convex was designed based on the revolutionary ideas of Convergent Proof of Stake invented in 2018, and the concept was proven with the development of the Convex Virtual Machine capable of executing arbitrary Turing complete smart contracts using functional programming and the lamdba calculus."]]}

                   {:id :testnet
                    :title "Testnet"
                    :status :completed
                    :body
                    [:div
                     [:p
                      "The Test Network was launched in early 2020 and has been running ever since. "]

                     [:p
                      "The testnet serves as a powerful tool for developing Convex actors and applications, as well as providing a testing ground for new CVM features and capabilities."]

                     [:p
                      "It is periodically reset for the purposes of upgrades, but otherwise works as a fully functioning Convex network."]]}

                   {:id :alpha
                    :title "Alpha"
                    :status :completed
                    :body
                    [:div
                     [:p
                      "The Alpha release brought substantial new capabilities to the CVM, performance enhancements and tolling to make it possible for anyone to create a Convex Peer and participate in maintaining the consensus of the Network. "]

                     [:p
                      "The post-Alpha phase will include further functional development to complete the scope of capabilities expected for the main network. Some breaking changes may occur, however the Alpha is already broadly suitable for development of prototype applications and use cases."]]}

                   {:id :beta
                    :title "Beta"
                    :status :in-progress
                    :body
                    [:div
                     [:p
                      "The Beta release will be broadly feature complete, suitable for development of full-scale decentralised applications and use cases in advance of the main network launch. Developers can confidently build upon the Beta release and expect only minor changes and upgrades prior to main network launch."]]}

                   {:id :gamma
                    :title "Gamma"
                    :status :todo
                    :body
                    [:div
                     [:p
                      "The Gamma release will provide a feature complete platform for security audits, performance tuning and testing of main network release candidates. No substantial functional or protocol changes will be made during this period unless critical security issues make these necessary. "]]}

                   {:id :v1
                    :title "V1"
                    :status :todo
                    :body
                    [:div
                     [:p
                      "The V1 Mainnet release will be the first production launch of the Convex network, suitable for production applications managing real digital assets and applications. "]

                     [:p
                      "Holders of pre-sold Convex coins will be able to receive and utilise their coins via their own secure wallets. Decentralised applications and use will be able to launch with fully functional digital assets."]]}

                   {:id :v2
                    :title "V2"
                    :status :todo
                    :body
                    [:div
                     [:p
                      "The V2 Mainnet release will be the first major upgrade to Convex, and may involve changes to the peer protocol and CVM design.  Planned developments include:"]

                     [:ul
                      [:li
                       "First class CVM types"]

                      [:li
                       "Unlimited scalability with integrated subnets"]]

                     [:p
                      "However we are committed to retaining backwards compatibility and seamless upgrade for existing Convex applications. Most Convex applications will be able to run unchanged."]]}]

          ;; Roadmap indexed by ID; it's easy to read a particular version by ID.
          roadmap-indexed (->> roadmap
                            (map (juxt :id identity))
                            (into {}))]

      [:div.flex.flex-col.space-y-10

       ;; -- Timeline
       [:div.relative.flex.items-center.py-10.px-8.space-x-2.overflow-auto

        (into [:<>]

          (interpose
            [:div.w-full
             {:style {"minWidth" "40px"}}
             [:hr.flex-1.border.border-gray-400]]

            (for [{:keys [id title status]} roadmap]
              [:button
               {:on-click #(reset! selected-version-ref id)}
               [:div.w-14.h-14.flex.justify-center.items-center.rounded-full.shadow-2xl
                {:class
                 (case status
                   :in-progress
                   "bg-blue-500 hover:bg-blue-400"

                   :completed
                   "bg-teal-500 hover:bg-teal-400"

                   :todo
                   "bg-gray-400 bg-opacity-30 hover:bg-opacity-50")}


                ;; -- Version

                [:p.absolute.top-0.text-2xl.text-white
                 {:class (when (= id selected-version)
                           "underline")}
                 title]


                ;; -- Status icon

                (case status
                  :in-progress
                  [:> icon/CogIcon
                   {:className "w-6 h-6 text-white"}]

                  :completed
                  [:> icon/CheckIcon
                   {:className "w-6 h-6 text-white"}]

                  :todo
                  nil)]])))]

       [:div.self-center.bg-black.bg-opacity-10.rounded.py-4.px-6

        [:div.flex.flex-col.overflow-auto
         {:class "h-[240px]"}

         [:p.self-center.text-gray-200.text-3xl.font-bold
          (get-in roadmap-indexed [selected-version :title])]

         [:div.prose.prose-xl.text-gray-200
          (get-in roadmap-indexed [selected-version :body])]]]])))

(defn WelcomePage [_ _ _]
  (let [marketing-vertical ["md:w-1/2 flex flex-col justify-center space-y-8"]
        marketing-bullets ["flex flex-col space-y-3 text-white"]
        marketing-copy ["text-base lg:text-xl text-white leading-8"]
        
        ItemTeal (fn [s]
                   [:div.flex.items-center.flex-shrink-0.text-base.lg:text-lg.space-x-3
                    [:div.flex-shrink-0.w-8.h-8.rounded-full.bg-teal-500]
                    [:span.text-gray-700 s]])
        
        ItemBlue (fn [s]
                   [:div.flex.items-center.text-base.lg:text-lg.space-x-3
                    [:div.flex-shrink-0.w-8.h-8.rounded-full.bg-blue-500]
                    [:span s]])]
    
    [:div
     
     [marketing/Nav]
     
     ;; -- Building the Internet of Value

     [:div.w-full.flex.justify-center.items-center
      {:class "w-full bg-convex-dark-blue h-[492px]"}

      [:div.flex.flex-col.max-w-5xl.gap-3

       [:h1.text-4xl.lg:text-5xl.font-extrabold.text-white.text-center
        "Building the Internet of Value for the business and consumer world"]

       [:p.font-source-sans-pro.text-white.text-lg.md:text-3xl.text-center
        "Solving traditional blockchain scalability, sustainability, costs, security and business model problems."]]]
     
     
     ;; -- Key advantages
     #_(let [container-style "flex flex-col items-center space-y-3"

             caption-style "text-white text-base md:text-2xl font-bold uppercase"

             image-style "object-cover rounded-lg w-36 md:w-48 h-26 md:h-48"

             copy-style "text-white text-base md:text-lg text-center md:text-left"]

         [:div.flex.flex-col.justify-center.items-center.py-16.md:py-40
          {:id "advantages"
           :class "bg-[#001D49]"}

          [:div.grid.grid-cols-1.md:grid-cols-3.gap-x-12.gap-y-20.px-6.max-w-screen-xl

           ;; -- Instant Transaction
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/instant.png"}]

            [:span
             {:class caption-style}
             "Instant Transactions"]

            [:p
             {:class copy-style}
             "Confirmations of "
             [:span.font-bold "transactions in milliseconds"]
             ", ideal for interactive apps and consumer usage."]]

           ;; -- Global Scale
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/global.png"}]

            [:span
             {:class caption-style}
             "Global Scale"]

            [:p
             {:class copy-style}
             [:span.font-bold "100,000+ TPS"] " – enough for the whole world to use the  Convex Network."]]

           ;; -- Maximum SecurIty
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/security.png"}]

            [:span
             {:class caption-style}
             "Maximum SecurIty"]

            [:p
             {:class copy-style}
             " Best in class cryptography and secure "
             [:span.font-bold "BFT consensus algorithm"]
             ", fully pseudonymised."]]

           ;; -- Low Cost
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/cost.png"}]

            [:span
             {:class caption-style}
             "Low Cost"]

            [:p
             {:class copy-style}
             "Transaction costs less than $0.0001 – "
             [:span.font-bold "more than 10,000 times cheaper than Ethereum."]]]

           ;; -- 100% Green
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/green.png"}]

            [:span
             {:class caption-style}
             "100% Green"]

            [:p
             {:class copy-style}
             "No wastage of energy or computing resources."
             [:span.font-bold " More than 1,000,000 times more efficient than Bitcoin."]]]

           ;; -- Smart Contracts
           [:div
            {:class container-style}

            [:img
             {:class image-style
              :src "/images/contracts.png"}]

            [:span
             {:class caption-style}
             "Smart Contracts"]

            [:p
             {:class copy-style}
             "Advanced virtual machine (CVM), that supports  execution of "
             [:span.font-bold
              "custom smart contracts and unlimited extensibility."]]]]])
     
     
     #_[:div.relative.w-full.max-w-screen-xl.mx-auto.flex.flex-col.flex-1.items-center.justify-center.rounded.space-y-12.px-6
        {:style
         {:height "640px"}}

        ;; Show only on large screens.
        [:div.absolute.top-0.right-0.w-40.mr-32.mt-10.invisible.xl:visible
         [:img.self-center
          {:src "images/convex.png"}]]

        [:h1.text-5xl.lg:text-7xl.font-extrabold.text-blue-800
         "What is Convex?"]

        [:div.flex.flex-col.items-center.text-xl.text-gray-800.leading-8.max-w-screen-md
         [:div.prose.lg:prose-2xl
          [:p "Convex is the next generation of blockchain technology, with web-scale performance, flexibility and energy efficiency."]
          [:p "We're an open source, non-profit foundation enabling new decentralised ecosystems in finance, gaming virtual worlds and the enterprise."]]]]
     
     
     #_[:div.w-full

        ;; Convex is flexible
        ;; =========================
        [:div.max-w-screen-xl.mx-auto.lg:flex.items-center.md:space-x-12.py-16.px-6

         ;; -- Image
         [:div {:class "md:w-1/2"}
          [:img {:src "images/convex_flexible_2.png"}]]

         ;; -- Copy
         [:div {:class marketing-vertical}

          [:h3.text-5xl.lg:text-7xl.font-extrabold "Convex is Flexible"]

          [:p.prose.lg:prose-2xl
           "Convex supports decentralised applications that allow ownership and exchange of Digital Assets that need to be
          100% secure and publicly verifiable (both in terms of data and
          application behaviour), such as:"]

          [:div.text-gray-600 {:class marketing-bullets}
           [ItemTeal "Public registries and databases"]
           [ItemTeal "Digital currencies"]
           [ItemTeal "Prediction markets"]
           [ItemTeal "Smart contracts for managing digital assets"]
           [ItemTeal "Immutable provenance records"]]

          [:a.invisible.md:visible
           {:href (rfe/href :route-name/vision)}
           [gui/TealButton
            {}
            [:div.w-40
             [:span.text-sm.text-white.uppercase
              "Our Vision"]]]]]]


        ;; Convex is fast
        ;; =========================
        #_[:div.py-28.text-white
           {:style {:background-color "#2E3192"}}
           [:div.max-w-screen-xl.mx-auto.lg:flex.items-center.space-y-12.md:space-y-0.md:space-x-12.py-16.px-6

            ;; -- Copy
            [:div {:class marketing-vertical}

             [:h3.text-5xl.lg:text-7xl.font-extrabold "Convex is Fast"]

             [:p {:class marketing-copy}
              "Using Convergent Proof of Stake, a completely new consensus algorithm, the Convex network is able to execute
          decentralised applications at internet scale. With normal consumer
          grade hardware and network bandwidth the Convex Virtual Machine can achieve:"]

             [:div {:class marketing-bullets}
              [ItemBlue
               "Tens of thousands of digitally signed transactions per second (far more than the
           1,700 transactions per second typically handled by the VISA network)"]

              [ItemBlue
               "Millions of smart contract operations per second"]

              [ItemBlue
               "Low latency (less than a second for global consensus)"]]

             [:p {:class marketing-copy}
              "This already is enough to enable consumer applications for the Internet of Value. In the future,
          it will be possible to extend scalability even further."]]

            ;; -- Image
            [:div {:class "md:w-1/2"}
             [:img {:src "images/convex_fast_2.png"}]]]]

        ;; Convex is fun
        ;; =========================
        [:div.md:py-52.lg:px-0.text-white
         {:style {:background-color "#1C2951"}}
         [:div.max-w-screen-xl.mx-auto.lg:flex.items-center.space-y-12.md:space-y-0.md:space-x-12.py-16.px-6

          ;; -- Image
          [:div {:class "md:w-1/2"}
           [:img {:src "images/convex_fun_2.png"}]]

          ;; -- Copy
          [:div {:class marketing-vertical}

           [:h3.text-5xl.lg:text-7xl.font-extrabold "Convex is Fun"]

           [:p {:class marketing-copy}
            "We provide a powerful, interactive environment for
          development in Convex that enables high productivity while maintaining
          secure coding principles."]

           [:p {:class marketing-copy}
            "convex.world provides an interactive REPL allowing users to code directly
          on the Convex platform using Convex Lisp."]

           [:a.invisible.md:visible
            {:href (rfe/href :route-name/sandbox)}
            [gui/TealButton
             {}
             [:div.w-40
              [:span.text-sm.text-white.uppercase
               "Try It Now"]]]]]]]


        ;; Roadmap
        ;; =========================

        #_[:div.flex.items-center.justify-center.bg-gray-800.h-screen
           [:div.w-full.max-w-screen-xl.mx-auto.px-6.flex.flex-col.space-y-24

            [:h3.text-5xl.lg:text-7xl.font-extrabold.text-white
             "Roadmap"]

            [Roadmap]]]


        ;; Bottom Nav
        ;; =========================
        [:div.w-full.flex.justify-center
         {:class "bg-convex-dark-blue"}
         [marketing/Sitemap (marketing/sitemap)]]]
     
     ;; Copyright
     ;; =========================
     [marketing/Copyrigth]]))

(def welcome-page
  #:page {:id :page.id/welcome
          :component #'WelcomePage
          :scaffolding? false})
