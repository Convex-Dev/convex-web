While [lists](/cvm/data-types/list) are more often used to describe code, vectors behave slightly different and have a broader usage.
Written between square brackets, they are sequential and associative collections where containing items of any type:

```clojure
[]
[:a 42 "text"]

(vector? [42])  ;; True
(coll?   [42])  ;; True
```


## Create a new vector

By using the literal notation:

```clojure
[:a :b]
```

By using a function:

```clojure
(vector :a :b)
```

By adding an item to an existing list:

```clojure
(conj [:a :b]
      42)

;; [:a :b 42]
;;
;; Items are always added at the end of the new vector, old one is left intact.
```

By replacing an item at a given position:

```clojure
(assoc [:a :b :c]
       1
       "here")

;; [:a "here" :c]


(assoc-in [:a [:b :c]]
          [1 0]
          "here")

;; [:a ["here" :c]]

```

By casting any other collection or pseudo-collection:

```clojure
(vec (list :a :b))  ;; [:a :b]
(vec "Convex")      ;; [\C \o \n \v \e \x]
```


## Accessing items

By retrieving nthiest one (count starts at 0):

```clojure
(nth [:a :b :c]
     1)

;; :b


(nth [:a :b :c]
     42)

;; Error, requested position beyond the 


([:a :b] 1)

;; :a
;;
;; Vectors can also behave like functions, which has the same effect as `nth`.
```

Similarly to what is described in [lists](/cvm/data-types/list), vectors can also work with the `get` function:

```clojure
(get [:a :b :c]
     1)

;; :b


(get-in [:a [:b :c]]
        [1 0])

;; :b
;;
;; Nested `get`, akin to a matrix access: item at 1 is [:b :c], then item at 0 is :b.


(get [:a :b :c]
     42)

;; Nil
;;
;; Unlike `nth`, does not produce an error when accessed position is beyond the limits of the list.
```


## Common collection functions

```clojure
(count [:a :b])       ;; 2
(empty? [])           ;; True, there are no items
(empty? [:a :b])      ;; False, there are 2 items
(empty [:a :b])       ;; (), an empty vector

(first [:a :b :c])    ;; :a
(second [:a :b :¢])   ;; :b
(last [:a :b :c])     ;; :c

(next [:a :b :c])     ;; (:b :c)
(next [:a])           ;; nil

(reverse [:a :b :c])  ;; (:c :b :a), returns a list for performance reasons

(concat [:a :b]
        [:c])         ;; [:a :b :c]
```

Vectors can be looped over as described in the [section about loops](/cvm/loops).
