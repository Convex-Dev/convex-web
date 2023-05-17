(ns convex-web.site.brand)

(defn Logo [{:keys [name src]}]
  [:div.flex.flex-col.gap-5
   [:div
    {:class "h-[200px] flex items-center justify-center border border-[#CED0D5] rounded"}

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

(defn LogoBlack [{:keys [name src logo]}]
  [:div.flex.flex-col.gap-5

   [:div.bg-black
    {:class "h-[200px] flex items-center justify-center border border-[#CED0D5] rounded"}

    logo]

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

(defn DownloadLink [{:keys [text href]}]
  [:a
   {:class
    ["h-[55.75px] w-[220px]"
     "inline-flex items-center justify-center self-center"
     "bg-white hover:bg-gray-100 focus:bg-gray-300"
     "border-2 border-convex-dark-blue rounded"]
    :href href}
   [:span.text-base.text-convex-dark-blue
    text]])

(defn Heading2 [{:keys [text]}]
  [:h2.text-4xl.font-extrabold
   {:class "text-[#1A2B6B]"}
   text])

(defn BrandPage
  [_frame _state _set-state]

  [:div.flex.flex-col

   [:div.flex.flex-col
    [:h1
     "Logo"]

    [:p
     "This is the Convex logo in the official colors and layout versions. The logo should not be placed on top of other objects or distorted and skewed in any way.  Please refer to the Brand Manual for more complete details."]]

   ;; -- Download Brand Manual
   [:div.mt-36.self-center
    [DownloadLink
     {:text "Download Brand Manual"
      :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}]]


   ;; -- Logos

   [:div.grid.grid-cols-1.md:grid-cols-3.gap-8.mt-20
    [Logo
     {:name "Logo 2 Color Blue"
      :src "images/logo_2_color_blue.svg"}]

    [Logo
     {:name "Logo Dark Blue"
      :src "images/logo_dark_blue.svg"}]

    [Logo
     {:name "Logo Medium Blue"
      :src "images/logo_medium_blue.svg"}]

    [Logo
     {:name "Logo Sky Blue"
      :src "images/logo_sky_blue.svg"}]

    [Logo
     {:name "Logo Light Blue"
      :src "images/logo_light_blue.svg"}]

    [Logo
     {:name "Logo Black"
      :src "images/logo_black.svg"}]]

   ;; -- Download logo assets
   [:div.mt-16.self-center
    [DownloadLink
     {:text "Download logo assets"
      :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}]]


   [:div.grid.grid-cols-1.md:grid-cols-3.gap-8.mt-20
    [LogoBlack
     {:name "Logo 2 Color White"
      :logo
      [:img
       {:class "w-[340px] h-[60.71px]"
        :src "images/logo_2_color_white.svg"}]}]

    [LogoBlack
     {:name "Logo White"
      :logo
      [:img
       {:class "w-[340px] h-[144.97px]"
        :src "images/logo_white.svg"}]}]

    [LogoBlack
     {:name "Logo Light Blue"
      :logo
      [:img
       {:class "w-[340px] h-[144.97px]"
        :src "images/logo_light_blue.svg"}]}]]

   ;; -- Download logo assets
   [:div.mt-16.self-center
    [DownloadLink
     {:text "Download logo assets"
      :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}]]


   [:div.flex.self-center.mt-20
    [Heading2
     {:text "Vertical Logo"}]]

   [:div.grid.grid-cols-1.md:grid-cols-3.gap-8.mt-6
    [Logo
     {:name "Logo 2 Color Blue"
      :src "images/logo_2_color_blue.svg"}]

    [Logo
     {:name "Logo Dark Blue"
      :src "images/logo_dark_blue.svg"}]

    [Logo
     {:name "Logo Medium Blue"
      :src "images/logo_medium_blue.svg"}]]

   ;; -- Download vertical assets
   [:div.mt-16.self-center
    [DownloadLink
     {:text "Download vertical assets"
      :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}]]


   ;; -- Icon Symbol

   [:div.flex.self-center.mt-20
    [Heading2
     {:text "Icon Symbol"}]]

   [:div.grid.grid-cols-1.md:grid-cols-2.justify-center.gap-8.mt-6
    [Logo
     {:name "Logo 2 Color Blue"
      :src "images/logo_2_color_blue.svg"}]

    [Logo
     {:name "Logo Dark Blue"
      :src "images/logo_dark_blue.svg"}]]

   [:div.mt-16.self-center
    [DownloadLink
     {:text "Download icon assets"
      :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}]]


   ;; -- Colors

   [:div.flex.self-center.mt-20
    [Heading2
     {:text "Colors"}]]

   ;; -- Primary

   [:p.font-source-sans-pro.inline-flex.self-center.mt-20.text-3xl
    {:class "text-[#6D7380]"}
    "Primary"]

   [:div.flex.self-center.mt-12.bg-convex-dark-blue.w-full.h-16.rounded]

   [:div.w-full.flex.flex-col.items-center.mt-4
    {:class "text-[#6D7380]"}
    [:span.font-source-sans-pro.font-bold "Dark Blue"]
    [:span.font-source-sans-pro "Pantone 2756C"]
    [:span.font-source-sans-pro "#0F206C"]]


   ;; -- Secondary

   [:p.font-source-sans-pro.inline-flex.self-center.mt-20.text-3xl
    {:class "text-[#6D7380]"}
    "Secondary"]

   [:div.w-full.flex.justify-center.mt-12.gap-8

    ;; -- Medium Blue
    [:div.flex.flex-col

     [:div.h-16.w-40.rounded
      {:class "bg-[#416BA9]"}]

     [:div.flex.flex-col.items-center.mt-4
      {:class "text-[#6D7380]"}
      [:span.font-source-sans-pro.font-bold "Medium Blue"]
      [:span.font-source-sans-pro "Pantone 7683C"]
      [:span.font-source-sans-pro "#416BA9"]]]

    ;; -- Sky Blue
    [:div.flex.flex-col

     [:div.h-16.w-40.rounded
      {:class "bg-[#6AAAE4]"}]

     [:div.flex.flex-col.items-center.mt-4
      {:class "text-[#6D7380]"}
      [:span.font-source-sans-pro.font-bold "Sky Blue"]
      [:span.font-source-sans-pro "Pantone 284C"]
      [:span.font-source-sans-pro "#6AAAE4"]]]

    ;; -- Light Blue
    [:div.flex.flex-col

     [:div.h-16.w-40.rounded
      {:class "bg-[#B8D8EB]"}]

     [:div.flex.flex-col.items-center.mt-4
      {:class "text-[#6D7380]"}
      [:span.font-source-sans-pro.font-bold "Light Blue"]
      [:span.font-source-sans-pro "Pantone 290C"]
      [:span.font-source-sans-pro "#B8D8EB"]]]]


   ;; -- Tertiary

   [:p.font-source-sans-pro.inline-flex.self-center.mt-20.text-3xl
    {:class "text-[#6D7380]"}
    "Tertiary"]

   [:div.w-full.flex.justify-center.mt-12.gap-8

    ;; -- Apple Green
    [:div.flex.flex-col

     [:div.h-16.w-40.rounded
      {:class "bg-[#93D500]"}]

     [:div.flex.flex-col.items-center.mt-4
      {:class "text-[#6D7380]"}
      [:span.font-source-sans-pro.font-bold "Apple Green"]
      [:span.font-source-sans-pro "Pantone 375C"]
      [:span.font-source-sans-pro "#93D500"]]]

    ;; -- Rich Yellow
    [:div.flex.flex-col

     [:div.h-16.w-40.rounded
      {:class "bg-[#E0CC00]"}]

     [:div.flex.flex-col.items-center.mt-4
      {:class "text-[#6D7380]"}
      [:span.font-source-sans-pro.font-bold "Rich Yellow"]
      [:span.font-source-sans-pro "Pantone 605C"]
      [:span.font-source-sans-pro "#6D7380"]]]]


   ;; -- Fonts

   [:p.font-source-sans-pro.inline-flex.self-center.mt-28.text-3xl
    {:class "text-[#6D7380]"}
    "Fonts"]


   ;; -- Inter

   [:div.w-full.flex.mt-12

    [:div.flex.flex-col.gap-2.bg-convex-dark-blue.rounded-lg
     {:class "min-w-[422px] text-[#B8D8EB]"}

     [:div.flex.flex-col.items-end.p-6
      {:class "w-[304.78px]"}

      [:span.text-4xl.font-light
       "Inter Light"]

      [:span.text-4xl.font-normal
       "Inter Regular"]

      [:span.text-4xl.font-medium
       "Inter Medium"]

      [:span.text-4xl.font-bold
       "Inter Bold"]

      [:span.text-4xl.italic
       "Inter Italic"]]]

    [:div.flex.flex-col.justify-center.ml-16

     [:span.text-lg
      {:class "text-[#1A2B6B]"}
      "Inter"]

     [:p.mt-10.max-w-prose
      {:class "text-[#6D7380]"}
      "The main Convex corporate font is Inter. Inter is an Open Source font. This font should be used for all main headlines and should be title case. Avoid setting headlines in all caps. Inter is a free open-source font for commercial use available on Google Fonts."]]]


   [:div.mt-16.self-center
    [DownloadLink
     {:text "Download Inter Font"
      :href "https://fonts.google.com/specimen/Inter"}]]


   ;; -- Source Sans Pro

   [:div.w-full.flex.mt-28

    [:div.flex.flex-col.justify-center

     [:span.text-lg
      {:class "text-[#1A2B6B]"}
      "Source Sans Pro"]

     [:p.mt-10.max-w-prose
      {:class "text-[#6D7380]"}
      "The secondary font is Source Sans Variable. As a substitute, Source Sans Pro may be used if Source Sans Variable is not available. This font should be used for all body text including subheadings, footnotes, quotes, etc. Source Sans Var is a premium font, it can't be distributed freely. You can find out more about obtaining a license to this font through the type foundry. Source sans Pro is a free open-source font for commercial use available on Google Fonts."]]

    [:div.flex.flex-col.flex-1.gap-2.bg-convex-dark-blue.rounded-lg
     {:class "min-w-[550px] text-[#B8D8EB]"}

     [:div.flex.flex-col.flex-1.pl-12.py-12
      {:class "w-[397.22px]"}

      [:span.font-source-sans-pro.text-4xl.font-light
       "Source Sans Pro Light"]

      [:span.font-source-sans-pro.text-4xl.font-normal
       "Source Sans Pro Regular"]

      [:span.font-source-sans-pro.text-4xl.font-semi-bold
       "Source Sans Pro Medium"]

      [:span.font-source-sans-pro.text-4xl.font-bold
       "Source Sans Pro Bold"]

      [:span.font-source-sans-pro.text-4xl.italic
       "Source Sans Pro Italic"]]]]

])

(def brand-page
  #:page {:id :page.id/brand
          :component #'BrandPage
          :template :marketing})
