At this point, lists have already been encountered many times. Written between parens, they are a sequential collection of items where an item can be
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


(list? (list :a :b))

;; True
```

Alternatively, the [section about code as data](/cvm/code-as-data) describes **quoting** which prevent any evaluation at all:

```clojure
(quote (1 2 (+ 1 3)))

;; (1 2 (+ 1 3))
;;
;; Nothing is evaluated, not even the inner parens.
```

Generally, [vectors](/cvm/data-types/vector) are more flexible for grouping several values together. Lists are more commonly used in the context of
[macros](/cvm/macros), an advanced topic.


## Key usage

Create a new list by adding an element to an existing list:

```clojure
(conj (list :a :b)
      42)

;; (42 :a :b)
;;
;; Items are always added at the beginning of the new list.
```

A new list can also be constructed by prepending an item to any other collection (a [map](/cvm/data-types/map), a [set](/cvm/data-types/set),
a [vector](/cvm/data-types/vector), or another list):

```clojure
(cons 42
      [:a :b])

;; (42 :a :b)
```

Retrieves the nthiest item (starting at 0):

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


(get (list :a :b :c)
     42)

;; Nil
;;
;; Unlike `nth`, does not produce an error when accessed position is beyond the limits of the list.
;;
```

Common collection functions:

```clojure
(empty? (list))            ;; True, there are no items
(empty? (list :a :b))      ;; False, there are 2 items
(empty (list :a :b))       ;; (), an empty list

(first (list :a :b :c))    ;; :a
(second (list :a :b :c)    ;; :b
(last (list :a :b :c))     ;; :c

(reverse (list :a :b :c))  ;; (:c :b :a)
```
