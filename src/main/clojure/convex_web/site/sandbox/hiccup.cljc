(ns convex-web.site.sandbox.hiccup
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::element
  (s/cat
    :tag #{:text :button}
    :attributes (s/? map?)
    :content (s/+ (s/or
                    :terminal (s/or
                                :string string?
                                :number number?)

                    :element ::element))))