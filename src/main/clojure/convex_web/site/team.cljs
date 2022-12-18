(ns convex-web.site.team)

(defn TeamMember [{:keys [name title areas image github linkedin]}]
  [:div.p-4.rounded-lg.overflow-auto.flex-shrink-0
   [:div.flex.flex-col.flex-shrink-0.space-y-2

    ;; -- Avatar
    [:img.self-center.shadow-md
     {:class "object-cover rounded-lg w-60 h-60"
      :src image}]

    ;; -- Name & title
    [:div.flex.flex-col.items-center

     [:p name]

     (when title
       [:p.text-blue-600.text-sm.text-center.font-bold.font-mono
        title])

     (when areas
       [:p.text-gray-600.text-xs.text-center.font-mono
        areas])]

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
         {:src "/GitHub-Mark-32px.png"}]])]]])

(defn TeamPage [_ _ _]
  (let [team {:key-team-members
              [;; -- Mike
               {:name "Mike Anderson"
                :title "Founder"
                :areas "Consensus algorithm, CVM execution engine, Overall Convex architecture"
                :image "/images/team/mike.jpg"
                :linkedin "https://www.linkedin.com/in/mike-anderson-7a9412/"
                :github "https://github.com/mikera"}
               
               ;; -- Adam
               {:name "Adam Helins"
                :title "Developer"
                :areas "Developer tools, Language design, CVM testing"
                :image "/images/team/adam.png"
                :linkedin "https://www.linkedin.com/in/adam-helins-b81b23215/"
                :github "https://github.com/helins"}

               ;; -- Dr Leonard
               {:name "Dr Leonard Anderson"
                :title "Advisor"
                :areas "Investment and Marketing"
                :image "/images/team/leonard.jpg"
                :linkedin "https://www.linkedin.com/in/kemuri/"}

               ;; -- Michael Borrelli
               {:name "Michael Borrelli"
                :title "Operations"
                :image "/images/team/borelli.png"}

               ;; -- Rich Kopcho
               {:name "Rich Kopcho"
                :title "Marketing"
                :image "/images/team/rich.png"}]

              :advisors
              []

              :community-contributors
              (sort-by :name
                [;; -- Miguel
                 {:name "Miguel Depaz"
                  :title "Marketing and Partnerships"
                  :areas "Marketing, Product, Business Partnership, Branding"
                  :image "/images/team/miguel.jpg"
                  :linkedin "https://www.linkedin.com/in/miguel-depaz/"}

                 ;; -- Alex
                 {:name "Alexandra Au Yong"
                  :title "Design and UX"
                  :areas "UX/UI, Design"
                  :image "/images/team/alex.png"
                  :linkedin "https://www.linkedin.com/in/alexandraauyong/"}


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

                 ;; -- Isaac
                 {:name "Isaac Johnston"
                  :title "Developer"
                  :areas "Commercial Use Case Design, Implementation and Integration"
                  :image "/images/team/isaac.png"
                  :linkedin "https://www.linkedin.com/in/superstructor/"
                  :github "https://github.com/superstructor"}

                 ;; -- John
                 {:name "John Newman"
                  :title "Developer"
                  :areas "Smart contracts, Layer 2 solutions, dApps"
                  :image "/images/team/john.jpg"
                  :linkedin "https://www.linkedin.com/in/johnmichaelnewman/"
                  :github "https://github.com/johnmn3"}])

              :partners-collaborators
              []}]
    [:div

     [:p.mt-6.mb-10.text-gray-700.font-light
      "Convex is developed by a diverse international team using an open source model. Some of the people building Convex are highlighted below."]

     ;; -- Key Team Members
     [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mb-40
      
      (for [team-member (:key-team-members team)]
        ^{:key (:name team-member)}
        [TeamMember team-member])]


     ;; -- Community Contributors
     [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mb-40

      (for [team-member (:community-contributors team)]
        ^{:key (:name team-member)}
        [TeamMember team-member])]]))

(def team-page
  #:page {:id :page.id/team
          :title "Team"
          :template :marketing
          :component #'TeamPage})
