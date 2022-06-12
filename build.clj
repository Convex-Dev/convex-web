(ns build
  (:require
    [clojure.tools.build.api :as b]))
    
;; More info see https://clojure.org/guides/tools_build

;; Project settings

(def lib           'convex/convex-web)
(def main-class    'convex-web.core)
(def src-dirs      ["src/main/clojure"])
(def resource-dirs ["src/main/resources"])

;; General settings

(def version (format "0.1.%s" (b/git-count-revs nil)))
 
(def build-path "build")
(def class-dir (str build-path "/classes"))

(def basis (b/create-basis {:project "deps.edn"}))
(def dirs-to-copy (concat src-dirs resource-dirs))


(defn clean [_]
  (b/delete {:path build-path}))


(def uber-file (format "%s-%s-standalone.jar" (name lib) version))
(def uber-full-path (str build-path "/" uber-file))
(def build-uberjar-name-file (str build-path "/" "UBERJAR_FILENAME"))

(defn uberjar [{:as opts}]
  (clean nil)
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     basis
                :src-dirs  src-dirs})
  (b/copy-dir {:src-dirs   dirs-to-copy
               :target-dir class-dir})
  ;; Compile not necesary, we can skip this
  (b/compile-clj {:basis     basis
                  :src-dirs  src-dirs
                  :class-dir class-dir
                  :java-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]})
  (b/uber {:class-dir class-dir
           :uber-file uber-full-path
           :basis     basis
           :main      main-class})
  ;; Write filename to specific file for CI
  (spit build-uberjar-name-file uber-file))