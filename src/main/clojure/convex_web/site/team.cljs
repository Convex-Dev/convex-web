(ns convex-web.site.team
  (:require [convex-web.site.gui.marketing :as marketing]))

(defn TeamPage [_ _ _]
  [:div
   
   [marketing/Nav]
   
   [:div.px-10.py-6
    
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
                 :image "/images/team/mike.jpg"
                 :linkedin "https://www.linkedin.com/in/mike-anderson-7a9412/"
                 :github "https://github.com/mikera"}
                
                
                ;; -- Miguel
                {:name "Miguel Depaz"
                 :title "Marketing and Partnerships"
                 :image "/images/team/miguel.jpg"
                 :linkedin "https://www.linkedin.com/in/miguel-depaz/"}
                
                
                ;; -- Dr Leonard
                {:name "Dr Leonard Anderson"
                 :title "Advisor"
                 :image "/images/team/leonard.jpg"
                 :linkedin "https://www.linkedin.com/in/kemuri/"}
                
                
                ;; -- Alex
                {:name "Alexandra Au Yong"
                 :title "Design and UX"
                 :image "/images/team/alex.png"
                 :linkedin "https://www.linkedin.com/in/alexandraauyong/"}
                
                
                ;; -- Adam
                {:name "Adam Helins"
                 :title "Developer"
                 :image "/images/team/adam.png"
                 :linkedin "https://www.linkedin.com/in/adam-helins-b81b23215/"
                 :github "https://github.com/helins"}
                
                
                ;; -- Pedro
                {:name "Pedro Girardi"
                 :title "Developer"
                 :image "/images/team/pedro.jpg"
                 :linkedin "https://www.linkedin.com/in/pedrorgirardi/"
                 :github "https://github.com/pedrorgirardi"}
                
                
                ;; -- Mark
                {:name "Mark Engelberg"
                 :title "Developer"
                 :image "/images/team/mark.jpg"
                 :linkedin "https://www.linkedin.com/in/mark-engelberg-0a09a88a/"
                 :github "https://github.com/Engelberg"}
                
                
                ;; -- Bill
                {:name "Bill Barman"
                 :title "Developer"
                 :image "/images/team/bill.jpg"
                 :linkedin "https://www.linkedin.com/in/billbarman/"
                 :github "https://github.com/billbsing"}]]
      
      [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6
       
       (for [{:keys [name title image github linkedin]} team]
         ^{:key name}
         [:div.p-4.rounded-lg.shadow.overflow-auto.flex-shrink-0
          [:div.flex.flex-col.flex-shrink-0.space-y-2
           
           ;; -- Avatar
           [:img.self-center.shadow-md
            {:class "rounded-full object-cover w-44 h-44"
             :src image}]
           
           ;; -- Name & title
           [:div.flex.flex-col.items-center
            
            [:p name]
            
            [:p.text-blue-600.text-sm.text-center.font-bold.font-mono
             title]]
           
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
                {:src "/GitHub-Mark-32px.png"}]])]]])])]])

(def team-page
  #:page {:id :page.id/team
          :component #'TeamPage
          :scaffolding? false})