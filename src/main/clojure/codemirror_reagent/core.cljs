(ns codemirror-reagent.core
  (:require [cljs.pprint :as pprint]
            [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [reagent.dom :as dom]

            ["codemirror" :as codemirror]
            ["codemirror/mode/javascript/javascript"]
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

(defn extra-keys
  "Mapping of key name to key handler function.

   Key handler function is an unary function which gets passed a CodeMirror
   instance.

   Reference:

   The values of properties in key maps can be either functions of a single
   argument (the CodeMirror instance), strings, or false. Strings refer to
   commands, which are described below. If the property is set to false,
   CodeMirror leaves handling of the key up to the browser. A key handler
   function may return CodeMirror.Pass to indicate that it has decided not to
   handle the key, and other handlers (or the default behavior) should be given
   a turn."
  [{:keys [enter shift-enter ctrl-up ctrl-down ctrl-backspace]}]
  (merge {}
         (when enter
           {"Enter" enter})
         (when shift-enter
           {"Shift-Enter" shift-enter})
         (when ctrl-up
           {"Ctrl-Up" ctrl-up})
         (when ctrl-down
           {"Ctrl-Down" ctrl-down})
         (when ctrl-backspace
           {"Ctrl-Backspace" ctrl-backspace})))

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

(defn set-cursor-at-the-end [^js cm]
  (.setCursor cm (.lineCount cm) 0))

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
  "CodeMirror Reagent component.

   `configuration` is the 'vanilla configuration' (please see the official docs),
    but as a ClojureScript map, and with keyword keys instead.

   `on-mount` and `on-update` are a two-argument function (fn [component editor] ... )"
  [render & [{:keys [configuration events on-mount on-update]}]]
  (let [configuration (merge default-configuration configuration)

        codemirror-ref (atom nil)]
    (r/create-class
      {:display-name "CodeMirror"

       :component-did-mount
       (fn [this]
         (let [node (dom/dom-node this)
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
         (let [[_ _ {:keys [configuration on-update]}] (r/argv this)

               {:keys [value mode]} configuration

               scroll-info (.getScrollInfo @codemirror-ref)]

           (when mode
             (.setOption @codemirror-ref "mode" mode))

           (when-not (= value (cm-get-value @codemirror-ref))
             (cm-set-value @codemirror-ref value)
             (.scrollTo @codemirror-ref (.-left scroll-info) (.-top scroll-info)))

           (when on-update
             (on-update this @codemirror-ref))))

       :reagent-render
       (fn [_]
         render)})))
