(ns convex-web.site.welcome
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.gui.marketing :as marketing]
            
            [reitit.frontend.easy :as rfe]))

(defn WelcomePage [_ _ _]
  (let [marketing-vertical ["w-1/2 flex flex-col justify-center space-y-8"]
        marketing-bullets ["flex flex-col space-y-3 text-base"]
        marketing-copy ["text-xl text-white leading-8"]
        
        Item (fn [s]
               [:div.flex.items-center
                [gui/BulletIcon {:style {:min-width "40px" :min-height "40px"}}]
                [:span.font-mono.ml-4 s]])]
    
    [:div
     
     [marketing/Nav (marketing/nav)]
     
     [:div.flex.flex-col.flex-1.items-center.justify-center.rounded.space-y-12
      {:style
       {:height "640px"}}
      
      [gui/ConvexLogo {:width "56px" :height "64px"}]
      
      [:h1.font-mono.text-6xl.text-blue-800
       "What is Convex?"]
      
      [:div.flex.flex-col.items-center.text-xl.text-gray-800.leading-8.max-w-screen-md
       [:div.prose.prose-2xl
        [:p "Convex is an open, decentralised, and efficient technology platform built in the spirit of the original Internet."]
        [:p "Create your own digital assets, smart contracts and powerful decentralised applications for the Digital Economy of tomorrow."]]]
      
      
      [:div.flex.space-x-12
       [:a
        {:href (rfe/href :route-name/vision)}
        [gui/TealButton
         {}
         [:div.w-40
          [:span.font-mono.text-sm.text-white.uppercase
           "Our Vision"]]]]
       
       [:a
        {:href (rfe/href :route-name/documentation-getting-started)}
        [gui/BlueButton
         {}
         [:div.w-40
          [:span.font-mono.text-sm.text-white.uppercase
           "Start Building"]]]]]]
     
     
     [:div.w-full
      
      ;; Convex is flexible
      ;; =========================
      [:div.max-w-screen-xl.mx-auto.flex.items-center.space-x-12.py-16
       
       ;; -- Image
       [:div {:class "w-1/2"}
        [:img {:src "images/convex_flexible_2.png"}]]
       
       ;; -- Copy
       [:div {:class marketing-vertical}
        
        [:h3.font-mono.text-4xl "Convex is Flexible"]
        
        [:p.prose.prose-2xl.leading-8.prose.prose-2xl
         "Convex supports decentralised applications that allow ownership and exchange of Digital Assets that need to be
          100% secure and publicly verifiable (both in terms of data and
          application behaviour), such as:"]
        
        [:div {:class marketing-bullets}
         [Item "Public registries and databases"]
         [Item "Digital currencies"]
         [Item "Prediction markets"]
         [Item "Smart contracts for managing digital assets"]
         [Item "Immutable provenance records"]]
        
        [:a
         {:href (rfe/href :route-name/vision)}
         [gui/TealButton
          {}
          [:div.w-40
           [:span.font-mono.text-sm.text-white.uppercase
            "Our Vision"]]]]]]
      
      
      ;; Convex is fast
      ;; =========================
      [:div.py-16.text-white
       {:style {:background-color "#2E3192"}}
       [:div.max-w-screen-xl.mx-auto.flex.space-x-12
        
        ;; -- Copy
        [:div {:class marketing-vertical}
         
         [:h3.font-mono.text-4xl "Convex is Fast"]
         
         [:p {:class marketing-copy}
          "Using Convergent Proof of Stake, a completely new consensus algorithm, the Convex network is able to execute
          decentralised applications at internet scale. With normal consumer
          grade hardware and network bandwidth the Convex Virtual Machine can achieve:"]
         
         [:div {:class marketing-bullets}
          [Item
           "Tens of thousands of digitally signed transactions per second (far more than the
           1,700 transactions per second typically handled by the VISA network)"]
          
          [Item
           "Millions of smart contract operations per second"]
          
          [Item
           "Low latency (less than a second for global consensus)"]]
         
         [:p {:class marketing-copy}
          "This already is enough to enable consumer applications for the Internet of Value. In the future, 
          it will be possible to extend scalability even further."]]
        
        ;; -- Image
        [:div {:class "w-1/2"}
         [:img {:src "images/convex_fast_2.png"}]]]]
      
      ;; Convex is fun
      ;; =========================
      [:div.py-16.text-white
       {:style {:background-color "#1C2951"}}
       [:div.max-w-screen-xl.mx-auto.flex.space-x-12
        
        ;; -- Image
        [:div {:class "w-1/2"}
         [:img {:src "images/convex_fun_2.png"}]]
        
        ;; -- Copy
        [:div {:class marketing-vertical}
         
         [:h3.font-mono.text-4xl "Convex is Fun"]
         
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
            [:span.font-mono.text-sm.text-white.uppercase
             "Try It Now"]]]]]]]
      
      
      ;; Bottom nav
      ;; =========================
      [:div.bg-gray-900
       [:div.max-w-screen-xl.mx-auto
        [marketing/BottomNav (marketing/nav)]]]]
     
     ;; Copyright
     ;; =========================
     [marketing/Copyrigth]]))

(def welcome-page
  #:page {:id :page.id/welcome
          :component #'WelcomePage
          :scaffolding? false})
