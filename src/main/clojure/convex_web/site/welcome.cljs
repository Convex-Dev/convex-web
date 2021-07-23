(ns convex-web.site.welcome
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.gui.marketing :as marketing]
            
            [reitit.frontend.easy :as rfe]))

(defn WelcomePage [_ _ _]
  (let [marketing-vertical ["w-1/2 flex flex-col justify-center space-y-8"]
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
     
     [marketing/Nav (marketing/nav)]
     
     [:div.relative.w-full.max-w-screen-xl.mx-auto.flex.flex-col.flex-1.items-center.justify-center.rounded.space-y-12.px-10
      {:style
       {:height "640px"}}
      
      ;; Show only on large screens.
      [:div.absolute.top-0.right-0.w-40.mr-32.mt-10.hidden.xl:visible
       [:img.self-center
        {:src "images/convex.png"}]]
      
      [:h1.text-5xl.lg:text-7xl.font-extrabold.text-blue-800
       "What is Convex?"]
      
      [:div.flex.flex-col.items-center.text-xl.text-gray-800.leading-8.max-w-screen-md
       [:div.prose.lg:prose-2xl
        [:p "Convex is an open, decentralised, and efficient technology platform built in the spirit of the original Internet."]
        [:p "Create your own digital assets, smart contracts and powerful decentralised applications for the Digital Economy of tomorrow."]]]
      
      
      [:div.flex.space-x-6.md:space-x-12
       [:a
        {:href (rfe/href :route-name/vision)}
        [gui/TealButton
         {}
         [:div.w-28.md:w-40
          [:span.text-xs.md:text-sm.text-white.uppercase
           "Our Vision"]]]]
       
       [:a
        {:href (rfe/href :route-name/documentation-getting-started)}
        [gui/BlueButton
         {}
         [:div.w-28.md:w-40
          [:span.text-xs.md:text-sm.text-white.uppercase
           "Start Building"]]]]]]
     
     
     [:div.w-full
      
      ;; Convex is flexible
      ;; =========================
      [:div.max-w-screen-xl.mx-auto.lg:flex.items-center.space-x-12.py-16
       
       ;; -- Image
       [:div {:class "w-1/2"}
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
        
        [:a
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
       [:div.max-w-screen-xl.mx-auto.flex.space-x-12
        
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
        [:div {:class "w-1/2"}
         [:img {:src "images/convex_fast_2.png"}]]]]
      
      ;; Convex is fun
      ;; =========================
      [:div.py-52.px-12.lg:px-0.text-white
       {:style {:background-color "#1C2951"}}
       [:div.max-w-screen-xl.mx-auto.lg:flex.space-y-12.lg:space-x-12
        
        ;; -- Image
        [:div {:class "w-1/2"}
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
         
         [:a
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
