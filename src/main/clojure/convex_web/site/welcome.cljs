(ns convex-web.site.welcome
  (:require 
   [convex-web.site.gui.marketing :as marketing]
   
   ["@heroicons/react/solid" :as icon]
   ["@heroicons/react/outline" :as icon-outline]
   ["@radix-ui/react-tooltip" :as tooltip]))

(def key-advantages
  [;; -- Lightning-Fast Finality
   {:background-color "bg-[#6AAAE4]"
    :image "images/instant_transactions1.svg"
    :title "Lightning-Fast Finality"
    :body
    [:div.place-self-start
     {:class "max-w-[180px]"}
     [:p.text-white
      "Confirm " [:span.font-bold "transactions in milliseconds "] ", ideal for real-time apps and frictionless consumer experiences."]]}

   ;; -- Massive Scalability
   {:background-color "bg-convex-dark-blue"
    :image "images/global_scale1.svg"
    :title "Massive Scalability"
    :body
    [:p.text-white
    Process [:span.font-bold "50,000+ operations per second"] ", enabling global scale DeFi and large scale decentralised marketplaces."]}

   ;; -- Uncompromising Security
   {:background-color "bg-convex-medium-blue"
    :image "images/maximum_security1.svg"
    :title "Uncompromising Security"
    :body
    [:p.text-white
     "Harness best-in-class cryptography and a " [:span.font-bold "leaderless BFT consensus"] ", ensuring privacy and tamper-proof integrity."]}

   ;; --Ultra-Low Fees
   {:background-color "bg-convex-dark-blue"
    :image "images/instant_transactions2.svg"
    :title "Ultra-Low Fees"
    :body
    [:p.text-white
     "Pay less than $0.0001 per transaction– " [:span.font-bold "eliminating friction and unlocking new economic models"] "."]}

   ;; -- Green by Design
   {:background-color "bg-convex-medium-blue"
    :image "images/global_scale2.svg"
    :title "Green by Design"
    :body
    [:p.text-white
     "Consume minimal energy, " [:span.font-bold "over 1,000,000 times more efficient than Proof-of-Work—sustainable at internet scale"] "."]}

   ;; -- Advanced Virtual Machine
   {:background-color "bg-[#6AAAE4]"
    :image "images/maximum_security2.svg"
    :title "Advanced Virtual Machine"
    :body
    [:p.text-white
     "A Turing-complete environment based on the lambda calculus, offering " [:span.font-bold "limitless smart contract extensibility."]]}])

(defn KeyAdvantages []
  [:div
   {:class "max-w-[978px]"}
   [:div.grid.grid-cols-1.md:grid-cols-3
    (for [{:keys [title image body background-color]} key-advantages]
      ^{:key title}
      [:div.flex.flex-col.items-center.space-y-2.pt-10.px-6
       {:class ["h-[430px] max-h-[430px] w-[325px] max-w-[325px]" background-color]}

       [:img.mb-6
        {:src (or image "images/instant_transactions.svg")}]

       [:span.text-3xl.font-extrabold.text-white
        title]

       body])]])

(defn Milestone [{:keys [title status body]}]
  [:> tooltip/Root {:delayDuration 0}

   [:> tooltip/Trigger {:asChild true}
    [:div.flex.flex-col.items-center.gap-3.rounded.cursor-default
     {:class "w-[90px]"}

     [:p.text-2xl.text-convex-dark-blue.font-extrabold
      title]

     [:div.flex.justify-center.items-center.rounded-full
      {:class
       ["w-[80px] h-[80px]"
        (case status
          :in-progress
          "bg-transparent"

          :completed
          "bg-white border border-2 border-convex-medium-blue"

          :todo
          "bg-white border border-2 border-convex-light-blue opacity-50")]}

      ;; -- Status icon

      (case status
        :in-progress
        [:> icon-outline/CogIcon
         {:className "w-[80px] h-[80px] text-convex-light-blue"}]

        :completed
        [:> icon/CheckIcon
         {:className "w-11 h-11 text-convex-medium-blue"}]

        :todo
        nil)]]]


   ;; -- Milestone tooltip

   [:> tooltip/Content {:side "top"}
    [:div.px-4.py-2.rounded.shadow-lg
     {:class "bg-[#6D7380]"}
     [:article.prose.prose-sm.prose-invert.text-white

      [:h1.text-2xl
       title]

      body]]]])

