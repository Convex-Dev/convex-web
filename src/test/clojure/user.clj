(ns user
  (:require [kaocha.repl :as kaocha]))

(defn run []
  (kaocha/run :unit))

