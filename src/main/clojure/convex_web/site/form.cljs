(ns convex-web.site.form
  (:require
   [convex-web.site.stack :as stack]
   [convex-web.site.gui :as gui]

   [ajax.core :as ajax :refer [POST]]))

(defn SignUp [_context state set-state]
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
          (set-state assoc :company (gui/event-target-value e)))}]]


     ;; -- Privacy Policy

     [:p.text-white.my-4
      "Sending your data is consent to contact you. For further details on how your personal data will be processed and how your consent will be managed, refer to the "
      [:a
       {:href "https://convex.world/privacy-policy"}
       "Convex Privacy Policy."]]


     ;; -- Consent

     [:div.flex.items-center.gap-2

      [:input.mr-2
       {:type "checkbox"
        :checked (:consent state false)
        :on-change
        (fn [^js e]
          (set-state assoc :consent (.-checked (.-target e))))}]

      [:p.text-white
       "I agree to receive other communications from Convex.world."]]


     ;; -- Submit

     [:button
      {:class
       (into ["mt-4"
              "text-blue-900"
              "transition duration-150 ease-in-out"
              "px-4 py-2 rounded"]
         (if (= (:ajax/status state) :ajax.status/pending)
           ["pointer-events-none"
            "bg-gray-400"]
           ["bg-convex-sky-blue active:bg-convex-light-blue"]))
       :on-click
       (fn [_e]
         (set-state assoc :ajax/status :ajax.status/pending)

         (POST "https://api.hsforms.com/submissions/v3/integration/submit/24109496/bc3f0027-bc36-41d6-bfdb-c19700419d20"
           {:format (ajax/json-request-format)
            :params
            {:fields
             [{:objectTypeId "0-1"
               :name "firstname"
               :value (:firstname state "")}

              {:objectTypeId "0-1"
               :name "lastname"
               :value (:lastname state "")}

              {:objectTypeId "0-1"
               :name "email"
               :value (:email state "")}

              {:objectTypeId "0-1"
               :name "company"
               :value (:company state "")}]

             :legalConsentOptions
             {:consent
              {:consentToProcess (:consent state false)
               :text "Sending your data is consent to contact you. For further details on how your personal data will be processed and how your consent will be managed, refer to the Convex Privacy Policy."
               :communications
               [{:subscriptionTypeId 999
                 :value true
                 :text "I agree to receive other communications from Convex.world."}]}}}
            :handler
            (fn [_response]
              (stack/pop))
            :error-handler
            (fn [error]
              (js/console.error error))}))}
      "Submit"]]))

(def sign-up-page
  #:page {:id :page.id/sign-up
          :template :marketing
          :component #'SignUp
          :scaffolding? false})
