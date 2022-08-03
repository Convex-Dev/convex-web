While [lists](/cvm/data-types/list) are more often used to describe code, vectors behave slightly different and have a broader usage.
Written between `[ ]`, they are collections where containing items of any type:

```clojure
[]
[:a 42 "text"]

(vector? [42])  ;; -> true
(coll?   [42])  ;; -> true
```

They are broadly used to group items together while preserving a known order, where items can have different types and be collections
themselves.

Unlike other programming languages, separating items with `,` is optional and rarely seen unless it makes an expression more readable.
Like any other value, a vector can never directly be altered. All examples below return a new vector in an efficient manner.



## Create a new vector

By using the literal notation:

```clojure
[:a :b]
```

By using a function:

```clojure
(vector :a :b)

;; -> [:a :b]
```

By adding an item to an existing vector:

```clojure
(conj [:a :b]
      42)

;; -> [:a :b 42]
;;
;; Items are always added at the end of the new vector.
;; Old one is left intact.
```

By replacing an item at a given position:

```clojure
(assoc [:a :b :c]
       1
       "here")

;; -> [:a "here" :c]


(assoc-in [:a [:b :c]]
          [1 0]
          "here")

;; -> [:a ["here" :c]]

```

By casting any other collection or pseudo-collection:

```clojure
(vec (list :a :b))   ;; -> [:a :b]
(vec "Convex")       ;; -> [\C \o \n \v \e \x]

(into [:a]
      (list :b :c))  ;; -> [:a :b :c]
```


## Accessing items

By retrieving the nthiest one (count starts at 0):

```clojure
(nth [:a :b :c]
     1)

;; -> :b


(nth [:a :b :c]
     42)

;; Error! Requested position beyond the limits of the vector.


([:a :b] 1)

;; -> :b
;;
;; Vectors can also behave like functions, which has the
;; the same effect as `nth`.
```

Similarly to what is described in [lists](/cvm/data-types/list), vectors also work with the `get` function.
The **key** of an item is also its position, explaining why `nth` and `get` are similar, but not identical:

```clojure
(get [:a :b :c]
     1)

;; -> :b


(get-in [:a [:b :c]]
        [1 0])

;; -> :b
;;
;; Nested `get`, akin to a matrix access: item at 1 is [:b :c],
;; then item at 0 is :b.


(get [:a :b :c]
     42)

;; -> nil
;;
;; Unlike `nth`, it does not produce an error when the accessed position
;; is beyond the limits of the vector.
```


## Sequence functions

Following functions can only be used with sequential collections ([lists](/cvm/data-types/list) or vectors) where order is predictable:

```clojure
(reverse [:a :b :c])

;; -> (:c :b :a)
;;
;; Returns a list for performance reasons.


(concat [:a :b]
        [:c])

;; -> [:a :b :c]
```


## Common collection functions

```clojure
(count [:a :b])       ;; -> 2
(empty? [])           ;; -> true, there are no items
(empty? [:a :b])      ;; -> false, there are 2 items
(empty [:a :b])       ;; -> [], an empty vector

(first [:a :b :c])    ;; -> :a
(second [:a :b :¢])   ;; -> :b
(last [:a :b :c])     ;; -> :c

(next [:a :b :c])     ;; -> (:b :c)
(next [:a])           ;; -> nil
```

Vectors can be looped over as described in the [section about loops](/cvm/building-blocks/loops).
