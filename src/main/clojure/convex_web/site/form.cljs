(ns convex-web.site.form)

(defn SignUp [_context _state _set-state]
  [:div.flex.flex-col.gap-3.p-6.rounded-b-lg.bg-convex-dark-blue
   {:class "w-[740px]"}

   [:div.flex.flex-col
    [:label.text-white
     "First Name"]

    [:input.border.rounded
     {:class "h-[52px]"
      :type "text"
      :on-change
      (fn [_e])}]]


   [:div.flex.flex-col
    [:label.text-white
     "Last Name"]

    [:input.border.rounded
     {:type "text"
      :on-change
      (fn [_e])}]]

   [:div.flex.flex-col
    [:label.text-white
     "E-mail"]

    [:input.border.rounded
     {:type "text"
      :on-change
      (fn [_e])}]]

   ])

(def sign-up-page
  #:page {:id :page.id/sign-up
          :component #'SignUp
          :scaffolding? false})
