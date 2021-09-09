A single character is prefixed by `\`:

```clojure
\C \o \n \v \e \x
```

Typically, single characters are scarcely used on-chain. Most often, strings of characters are used, encompassed between `" "`, commonly known as "text":

```clojure
"Convex"

(str? "This is text")  ;; -> true
```


## Casts

Internally, chars are encoded as longs data-type:

```clojure
(long \c)  ;; -> 99
(char 99)  ;; -> \c
```

Any value can be cast into a string representation and several values can be concatenated together:

```clojure
(str 42)
;; -> "42"

(str \C
     \o
     "nvex")
;; -> "Convex

(str "One is lesser than two: "
     (< 1 2))
;; -> "One is lesser than two: true"

```


## Not quite a collection

Although strings are collections of chars, in theory, they are not collections in the sense envisioned in further sections and data types such as [vectors](/cvm/data-types/vector) or
[maps](/cvm/data-types/map).

However, they are countable and it is possible to extract single chars:

```clojure
(count "Convex")  ;; -> 6

(nth "Convex" 0)  ;; -> \C
(nth "Convex" 1)  ;; -> \o
(nth "Convex" 2)  ;; -> \n
```