(def roadmap
  [{:id :genesis
    :title "Genesis"
    :status :completed
    :body
    [:div
     [:p
      "Convex was designed based on the revolutionary ideas of Convergent Proof of Stake invented in 2018, and the concept was proven with the development of the Convex Virtual Machine capable of executing arbitrary Turing complete smart contracts using functional programming and the lamdba calculus."]]}

   {:id :testnet
    :title "TestNet"
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
      "However we are committed to retaining backwards compatibility and seamless upgrade for existing Convex applications. Most Convex applications will be able to run unchanged."]]}])

(defn Roadmap []
  (into
    [:div
     {:class
      ["w-full"
       "flex flex-col items-center gap-12"
       "md:flex-row md:justify-between md:gap-0"]}]
    (map-indexed
      (fn [i milestone]
        (cond
          (= i (dec (count roadmap)))
          ^{:key (:title milestone)}
          [Milestone milestone]

          :else
          ^{:key (:title milestone)}
          [:div.flex.flex-col.md:flex-row
           [Milestone milestone]

           [:div.hidden.md:flex.flex-col.gap-3
            {:class "w-[60px]"}

            [:div
             {:class "h-[32px]"}]

            [:div.flex.flex-1.items-center
             [:div
              {:class "bg-[#6D7380] h-px w-full"}]]]])))
    roadmap))

