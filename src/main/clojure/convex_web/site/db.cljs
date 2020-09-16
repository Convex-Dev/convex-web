(ns convex-web.site.db
  (:require [convex-web.site.runtime :refer [disp sub]]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx :db/!transact
  (fn [{:keys [db]} [_ f args]]
    {:db (apply f db args)}))

(defn transact! [f & args]
  (disp :db/!transact f args))

(defn pages [db]
  (:site/pages db))

(defn find-page [db id]
  (some
    (fn [{id' :page/id :as page}]
      (when (= id id')
        page))
    (pages db)))