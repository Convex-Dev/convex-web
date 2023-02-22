(ns convex-web.site.welcome
  (:require 
   [convex-web.site.gui.marketing :as marketing]
   
   ["@heroicons/react/solid" :as icon]
   ["@heroicons/react/outline" :as icon-outline]
   ["@radix-ui/react-tooltip" :as tooltip]))

(def key-advantages
  [;; -- Instant Transactions
   {:background-color "bg-[#6AAAE4]"
    :image "images/instant_transactions1.svg"
    :title "Instant Transactions"
    :body
    [:div.place-self-start
     {:class "max-w-[180px]"}
     [:p.text-white
      "Confirmations of " [:span.font-bold "transactions in milliseconds "] ", ideal for interactive apps and consumer usage."]]}

   ;; -- Global Scale
   {:background-color "bg-convex-dark-blue"
    :image "images/global_scale1.svg"
    :title "Global Scale"
    :body
    [:p.text-white
     [:span.font-bold "100,000+ TPS "] "– enough for the whole world to use the Convex Network."]}

   ;; -- Maxium Secutiry
   {:background-color "bg-convex-medium-blue"
    :image "images/maximum_security1.svg"
    :title "Maxium Secutiry"
    :body
    [:p.text-white
     "Best in class cryptography and secure " [:span.font-bold "BFT consensus algorithm"] ", fully pseudonymised."]}

   ;; -- Front Running Resistance
   {:background-color "bg-convex-dark-blue"
    :image "images/instant_transactions2.svg"
    :title "Front Running Resistance"
    :body
    [:p.text-white
     "Best in class cryptography and secure " [:span.font-bold "BFT consensus algorithm"] ", fully pseudonymised."]}

   ;; -- 100% Green
   {:background-color "bg-convex-medium-blue"
    :image "images/global_scale2.svg"
    :title "100% Green"
    :body
    [:p.text-white
     "Best in class cryptography and secure " [:span.font-bold "BFT consensus algorithm"] ", fully pseudonymised."]}

   ;; -- Lambda Calculus
   {:background-color "bg-[#6AAAE4]"
    :image "images/maximum_security2.svg"
    :title "Lambda Calculus"
    :body
    [:p.text-white
     "Advanced virtual machine (CVM), that supports execution of " [:span.font-bold "custom smart contracts and unlimited extensibility."]]}])

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
  (into [:div.w-full.flex.flex-col.items-center.md:flex-row.md:justify-between]
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
     
     ;; -- Building the Internet of Value

     [:div.w-screen.relative
      {:class "md:h-[492px] md:p0 p-8 bg-convex-dark-blue"}

      [:img.absolute.top-0.right-0
       {:src "images/shape_6_white.png"}]

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-3

        [:h1.text-4xl.lg:text-5xl.font-extrabold.text-white.text-center
         "Building the Internet of Value for the business and consumer world"]

        [:p.font-source-sans-pro.text-white.text-3xl.text-center
         "Solving traditional blockchain scalability, sustainability, costs, security and business model problems."]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 rounded"]
           :href "/sandbox"}
          [:span.text-base.text-convex-dark-blue
           "Try Convex Now"]]]]]]


     ;; -- What is Convex

     [:div.w-screen
      {:class "md:h-[492px] md:p0 p-8 bg-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.flex-col.justify-center.items-center

       [:div.flex.flex-col.items-center.md:flex-row.gap-12

        ;; -- What is Convex

        [:div.flex.flex-col.gap-5

         [:h2
          {:class subtitle-dark-classes}
          "What is Convex?"]

         [:p
          {:class prose-dark-classes}
          "Convex is the next generation of blockchain technology, with web-scale performance, flexibility and energy efficiency. We're an open source, non-profit foundation enabling new decentralised ecosystems in finance, gaming virtual worlds and the enterprise."]]


        ;; -- Logo

        [:img
         {:class "w-[213.52px] h-[140px]"
          :src "images/convex_logo_blue.svg"}]]


       [:div.flex.justify-center.mt-12
        [:a
         {:class "h-[55.75px] w-[220px] inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"
          :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}
         [:span.text-base.text-convex-dark-blue
          "Get the Whitepaper"]]]]]


     ;; -- Convex is Flexible

     [:div.w-screen
      {:class "md:h-[584px] md:p0 p-8 bg-convex-sky-blue"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:div.flex.flex-col.gap-5
         {:class content-margin-left}
         [:h2
          {:class subtitle-dark-classes}
          "Convex is Flexible"]

         [:p
          {:class prose-dark-classes}
          "Convex supports decentralised applications that allow ownership and exchange of Digital Assets that need to be 100% secure and publicly verifiable (both in terms of data and application behaviour), such as:"]

         [:ul.list-disc.list-inside
          {:class prose-dark-classes}
          [:li "Public registries and databases"]
          [:li "Digital currencies"]
          [:li "Prediction markets"]
          [:li "Smart contracts for managing digital assets"]
          [:li "Immutable provenance records"]]]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"]
           :href "/vision"}
          [:span.text-base.text-convex-dark-blue
           "Our Vision"]]]]]]


     ;; -- Convex is Fast

     [:div.w-screen
      {:class "md:h-[584px] md:p0 p-8 bg-convex-dark-blue"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:h2
         {:class subtitle-light-classes}
         "Convex is Fast"]

        [:p
         {:class prose-light-classes}
         "Using Convergent Proof of Stake, a completely new consensus algorithm, the Convex network is able to execute decentralised applications at internet scale. With normal consumer grade hardware and network bandwidth the Convex Virtual Machine can achieve:"]

        [:ul.list-disc.list-inside
         {:class prose-light-classes}
         [:li "Tens of thousands of digitally signed transactions per second (far more than the 1,700 transactions per second typically handled by the VISA network)"]
         [:li "Millions of smart contract operations per second"]
         [:li "Low latency (less than a second for global consensus)"]
         [:li "This already is enough to enable consumer applications for the Internet of Value. In the future, it will be possible to extend scalability even further."]]]]]


     ;; -- Convex is Fun

     [:div.w-screen
      {:class "md:h-[492px] md:p0 p-8 bg-convex-white"}

      [:div.h-full.max-w-5xl.mx-auto.flex.items-center

       [:div.flex.flex-col.gap-5

        [:div.flex.flex-col.gap-5
         {:class content-margin-left}
         [:h2
          {:class subtitle-dark-classes}
          "Convex is Fun"]

         [:p
          {:class prose-dark-classes}
          "We provide a powerful, interactive environment for development in Convex that enables high productivity while maintaining secure coding principles."]

         [:p
          {:class prose-dark-classes}
          "convex.world provides an interactive REPL allowing users to code directly on the Convex platform using Convex Lisp."]]

        [:div.flex.justify-center.mt-12
         [:a
          {:class [button-size "inline-flex items-center justify-center bg-white hover:bg-gray-100 focus:bg-gray-300 border-2 border-convex-dark-blue rounded"]
           :href "/sandbox"}
          [:span.text-base.text-convex-dark-blue
           "Try it Now"]]]]]]


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
      [marketing/Sitemap (marketing/sitemap)]]


     ;; -- Copyright

     [marketing/Copyrigth]]))

(def welcome-page
  #:page {:id :page.id/welcome
          :component #'WelcomePage
          :scaffolding? false})
