(ns convex-web.site.brand)

(defn Logo [{:keys [name src]}]
  [:div.flex.flex-col.gap-5
   {:class "w-[405px]"}

   [:div
    {:class "w-[405px] h-[200px] flex items-center justify-center border border-[#CED0D5] rounded"}

    [:img
     {:class "w-[340] h-[146.13px]"
      :src src}]]

   ;; -- Name, SVG, PNG
   [:div.flex.justify-between.items-center

    [:span
     {:class "text-[#6D7380]"}
     name]

    (let [B (fn [text]
              [:div
               {:class "w-[40px] h-[24px] flex items-center justify-center bg-[#F5F7FD] rounded"}
               [:span.text-xs
                {:class "text-[#1A2B6B]"}
                text]])]

      [:div.flex.gap-3
       [B "SVG"]
       [B "PNG"]])]])

(defn BrandPage
  [_frame _state _set-state]

  [:div.flex.flex-col

   [:div.flex.flex-col
    [:h1
     "Logo"]

    [:p
     "This is the Convex logo in the official colors and layout versions. The logo should not be placed on top of other objects or distorted and skewed in any way.  Please refer to the Brand Manual for more complete details."]]

   ;; -- Download Brand Manual
   [:a
    {:class
     ["h-[55.75px] w-[220px]"
      "inline-flex items-center justify-center self-center"
      "bg-white hover:bg-gray-100 focus:bg-gray-300"
      "border-2 border-convex-dark-blue rounded"
      "mt-36"]
     :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}
    [:span.text-base.text-convex-dark-blue
     "Download Brand Manual"]]


   [Logo
    {:name "Logo 2 Color Blue"
     :src "images/logo_2_color_blue.svg"}]


   ])

(def brand-page
  #:page {:id :page.id/brand
          :component #'BrandPage
          :template :marketing})