(defn WelcomePage [_ _ _]
  (let [subtitle-classes ["text-3xl font-extrabold"]
        subtitle-light-classes (conj subtitle-classes "text-white")
        subtitle-dark-classes (conj subtitle-classes "text-convex-dark-blue")

        prose-classes ["font-source-sans-pro text-lg.md text-2xl"]
        prose-light-classes (conj prose-classes "text-white")
        prose-dark-classes (conj prose-classes "text-convex-dark-blue")

        button-size "h-[55.75px] w-[220px]"
        content-margin-left "md:ml-[280px]"]
    
    [:div

     [marketing/Nav]
     
     ;; -- Building Decentralised Open Economic Systems

     [:div.w-screen.relative
      {:class "md:h-[492px] md:p0 p-8 bg-convex-dark-blue"}

      [:img.absolute.top-0.right-0.opacity-50
       {:src "images/shape_6_white.png"}]

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-3

        [:h1.text-4xl.lg:text-5xl.font-extrabold.text-white.text-center
         "Powering Decentralised Open Economic Systems"]

        [:p.font-source-sans-pro.text-white.text-3xl.text-center
         "A platform for visionaries and builders to create the next generation of intelligent applications and economies."]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 rounded"]
           :href "/sandbox"}
          [:span.text-base.text-convex-dark-blue
           "Try the Sandbox"]]]]]]


     ;; -- Why Convex?

     [:div.w-screen
      {:class "md:h-[492px] md:p0 p-8 bg-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.flex-col.justify-center.items-center

       [:div.flex.flex-col.items-center.md:flex-row.gap-12

        ;; -- Why Convex?

        [:div.flex.flex-col.gap-5

         [:h2
          {:class subtitle-dark-classes}
          "Why Convex?"]

         [:p
          {:class prose-dark-classes}
          "Convex is a high-performance Lattice network for secure, scalable decentralized computation. Born from the demands of data-intensive applications like AI coordination, Convex overcomes the limitations of traditional solutions, delivering:"]]

      [:ul.list-disc.list-inside
         {:class prose-dark-classes}
         [:li [:span.font-bold "Rapid, Convergent Proof of Stake (CPoS)"]: "Sub-second finality and massive throughput for high-frequency transactions and real-time applications."]
         [:li [:span.font-bold "Turing-Complete Virtual Machine"]: "Complex automation for agentic and data-intensive applications, supporting sophisticated smart contracts and autonomous agent behavior."]
         [:li [:span.font-bold "Flexible Lattice Architecture"]: "A next-generation framework handling real-time data flows and trustless interactions at scale, facilitating secure data marketplaces and complex decentralized workflows."]]]]

        ;; -- Logo

        [:img
         {:class "w-[213.52px] h-[140px]"
          :src "images/convex_logo_blue.svg"}]]


       [:div.flex.justify-center.mt-12
        [:a
         {:class "h-[55.75px] w-[220px] inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"
          :href "https://docs.convex.world/"}
         [:span.text-base.text-convex-dark-blue
          "Convex Docs"]]]]]


     ;; -- Limitless Potential
     [:div.w-screen
      {:class "md:h-[584px] md:p0 p-8 bg-convex-sky-blue"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:div.flex.flex-col.gap-5
         {:class content-margin-left}
         [:h2
          {:class subtitle-dark-classes}
          "Limitless Potential"]

         [:p
          {:class prose-dark-classes}
          "Convex empowers a new wave of decentralized applications demanding trustless security, transparent operations, and verifiable results. From intelligent automation to complex digital assets, Convex provides the foundation for innovation. Convex enables:"]

         [:ul.list-disc.list-inside
          {:class prose-dark-classes}
          [:li [:span.font-bold "Intelligent Data Networks"]: Decentralized data hubs and marketplaces with verifiable ownership, secure exchange, and seamless AI integration."]
          [:li [:span.font-bold "Tokenized Economies & Real-Time Markets"]: Digital assets, DeFi applications, and high-throughput marketplaces with instant finality and provable fairness."]
          [:li [:span.font-bold "Autonomous Agents & Intelligent Contracts"]: Smart contracts and agent-based systems to automate processes, coordinate actions, and adapt to changing conditions."]
          [:li [:span.font-bold "Verifiable Governance & Prediction"]: Transparent and tamper-proof systems for voting, decision-making, and forecasting."]
          [:li [:span.font-bold "Secure Provenance & Auditable Histories"]: Track assets, data, and processes with security, immutability, and public verifiability."]]]]
       
         [:p
          {:class prose-dark-classes}
          "Convex's flexible architecture and Turing-complete virtual machine give developers the freedom to build cutting-edge applications with built-in security and transparency."]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"]
           :href "/vision"}
          [:span.text-base.text-convex-dark-blue
           "Our Vision"]]]]]]

     ;; -- Unleashing Lightening-Fast and Massively Scalable Performance

     [:div.w-screen
      {:class "md:h-[584px] md:p0 p-8 bg-convex-dark-blue"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:h2
         {:class subtitle-light-classes}
         "Unleashing Lightning-Fast and Massively Scalable Performance"]

        [:p
         {:class prose-light-classes} 
         "Built on [:span.font-bold "Convergent Proof of Stake"]—an entirely new approach to consensus—Convex executes decentralized applications at [:span.font-bold "internet scale"] using standard [:span.font-bold "consumer hardware"]. The [:span.font-bold "Convex Virtual Machine"] can handle [:span.font-bold "tens of thousands"] of transactions per second (far beyond most global payment networks) and [:span.font-bold "millions"] of smart contract operations per second, all with sub-second finality. This [:span.font-bold "robust performance"] is already sufficient for consumer-facing, [:span.font-bold "real-time dApps"], and it will continue to scale as the network grows."]


    ;; -- Developer Experience

     [:div.w-screen
      {:class "md:h-[492px] md:p0 p-8 bg-convex-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:div.flex.flex-col.gap-5
         {:class content-margin-left}
         [:h2
          {:class subtitle-dark-classes}
          "Developer Experience"]

         [:p
          {:class prose-dark-classes}
          "Build on Convex with ease. Our powerful, interactive environment empowers developers with high productivity while maintaining secure coding principles."]

         [:p
          {:class prose-dark-classes}
          "Experiment and build directly on Convex using Convex Lisp via convex.world interactive REPL. Explore our comprehensive tools: SDKs, libraries, and documentation. Build reliable, trustworthy dApps and deploy confidently in a stable environment."]]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"]
           :href "https://docs.convex.world/docs/products/convex-desktop"}
          [:span.text-base.text-convex-dark-blue
           "Download Convex Desktop"]]]]]]


     [:div.w-screen
      {:class "bg-convex-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center.justify-center

       [KeyAdvantages]]]


     ;; -- Roadmap

     [:div.w-screen
      {:class "md:h-[492px] md:p0 p-8 bg-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.flex-col.gap-12.justify-center

       [:h2
         {:class subtitle-dark-classes}
         "Roadmap"]

       [Roadmap]]]


     ;; -- Site map

     [:div.w-screen.flex.justify-center
      {:class "bg-convex-dark-blue"}
      [marketing/Sitemap2]]


     ;; -- Copyright

     [marketing/Copyrigth]]))

(def welcome-page
  #:page {:id :page.id/welcome
          :component #'WelcomePage
          :scaffolding? false})
