# Interactive Sandbox

It's possible to create bespoken GUIs in the Sandbox
to interface with the Convex network, build documentation,
or talk to Smart Contracts.

The UI is described with Convex Lisp data structures, and it must be wrapped
in a Syntax object with a known set of keywords in its metadata:

```clojure
(syntax "Hello, world!" {:interact? true})
```

The presence of the `:interact?` keyword, in a Syntax's metadata, changes the semantics
of its value - in the context of the Sandbox. The Syntax's value is interpreted
as a language to describe an interactive interface.

These are the known keywords, by the Sandbox, which
can be attached to a Syntax object:

- `interact?`: boolean, used to change the semantics of a Syntax's value in the context of the Sandbox;
- `:cls?`: boolean, used to clear previous results;
- `:mode`: `:transaction` or `:query`, is used to set the mode for next commands;
- `:input`: string, is used to set the content of the editor;
- `:lang`: function, but string encoded, called with a Command's result; it's useful to wrap the result.

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

### Reference

#### Text

Short syntax:

```clojure
"Hello"
```

Hiccup syntax:

```clojure
[:text "Hello"]
```

#### Paragraph

Short syntax:

```clojure
["Hi there!"]
```

Hiccup syntax:

```clojure
[:p "Hi there!"]
```

#### Code

```clojure
[:code "(inc 1)"]
```

#### Markdown

```clojure
[:md "**Markdown** *content*"]
```

#### Command

**Command** widget defaults to `:transaction` mode,
and its action button is labeled with its source:

```clojure
[:cmd "(inc 1)"]
```

Set a **Command**'s mode:

```clojure
[:cmd {:mode :query} "(inc 1)"]
```

Set a **Command**'s name:

```clojure
[:cmd {:name "Increment 1"} "(inc 1)"]
```

Show a **Command**'s source:

```clojure
[:cmd {:show-source? true} "(inc 1)"]
```

#### Layout

A layout is a compound Widget and its children are also Widgets.

You can nest layouts:

```clojure
[:v-box
 [:text "Hello"]
 [:h-box
  [:text "Example 1"]
  [:cmd "(inc 1)"]]]
```

#### Horizontal layout

```clojure
[:h-box
  [:text "Example 1"]
  [:cmd "(inc 1)"]]
```

#### Vertical layout

```clojure
[:v-box
  [:text "Example 1"]
  [:cmd "(inc 1)"]]
```