(ns convex-web.site.stack
  (:refer-clojure :exclude [pop])
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.db :as db]
            [re-frame.core :as re-frame]))

(defn- stack [db]
  (:site/stack db))

(defn make-set-state [uuid]
  (fn [f & args]
    (disp :stack/!set-state uuid f args)))

(defn find-frame [db uuid]
  (some
    (fn [frame]
      (when (= uuid (:frame/uuid frame))
        frame))
    (stack db)))

(defn frame [{:page/keys [initial-state] :as page} & [{:keys [state modal?]}]]
  (let [state (or state initial-state)]
    (merge #:frame {:uuid (random-uuid)
                    :page page}
           (when state
             #:frame {:state state})

           (when modal?
             #:frame {:modal? modal?}))))

(re-frame/reg-sub :stack/?stack
  (fn [db _]
    (stack db)))

(re-frame/reg-sub ::?active-frame
  :<- [:stack/?stack]
  (fn [stack _]
    (peek stack)))

(re-frame/reg-sub ::?active-frame-state
  :<- [::?active-frame]
  (fn [{:frame/keys [state]} _]
    state))

(re-frame/reg-sub ::?active-page-frame
  :<- [:stack/?stack]
  (fn [stack _]
    (loop [stack stack]
      (when-let [{:frame/keys [modal?] :as frame} (peek stack)]
        (if modal?
          (recur (cljs.core/pop stack))
          frame)))))

(re-frame/reg-fx :stack.fx/on-push
  (fn [f]
    (f)))

(re-frame/reg-fx :stack.fx/on-resume
  (fn [f]
    (f)))

(re-frame/reg-fx :stack.fx/on-pop
  (fn [f]
    (f)))

(re-frame/reg-event-fx :stack/!on-pop
  (fn [_ [_ f]]
    {:stack.fx/on-pop f}))

(re-frame/reg-event-fx :stack/!push*
  (fn [{:keys [db]} [_ {:frame/keys [uuid state page] :as frame} {:keys [reset?]}]]
    (let [;; When reseting the stack, we must invoke on-pop for the active frames (in reverse order).
          ;; This is required so Pages can potentially dispose resources - e.g.: clear interval.
          pops (when reset?
                 (reduce
                   (fn [pops {:frame/keys [state page] :as frame}]
                     (if-let [on-pop (get page :page/on-pop)]
                       (conj pops [:stack/!on-pop (fn []
                                                    (let [set-state identity]
                                                      ;; `set-state` can't do anything on-pop,
                                                      ;; but we pass it to the function
                                                      ;; for symmetry sake.
                                                      (on-pop frame state set-state)))])
                       pops))
                   []
                   (reverse (stack db))))]
      (merge {:db (if reset?
                    (assoc db :site/stack [frame])
                    (update db :site/stack conj frame))}

             ;; Dispatch effect to invoke on-push fn - if there's one.
             (when-let [on-push (get page :page/on-push)]
               {:stack.fx/on-push
                (fn []
                  (let [set-state (make-set-state uuid)]
                    (on-push frame state set-state)))})

             (when (seq pops)
               {:dispatch-n pops})))))

(re-frame/reg-event-fx :stack/!push
  (fn [{:keys [db]} [_ page-or-id {:keys [reset?] :as options}]]
    (let [page (if (keyword? page-or-id)
                 (db/find-page db page-or-id)
                 page-or-id)

          frame (frame page options)]
      {:dispatch [:stack/!push* frame options]})))

(re-frame/reg-event-fx :stack/!pop
  (fn [{:keys [db]} _]
    (let [stack (stack db)]
      (if (seq stack)
        (let [{:frame/keys [state page] :as frame} (last stack)

              stack' (cljs.core/pop stack)

              resumed-frame (last stack')]
          (merge {:db (assoc db :site/stack stack')}

                 ;; Dispatch effect to invoke on-pop fn - if there's one.
                 (when-let [on-pop (get page :page/on-pop)]
                   {:stack.fx/on-pop
                    (fn []
                      (let [set-state (constantly nil)]
                        ;; `set-state` can't do anything on-pop,
                        ;; but we pass it to the function
                        ;; for symmetry sake.
                        (on-pop frame state set-state)))})

                 ;; Whenever a frame is poped, an `on-resumed` function, on the resumed frame, must be called.
                 ;; You can think of `on-resume` as a transition from *deactive* to *active* state of the Frame.
                 (when-let [on-resume (get-in resumed-frame [:frame/page :page/on-resume])]
                   {:stack.fx/on-resume
                    (fn []
                      (let [{:frame/keys [uuid state]} resumed-frame

                            set-state (make-set-state uuid)]
                        (on-resume resumed-frame state set-state)))})))
        ;; Stack is empty; don't do anything.
        {}))))

(re-frame/reg-event-db :stack/!set-state
  (fn [db [_ uuid f args]]
    ;; Apply f & args to update Frame's state - which UUID matches the `uuid` arg.
    (assoc db :site/stack (reduce
                            (fn [stack {uuid' :frame/uuid :as frame}]
                              (let [frame' (if (= uuid uuid')
                                             (update frame :frame/state (fn [state]
                                                                          (apply f state args)))
                                             frame)]
                                (conj stack frame')))
                            []
                            (stack db)))))

(defn set-state
  "Sets a Frame's state.

   Where `uuid` is the Frame's UUID."
  [uuid f & args]
  (disp :stack/!set-state uuid f args))

(defn push* [frame & [{:keys [reset?] :as options}]]
  (disp :stack/!push* frame options))

(defn push
  "Push Frame to the Stack for Page.

   You can pass a Page's ID or the entity itself.
   In case the Page's ID is given, it will be used to find the Page entity,
   and then a Frame will be created and passed to a lower level API
   which pushes the Frame to the Stack.

   At the end, is always about pushing a Frame to the Stack.

   See `push*` for the low level API."
  [page-or-id & [{:keys [state modal? reset?] :as options}]]
  (disp :stack/!push page-or-id options))

(defn pop
  "Pop active Frame from the Stack.

   In practice, this will 'close' a Page - modal or not."
  []
  (disp :stack/!pop))

(defn ?active-frame
  "Subscription which returns the active Frame - be it a modal or not."
  []
  (sub ::?active-frame))

(defn ?active-frame-state
  "Subscription which returns the active Frame's state."
  []
  (sub ::?active-frame-state))

(defn ?active-page-frame
  "Subscription which returns the active page Frame.

   The last Frame in the Stack might be a modal,
   in this case, you still want the last non-modal Frame
   to display the Page behind the modal."
  []
  (sub ::?active-page-frame))

(defn ?stack []
  (sub :stack/?stack))
