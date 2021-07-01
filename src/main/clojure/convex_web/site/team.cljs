(ns convex-web.site.team
  (:require [convex-web.site.gui.marketing :as marketing]))

(defn TeamPage [_ _ _]
  [:div
   
   [marketing/Nav (marketing/nav)]
   
   [:div.px-10.py-6.w-full.max-w-screen-xl.mx-auto
    
    ;; Team
    [:div.flex.flex-col.space-y-4
     [:span.font-mono.text-gray-900
      {:class ["text-2xl" "leading-none"]}
      "Team"]
     
     [:div.w-32.h-2.bg-blue-500]]
    
    
    [:p.mt-6.mb-10
     "Convex is developed by a diverse international team using an open source model. Some of the people building Convex are highlighted below."]
    
    
    (let [team [;; -- Mike
                {:name "Mike Anderson"
                 :title "Founder"
                 :areas "Consensus algorithm, CVM execution engine, Overall Convex architecture"
                 :image "/images/team/mike.jpg"
                 :linkedin "https://www.linkedin.com/in/mike-anderson-7a9412/"
                 :github "https://github.com/mikera"}
                
                
                ;; -- Miguel
                {:name "Miguel Depaz"
                 :title "Marketing and Partnerships"
                 :areas "Marketing, Product, Business Partnership, Branding"
                 :image "/images/team/miguel.jpg"
                 :linkedin "https://www.linkedin.com/in/miguel-depaz/"}
                
                
                ;; -- Dr Leonard
                {:name "Dr Leonard Anderson"
                 :title "Advisor"
                 :areas "Patent, Investment and Marketing"
                 :image "/images/team/leonard.jpg"
                 :linkedin "https://www.linkedin.com/in/kemuri/"}
                
                
                ;; -- Alex
                {:name "Alexandra Au Yong"
                 :title "Design and UX"
                 :areas "UX/UI, Design"
                 :image "/images/team/alex.png"
                 :linkedin "https://www.linkedin.com/in/alexandraauyong/"}
                
                
                ;; -- Adam
                {:name "Adam Helins"
                 :title "Developer"
                 :areas "Developer tools, Language design, CVM testing"
                 :image "/images/team/adam.png"
                 :linkedin "https://www.linkedin.com/in/adam-helins-b81b23215/"
                 :github "https://github.com/helins"}
                
                
                ;; -- Pedro
                {:name "Pedro Girardi"
                 :title "Developer"
                 :areas "Client applications, Developer tools"
                 :image "/images/team/pedro.jpg"
                 :linkedin "https://www.linkedin.com/in/pedrorgirardi/"
                 :github "https://github.com/pedrorgirardi"}
                
                
                ;; -- Mark
                {:name "Mark Engelberg"
                 :title "Developer"
                 :areas "Smart contracts, Digital assets"
                 :image "/images/team/mark.jpg"
                 :linkedin "https://www.linkedin.com/in/mark-engelberg-0a09a88a/"
                 :github "https://github.com/Engelberg"}
                
                
                ;; -- Bill
                {:name "Bill Barman"
                 :title "Developer"
                 :areas "Peer operations, CLI, Client libraries"
                 :image "/images/team/bill.jpg"
                 :linkedin "https://www.linkedin.com/in/billbarman/"
                 :github "https://github.com/billbsing"}
                
                
                ;; -- John
                {:name "John Newman"
                 :title "Developer"
                 :areas "Smart contracts, Layer 2 solutions, dApps"
                 :image "/images/team/john.jpg"
                 :linkedin "https://www.linkedin.com/in/johnmichaelnewman/"
                 :github "https://github.com/johnmn3"}]]
      
      [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mb-40
       
       (for [{:keys [name title areas image github linkedin]} team]
         ^{:key name}
         [:div.p-4.rounded-lg.overflow-auto.flex-shrink-0
          [:div.flex.flex-col.flex-shrink-0.space-y-2
           
           ;; -- Avatar
           [:img.self-center.shadow-md
            {:class "object-cover rounded-lg w-60 h-60"
             :src image}]
           
           ;; -- Name & title
           [:div.flex.flex-col.items-center
            
            [:p name]
            
            [:p.text-blue-600.text-sm.text-center.font-bold.font-mono
             title]
            
            [:p.text-gray-600.text-xs.text-center.font-mono
             areas]]
           
           ;; -- Linkedin & GitHub
           [:div.flex.justify-center.space-x-2
            (when linkedin
              [:a
               {:href linkedin
                :target "_blank"}
               [:img.object-contain.h-8.w-8
                {:src "/LI-In-Bug.png"}]])
            
            (when github
              [:a 
               {:href github
                :target "_blank"}
               [:img
                {:src "/GitHub-Mark-32px.png"}]])]]])])
    
    [:div.mb-20
     [marketing/BottomNav (marketing/nav)]]]
   
   [:hr.border-gray-200.mb-8]
   
   [marketing/Copyrigth]])

(def team-page
  #:page {:id :page.id/team
          :component #'TeamPage
          :scaffolding? false})