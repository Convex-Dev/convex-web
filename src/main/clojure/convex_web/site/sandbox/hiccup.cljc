(ns convex-web.site.sandbox.hiccup
  "Specs for the Interactive Sandbox language.

  This is a very small language with a Hiccup-like syntax
  which can be used to create UIs in the Sandbox.

  See convex-web.site.sandbox.hiccup-test for examples,
  and convex-web.site.sandbox.renderer for the renderer."
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::element-tag
  #{:text
    :markdown
    :code
    :button
    :p
    :h-box
    :v-box})

(s/def ::element-attributes
  (s/map-of keyword? any?))

(s/def ::element-content
  (s/or
    :string string?
    :number number?
    :element ::element))

(s/def ::element
  (s/cat
    :tag ::element-tag
    :attributes (s/? ::element-attributes)
    :content (s/* ::element-content)))