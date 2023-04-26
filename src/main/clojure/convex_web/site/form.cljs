(ns convex-web.site.form
  (:require
   [convex-web.site.gui :as gui]))

(defn SignUp [_context state set-state]

  ;; https://api.hsforms.com/submissions/v3/integration/secure/submit/:portalId/:formGuid
  ;; hapikey=0dac4ad2-3d18-4f5f-9e7e-6ec9a1f6f74e

  (let [input-text-class "h-[52px] px-2"]
    [:div.flex.flex-col.gap-3.p-6.rounded-b-lg.bg-convex-dark-blue
     {:class "w-[740px]"}

     ;; -- First name

     [:div.flex.flex-col
      [:label.text-white
       "First name"]

      [:input.border.rounded
       {:class input-text-class
        :type "text"
        :value (:firstname state "")
        :on-change
        (fn [e]
          (set-state assoc :firstname (gui/event-target-value e)))}]]


     ;; -- Last name

     [:div.flex.flex-col
      [:label.text-white
       "Last name"]

      [:input.border.rounded
       {:class input-text-class
        :type "text"
        :value (:lastname state "")
        :on-change
        (fn [e]
          (set-state assoc :lastname (gui/event-target-value e)))}]]


     ;; -- E-mail

     [:div.flex.flex-col
      [:label.text-white
       "E-mail"]

      [:input.border.rounded
       {:class input-text-class
        :type "text"
        :value (:email state "")
        :on-change
        (fn [e]
          (set-state assoc :email (gui/event-target-value e)))}]]


     ;; -- Company

     [:div.flex.flex-col
      [:label.text-white
       "Company"]

      [:input.border.rounded
       {:class input-text-class
        :type "text"
        :value (:company state "")
        :on-change
        (fn [e]
          (set-state assoc :company (gui/event-target-value e)))}]]]))

(def sign-up-page
  #:page {:id :page.id/sign-up
          :component #'SignUp
          :scaffolding? false})
