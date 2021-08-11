(ns convex-web.site.welcome
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.gui.marketing :as marketing]
            
            [reitit.frontend.easy :as rfe]))

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

(defn WelcomePage [_ _ _]
  (let [marketing-vertical ["md:w-1/2 flex flex-col justify-center space-y-8"]
        marketing-bullets ["flex flex-col space-y-3 text-white"]
        marketing-copy ["text-base lg:text-xl text-white leading-8"]
        
        ItemTeal (fn [s]
                   [:div.flex.items-center.flex-shrink-0.text-base.lg:text-lg.space-x-3
                    [:div.flex-shrink-0.w-8.h-8.rounded-full.bg-teal-500]
                    [:span s]])
        
        ItemBlue (fn [s]
                   [:div.flex.items-center.text-base.lg:text-lg.space-x-3
                    [:div.flex-shrink-0.w-8.h-8.rounded-full.bg-blue-500]
                    [:span s]])]
    
    [:div
     
     [marketing/Nav]
     
     ;; -- Building the Internet of Value
     [:div.flex.flex-col.justify-center.items-center.space-y-32
      {:class ["bg-gradient-to-b from-[#01052A] to-[#000128]"]}
      
      [:div.w-full.max-w-screen-xl.flex.flex-col.justify-center.items-center.space-y-14.pt-48.px-6
       
       [:h1.text-4xl.lg:text-6xl.font-extrabold.text-white
        "Building the Internet of Value for the business and consumer world"]
       
       [:div.flex.flex-col.md:flex-row.space-y-10.md:space-x-10.md:space-y-0
        [:p.text-white.text-2xl
         "Solving traditional blockchain scalability, sustainability, costs, security and business model problems."]
        
        ;; -- Buttons
        [:div.flex.flex-col.space-y-2
         
         [:a
          {:href (rfe/href :route-name/sandbox)}
          [gui/BlueButton
           {}
           [:div.w-40
            [:span.text-sm.text-white.uppercase
             "Try It Now"]]]]
         
         [:a
          {:href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"
           :target "_blank"}
          [gui/BlueOutlineButton
           {}
           [:div.w-40
            [:span.text-sm.text-white.uppercase
             "Whitepaper"]]]]]]]
      
      [:img
       {:class "object-cover"
        :src "/images/blockchain.png"}]]
     
     ;; -- Key advantages
     (let [container-style "flex flex-col items-center space-y-3"
           
           caption-style "text-white font-bold uppercase"
           
           image-style "object-cover rounded-lg w-48 h-48"
           
           copy-style "text-white"]
       
       [:div.flex.flex-col.justify-center.items-center.py-40
        {:class "bg-[#001D49]"}
        
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
     
     
     [:div.relative.w-full.max-w-screen-xl.mx-auto.flex.flex-col.flex-1.items-center.justify-center.rounded.space-y-12.px-6
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
     
     
     [:div.w-full
      
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
      [:div.py-28.text-white
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
          {:href (rfe/href :route-name/documentation-getting-started)}
          [gui/TealButton
           {}
           [:div.w-40
            [:span.text-sm.text-white.uppercase
             "Try It Now"]]]]]]]
      
      
      ;; Bottom nav
      ;; =========================
      [:div.w-full.flex.justify-center.bg-gray-900
       [marketing/BottomNav (marketing/nav)]]]
     
     ;; Copyright
     ;; =========================
     [marketing/Copyrigth]]))

(def welcome-page
  #:page {:id :page.id/welcome
          :component #'WelcomePage
          :scaffolding? false})
