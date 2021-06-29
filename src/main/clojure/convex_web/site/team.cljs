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
                 :bio 
                 [:<>
                  [:p 
                   "Inventor of the Convergent Proof of Stake consensus algorithm, lead architect of Convex, and Managing Director of the Convex Foundation."]
                  
                  [:p
                   "Previously held multiple CTO roles. Leader at McKinsey & Company. UK team for International Informatics Olympiad. Passionate about open source technology and dancing."]]}
                
                
                ;; -- Miguel
                {:name "Miguel Depaz"
                 :title "Marketing and Partnerships"
                 :image "/images/team/miguel.jpg"
                 :bio 
                 [:<>
                  [:p 
                   "Founder and Director of a Fashion Brand. Previously led and commercialised Tech products in Telecom corporations such as AT&T, TIM, Orange Group."]
                  
                  [:p 
                   "Passionate about Marketing and Technology, Miguel speaks six languages and enjoys making Technology products simple. He builds Convex to create decentralised next-generation businesses."]]}
                
                
                ;; -- Dr Leonard
                {:name "Dr Leonard Anderson"
                 :title "Advisor"
                 :image "/images/team/leonard.jpg"
                 :bio 
                 [:<>
                  [:p "Serial innovator and entrepreneur with experience from multi-national, finance and public sector organisations. His mission is to minimise carbon sourced electricity in as many ways as possible."]
                  
                  [:p "He also has the patience to grow Bonsai trees."]]}
                
                
                ;; -- Adam
                {:name "Adam Helins"
                 :title "Developer"
                 :image "/images/team/adam.png"
                 :bio 
                 [:<>
                  [:p 
                   "Challenge-driven software engineer with a record in IoT, music software, and various open-source projects."]
                  
                  [:p 
                   "Hacker from the age of six ; holder of a Master's degree in neurocience ; amateur pianist and illusionist ; never short of ideas."]
                  
                  [:p 
                   "Builds Convex for empowering the green revolution."]]}
                
                
                ;; -- Pedro
                {:name "Pedro Girardi"
                 :title "Developer"
                 :image "/images/team/pedro.jpg"
                 :bio 
                 [:<>
                  [:p "Human-computer interaction."]
                  [:p "Building software for people."]]}
                
                
                ;; -- Mark
                {:name "Mark Engelberg"
                 :title "Developer"
                 :image "/images/team/mark.jpg"
                 :bio
                 [:<>
                  [:p "Developer of several popular mathematical and combinatorial open-source libraries, Mark was drawn to Convex by the power of Convex Lisp, which makes it possible to express complex smart contracts elegantly and succinctly."]]}
                
                
                ;; -- Bill
                {:name "Bill Barman"
                 :title "Developer"
                 :image "/images/team/bill.jpg"
                 :bio
                 [:<>
                  [:p
                   "Experienced professional in the IT industry, offering consulting services to large financial and educational institutions and also to SMEs and startups."]
                  
                  [:p 
                   "Extensive skillset in computer languages and operating systems, especially with open source technologies. Interest in electronics and hacking computer hardware."]]}]]
      
      [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6
       
       (for [{:keys [name title bio image]} team]
         ^{:key name}
         [:div.p-4.bg-gray-50.rounded-lg.shadow.overflow-auto.flex-shrink-0
          [:div.flex.flex-col.flex-shrink-0.space-y-2
           
           ;; -- Avatar
           [:img.self-center.shadow
            {:class "rounded-full object-contain w-44 h-44"
             :src image}]
           
           ;; -- Name & title
           [:div.flex.flex-col.items-center
            
            [:p name]
            
            [:p.text-blue-600.text-sm.text-center.font-bold.font-mono
             title]]
           
           ;; -- Bio
           [:article.prose.prose-sm
            bio]]])])]])

(def team-page
  #:page {:id :page.id/team
          :component #'TeamPage
          :scaffolding? false})