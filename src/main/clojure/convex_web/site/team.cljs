(ns convex-web.site.team)

(defn H2 [heading]
  [:h2
   {:class "text-xl md:text-2xl text-gray-900"}
   heading])

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
              [{:name "Mike Anderson"
                :title "Founder"
                :image "/images/team/mike_anderson.png"
                :linkedin "https://www.linkedin.com/in/mike-anderson-7a9412/"
                :github "https://github.com/mikera"}
               
               {:name "Adam Helins"
                :title "Technology"
                :image "/images/team/adam_helins.png"
                :linkedin "https://www.linkedin.com/in/adam-helins-b81b23215/"
                :github "https://github.com/helins"}

               {:name "Dr Leonard Anderson"
                :title "Governance"
                :image "/images/team/leanord_anderson.png"
                :linkedin "https://www.linkedin.com/in/kemuri/"}

               {:name "Michael Borrelli"
                :title "Operations"
                :image "/images/team/michael_borelli.png"
                :linkedin "https://www.linkedin.com/in/michael-borrelli-mba-llm-ppg-dipp-6a557253"}

               {:name "Rich Kopcho"
                :title "Marketing"
                :image "/images/team/rich_kopcho.png"
                :linkedin "https://www.linkedin.com/in/kopcho/"}]

              :advisors
              (sort-by :name
                [{:name "Claire Cumming"
                  :image "/images/team/claire_cummings.png"
                  :linkedin "https://www.linkedin.com/in/claire-cummings-1573651/"}

                 {:name "Spencer Dobson"
                  :image "/images/team/spencer_debson.png"
                  :linkedin "https://www.linkedin.com/in/spencerdebson/"}

                 {:name "Tej Dosanjh"
                  :image "/images/team/tej_dosanjh.png"
                  :linkedin "https://www.linkedin.com/in/tej-dosanjh-12a482/"}

                 {:name "Toby Lewis"
                  :image "/images/team/toby_lewis.png"
                  :linkedin "https://www.linkedin.com/in/toby-lewis-7aa5a533/"}

                 {:name "Victor Munoz"
                  :image "/images/team/victor_munoz.png"
                  :linkedin "https://www.linkedin.com/in/victor-munoz-sci/"}

                 {:name "Rupert Pearson"
                  :image "/images/team/rupert_pearson.png"
                  :linkedin "https://www.linkedin.com/in/rupert-pearson-b65b9aa/"}

                 {:name "Rodney Prescott"
                  :image "/images/team/rodney_prescott.png"
                  :linkedin "https://www.linkedin.com/in/technologistkiwi/"}

                 {:name "Kurt Sampson"
                  :image "/images/team/kurt_sampson.png"
                  :linkedin "https://www.linkedin.com/in/kurt-sampson/"}

                 {:name "Robert Seller"
                  :image "/images/team/robert_seller.png"
                  :linkedin "https://www.linkedin.com/in/robert-sellar-05104a12/"}

                 {:name "Ian Staley"
                  :image "/images/team/ian_staley.png"
                  :linkedin "https://www.linkedin.com/in/iantstaley/"}

                 {:name "Tirath Virdee"
                  :image "/images/team/tirath_virdee.png"
                  :linkedin "https://www.linkedin.com/in/tirath-virdee-6a08255/"}

                 {:name "Riley Wild"
                  :image "/images/team/riley_wild.png"
                  :linkedin "https://www.linkedin.com/in/rileyjameswild/"}

                 {:name "Christina Yan Zhang"
                  :image "/images/team/christina_yan_zhang.png"
                  :linkedin "https://www.linkedin.com/in/christinayanzhang/"}])

              :community-contributors
              (sort-by :name
                [{:name "Miguel Depaz"
                  :image "/images/team/miguel_depaz.png"}

                 {:name "Alexandra Au Yong"
                  :image "/images/team/alexa_au_yong.png"}

                 {:name "Pedro Girardi"
                  :image "/images/team/pedro_girardi.png"}

                 {:name "Mark Engelberg"
                  :image "/images/team/mark_engelberg.png"}

                 {:name "Bill Barman"
                  :image "/images/team/bill_barman.png"}

                 {:name "Isaac Johnston"
                  :image "/images/team/isaac_johnston.png"}

                 {:name "Ike Mawira"
                  :image "/images/team/ike_mawira.png"}

                 {:name "John Newman"
                  :image "/images/team/john_newman.png"}

                 {:name "Jonathan Day"
                  :image "/images/team/jonathan_day.png"}

                 {:name "Giezi Ordonez"
                  :image "/images/team/giezi_ordonez.png"}])

              :partners-collaborators
              []}]
    [:div

     [:p.mt-6.mb-10.text-gray-700.font-light
      "Convex is developed by a diverse international team using an open source model. Some of the people building Convex are highlighted below."]

     ;; -- Key Team Members

     [H2
      "Key Team Members"]

     [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mt-4
      
      (for [team-member (:key-team-members team)]
        ^{:key (:name team-member)}
        [TeamMember team-member])]


     ;; -- Advisors

     [H2
      "Advisors"]

     [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mt-4

      (for [team-member (:advisors team)]
        ^{:key (:name team-member)}
        [TeamMember team-member])]


     ;; -- Community Contributors

     [H2
      "Community Contributors"]

     [:div.grid.grid-cols-1.lg:grid-cols-3.xl:grid-cols-4.gap-6.mt-4.mb-40

      (for [team-member (:community-contributors team)]
        ^{:key (:name team-member)}
        [TeamMember team-member])]]))

(def team-page
  #:page {:id :page.id/team
          :title "Team"
          :template :marketing
          :component #'TeamPage})
