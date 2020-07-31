(ns codemirror-reagent.core
  (:require [cljs.pprint :as pprint]
            [cljs.spec.alpha :as s]
            [reagent.core :as reagent]

            ["codemirror" :as codemirror]
            ["codemirror/mode/clojure/clojure"]
            ["codemirror/addon/edit/matchbrackets"]
            ["codemirror/addon/edit/closebrackets"]))

(def pass (.-Pass codemirror))

(def default-configuration
  {:mode "clojure"
   :readOnly false
   :lineNumbers false
   :matchBrackets true
   :autoCloseBrackets true})

(defn extra-keys [{:keys [enter shift-enter]}]
  (merge {}
         (when enter
           {"Enter" enter})
         (when shift-enter
           {"Shift-Enter" shift-enter})))

(defn set-extra-keys [editor extra-keys]
  (.setOption editor "extraKeys" (clj->js extra-keys))

  nil)

;; -- Code Mirror API

(defn cm-get-value [^js editor]
  (.getValue editor))

(defn cm-set-value [^js editor value]
  ;; `nil` crashes CodeMirror
  (.setValue editor (or value "")))

(defn cm-focus [^js editor]
  (.focus editor))

(defn cm-get-doc [^js editor]
  (.getDoc editor))

(defn cm-get-cursor [^js doc]
  (.getCursor doc))

(defn cm-last-line [^js doc]
  (.lastLine doc))

(defn cm-get-line [^js doc n]
  (.getLine doc n))

;; --

(def available-editor-events
  "See https://codemirror.net/doc/manual.html#events"
  #{"change"
    "changes"
    "beforeChange"
    "cursorActivity"
    "keyHandled"
    "inputRead"
    "electricInput"
    "beforeSelectionChange"
    "viewportChange"
    "swapDoc"
    "gutterClick"
    "gutterContextMenu"
    "focus"
    "blur"
    "scroll"
    "refresh"
    "optionChange"
    "scrollCursorIntoView"
    "update"
    "renderLine"
    "mousedown"
    "dblclick"
    "touchstart"
    "contextmenu"
    "keydown"
    "keypress"
    "keyup"
    "cut"
    "copy"
    "paste"
    "dragstart"
    "dragenter"
    "dragover"
    "dragleave"
    "drop"})

(def available-document-events
  #{"change"
    "beforeChange"
    "cursorActivity"
    "beforeSelectionChange"})

(s/def :code-mirror.events/editor (s/map-of available-editor-events fn?))
(s/def :code-mirror.events/document (s/map-of available-document-events fn?))
(s/def :code-mirror/events (s/keys :opt-un [:code-mirror.events/editor
                                            :code-mirror.events/document]))

(defn CodeMirror
  "CodeMirror React component.

   `configuration` is the 'vanilla configuration' (please see the official docs),
    but as a ClojureScript map, and with keyword keys instead.

   `on-mount` and `on-update` are a two-argument function (fn [component editor] ... )"
  [render & [{:keys [configuration events on-mount on-update]}]]
  (let [configuration (merge default-configuration configuration)

        codemirror-ref (atom nil)]
    (reagent/create-class
      {:display-name "CodeMirror"

       :component-did-mount
       (fn [this]
         (let [node (reagent.dom/dom-node this)
               configuration (merge default-configuration configuration)
               editor (codemirror node (clj->js configuration))]

           ;; --

           (when (seq events)
             (if (s/valid? :code-mirror/events events)
               (do
                 (doseq [[k f] (:editor events)]
                   ;; f : (fn [editor change])
                   (.on editor k f))

                 (doseq [[k f] (:document events)]
                   ;; f : (fn [document change])
                   (.on (.getDoc editor) k f)))
               (let [editor-keys-table (with-out-str
                                         (pprint/print-table
                                           [:event]
                                           (map
                                             (fn [k]
                                               {:event k})
                                             available-editor-events)))

                     document-keys-table (with-out-str
                                           (pprint/print-table
                                             [:event]
                                             (map
                                               (fn [k]
                                                 {:event k})
                                               available-document-events)))

                     error (str "Invalid CodeMirror events map."
                                "\n\nAvailable :editor event keys:\n" editor-keys-table
                                "\n\nAvailable :document event keys:\n" document-keys-table "\n")]
                 (js/console.error error (s/explain-str :code-mirror/events events)))))

           ;; --

           (when on-mount
             (on-mount this editor))

           (reset! codemirror-ref editor)))

       :component-did-update
       (fn [this _]
         ;; It's important to use this `on-update`
         ;; instead of the one passed to the outer function,
         ;; since props might be different from the initial mount state.
         (let [[_ _ {:keys [configuration on-update]}] (reagent/argv this)

               {:keys [value]} configuration

               scroll-info (.getScrollInfo @codemirror-ref)]

           (when-not (= value (cm-get-value @codemirror-ref))
             (cm-set-value @codemirror-ref value)
             (.scrollTo @codemirror-ref (.-left scroll-info) (.-top scroll-info)))

           (when on-update
             (on-update this @codemirror-ref))))

       :reagent-render
       (fn [_]
         render)})))
