(ns convex-web.site.gui.command
  (:require
    [reagent.core :as r]

    ["@headlessui/react" :as headlessui]
    ["@heroicons/react/solid" :refer [PlayIcon]]))

(defn Popover []
  [:> headlessui/Popover
   {:className "relative"}

   [:> headlessui/Popover.Button
    {:className "outline-none"}
    [:> PlayIcon {:className "w-4 h-4 text-green-500"}]]

   [:> headlessui/Popover.Panel
    {:className "absolute z-10 w-screen max-w-xs px-4 mt-3 transform -translate-x-1/2 left-1/2"}

    (r/as-element
      [:div
       {:class "overflow-hidden rounded-lg shadow-lg ring-1 ring-black ring-opacity-5"}
       [:div.relative.flex.flex-col.bg-white.p-6
        [:span "Foo"]
        [:span "Bar"]]])

   ]])