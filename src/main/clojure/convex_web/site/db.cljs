(ns convex-web.site.db
  (:require [convex-web.site.runtime :refer [disp sub]]
            [re-frame.core :as re-frame]))

(defn pages [db]
  (:site/pages db))

(defn find-page [db id]
  (some
    (fn [{id' :page/id :as page}]
      (when (= id id')
        page))
    (pages db)))

(re-frame/reg-event-db :db/!swap-blocks
  (fn [db [_ blocks]]
    (assoc-in db [:site/db :convex-web/blocks] blocks)))

(defn swap-blocks [blocks]
  (disp :db/!swap-blocks blocks))

(re-frame/reg-sub :db/?latest-blocks
  (fn [db _]
    (get-in db [:site/db :convex-web/blocks])))

(defn ?latest-blocks []
  (sub :db/?latest-blocks))
