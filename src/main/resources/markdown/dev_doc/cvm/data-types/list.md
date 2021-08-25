At this point, lists have already been encountered many times. Written between `( )`, they are a sequential collection of items where an item can be
a value of any type.

Up to now, they were used to call functions:

```clojure
(+ 1 2)

(transfer #42
          10000)
```

Indeed, when evaluating a list, the CVM considers that the first item is a function while other items are arguments. To prevent this mechanism, a list
can be constructed using a dedicated function:

```clojure
(list 1
      2
      (+ 1 3))

;; (1 2 3)
;;
;; Without any further evaluation.


(list? (list :a :b))  :: True
(coll? (list :a :b))  ;; True
```

Generally, [vectors](/cvm/data-types/vector) are more flexible for grouping several values together. Lists are more commonly used in the context of
[code as data](/cvm/code-as-data) and [macros](/cvm/macros) (an advanced topic). Items can have different types and be collections themselves.

Unlike other programming languages, separating items with `,` is optional and rarely seen unless it makes an expression more readable.


## Create a new list

By using a function:

```clojure
(list 1 2 3)
```

By using `quote` (explained in greater detail in the the [section about code as data](/cvm/code-as-data), which prevent evaluation:

```clojure
(quote (1 2 (+ 1 3)))

;; (1 2 (+ 1 3))
;;
;; Nothing is evaluated, not even the inner parens.
```

By adding an item to an existing list:

```clojure
(conj (list :a :b)
      42)

;; (42 :a :b)
;;
;; Items are always added at the beginning of the new list, old one is left intact.
```

By prepending an item to any other collection (a [map](/cvm/data-types/map), a [set](/cvm/data-types/set), a [vector](/cvm/data-types/vector), or another list):

```clojure
(cons 42
      [:a :b])

;; (42 :a :b)
```

By replacing an item at a given position:

```clojure
(assoc (list :a :b :c)
       1
       "here")

;; (:a "here" :c), old list left intact.


(assoc-in (list :a
                (list :b :c))
          [1 0]
          "here")

;; (:a ("here" :c)), old list left intact.

```

By casting any other collection or pseudo-collection:

```clojure
(into (list :a)
      [:b :c])

;; (:c :b :a)
;;
;; Seems upside-down but it makes sense, new items are always added at the beginning of the list.


(into (list)
      "Convex")

;; (\x \e \v \n \o \C)
```


## Access items

By retrieving nthiest one (count starts at 0):

```clojure
(nth (list :a :b :c)
     1)

;; :b


(nth (list :a :b :c)
     42)

;; Error, requested position is beyond what the list holds


((list :a :b :c) 1)

;; :b
;;
;; Lists can also behave like functions, which has the same effect as `nth`
```

In Convex Lisp, besides being sequential (known order from first to last item), lists are also considered associative. Each item is mapped to
a specific position. In practice, it means they can be used with the `get` function:

```clojure
(get (list :a :b :c)
     1)

;; :b
;;
;; Behaves like `nth`.


(get-in (list :a
              (list :b :c))
        [1 0])

;; :b
;;
;; Nested `get`: item at 1 is (:b :c), then item at 0 is :b


(get (list :a :b :c)
     42)

;; Nil
;;
;; Unlike `nth`, does not produce an error when accessed position is beyond the limits of the list.
;;
```


## Common collection functions

```clojure
(count (list :a :b))            ;; 2
(empty? (list))                 ;; True, there are no items
(empty? (list :a :b))           ;; False, there are 2 items
(empty (list :a :b))            ;; (), an empty list

(first (list :a :b :c))         ;; :a
(second (list :a :b :c)         ;; :b
(last (list :a :b :c))          ;; :c

(contains-key? (list :a :b :c)
               1)               ;; True
(contains-key? (list :a :b :c)
               42)              ;; False

(next (list :a :b :c))          ;; (:b :c)
(next (list :a))                ;; nil

(reverse (list :a :b :c))       ;; [:c :b :a], returns a vector for performance reasons

(concat (list :a :b)
        (list :c))              ;; (:a :b :c)

```

Lists can be looped over as described in the [section about loops](/cvm/loops).
