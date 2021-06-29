(ns convex-web.site.team
  (:require [convex-web.site.gui.marketing :as marketing]))

(defn TeamPage [_ _ _]
  [:div
   
   [marketing/Nav]
   
   [:div.px-10.py-6
    
    [:div.flex.flex-col.space-y-4.mb-10
     [:span.font-mono.text-gray-900
      {:class ["text-2xl" "leading-none"]}
      "Team"]
     
     [:div.w-32.h-2.bg-blue-500.mb-8]]
    
    (let [team [{:name "Mike Anderson"
                 :title "Founder"
                 :bio 
                 [:<>
                  [:p 
                   "Inventor of the Convergent Proof of Stake consensus algorithm and lead architect of Convex. Previously held multiple CTO roles and worked at McKinsey & Company."]
                  
                  [:p
                   "Passionate about open source and dancing."]]}
                
                {:name "Miguel Depaz"
                 :title "Marketing and Partnerships"
                 :bio 
                 [:<>
                  [:p 
                   "Founder and Director of Fashion Luxury Brand. Previously led, created and commercialised Tech-driven products in Telecom corporations such as AT&T, France Telecom, Orange Group, where he held Product marketing and Strategic business positions."]
                  
                  [:p 
                   "Miguel is passionate about Marketing, Technology and Travelling. He speaks 6 languages and enjoys making Technology products simple and accessible to wider audiences"]]}
                
                {:name "Dr Leonard Anderson"
                 :title "Advisor"
                 :bio "Serial innovator and entrepreneur with experience from multi-national, finance and public sector organisations.  His mission is to minimise carbon sourced electricity in as many ways as possible.  One company, Lilli, holds his patent for energy management capsules that could reduced global demand for powering domestic refrigeration. Over £6million has been raised. He advised on the patent application for convergent proof of stake algorithm for efficient, energy saving, decentalised ledger technology.  Historic clients include Shell, BP, Visa, Ford, Nationwide Building Society, Home Office, Pfizer and Regional Electricity Companies.  He also has the patience to grow bonsai trees."}
                
                {:name "Adam Helins"
                 :title "Developer"
                 :bio 
                 [:<>
                  [:p 
                   "Challenge-driven software engineer with a record in IoT, music software, and various open-source projects. Holder of a Master's degree in neuroscience, Adam maintains a keen interest for psychology and human interactions. Currently, he focuses on producing sustainable, simpler, and genuinely useful tech. The uniqueness of Convex has driven him to actively join the project.Hacker from the age of six, amateur pianist and illusionist, never short of ideas."]
                  
                  [:p 
                   "Hacker from the age of six, amateur pianist and illusionist, never short of ideas."]]}
                
                {:name "Pedro Girardi"
                 :title "Developer"
                 :bio 
                 [:<>
                  [:p "Creator of human-computer experiences. He is constantly trying to simplify things and make software more accessible for people."]
                  [:p "Despite spending most of the time crafting user interfaces, he believes excellent technology is usually the ones we can not see, working behind the scenes without demanding our time and attention."]]}
                
                {:name "Mark Engelberg"
                 :title "Developer"
                 :bio
                 [:<>
                  [:p "Developer of several popular mathematical and combinatorial open-source libraries, Mark was drawn to Convex by the power of Convex Lisp, which makes it possible to express complex smart contracts elegantly and succinctly."]]}
                
                {:name "Bill Barman"
                 :title "Developer"
                 :bio
                 [:<>
                  [:p
                   "Bill has many years experience in the information technology industry, offering consulting services to large financial and educational institutions and also to SMEs and startups."]
                  
                  [:p 
                   "Bill’s skillset in computer languages and operating systems is expansive, especially in open source technologies."]
                  
                  [:p 
                   "This breadth of experience allows Bill to offer fresh and unique insights into any development project, because he can confidently work across platforms, systems and technologies. Furthermore, his interest in electronics and hacking computer hardware has been a particular strength when creating embedded system solutions."] 
                  
                  ]}]]
      
      [:div.grid.grid-cols-1.sm:grid-cols-3.lg:grid-cols-4.gap-6
       
       (for [{:keys [name title bio]} team]
         ^{:key name}
         [:div.h-64.p-4.bg-gray-50.rounded-md.shadow.overflow-auto
          [:div.flex.flex-col.space-y-2
           [:span name]
           
           [:span.text-gray-700.text-sm.font-bold.font-mono
            title]
           
           [:article.prose.prose-sm
            bio]]])])]])

(def team-page
  #:page {:id :page.id/team
          :component #'TeamPage
          :scaffolding? false})