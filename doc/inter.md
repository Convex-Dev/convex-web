# Interactive Sandbox

## Widgets

Widgets are written in a Hiccup-like syntax, but most of them have a shorter syntax too.

Hiccup is a compact syntax already, but since the value is stored on chain,
it's important to try to make it shorter where possible.

A Widget's syntax is always expanded, if neeed, to its canonical form, on the server.
The canonical syntax is then conformed to a spec, and its value is sent to the client.
It's up to the client to decide how to render a Widget.

Example of conformed text Widget:

```clojure
"Hello" ;; or [:text "Hello"]

;; is expanded to:

{:tag :text
 :content [[:string "Hello"]]}
```

### Text

```clojure
"Hello"

;; => {:tag :text :content [[:string "Hello"]]}

;; Alternative syntax / canonical format:
;; => [:text "Hello"]
```

### Code

```clojure
(quote (inc 1))

;; => [:code "(inc 1)"]
```

### Markdown

```clojure
[:md "Markdown *content*"]
```

### Label 

```clojure
[:label "Small text"]

;; => [:text {:style :label} "Small text"]
```

### Query

```clojure
[:q '(inc 1)]
```

```clojure
[:q {:runnable? true}
 '(inc 1)]
```

### Transaction

```clojure
[:tx '(inc 1)]
```

### Layout

A layout is a compound Widget and its children are also Widgets.

You can nest layouts:

```clojure
[:v-box
 "Hello"
 [:v-box
  "Example 1"
  [:q '(inc 1)]]]
```

#### Horizontal layout

```clojure
["Hello" "World"]

;; => [:h-box [:text "Hello"] [:text "World"]]
```

#### Vertical layout

```clojure
[:v-box "Hello" "World"]

;; => [:v-box [:text "Hello"] [:text "World"]]
```