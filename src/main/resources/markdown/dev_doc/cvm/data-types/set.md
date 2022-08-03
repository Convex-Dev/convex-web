Sets are unordered collections of unique items, meaning a given item cannot appear twice in the same set. They are used for efficiently checking if
an item is a member of a set or not. Written between `#{ }`, items can be of different types, collections as well:

```clojure
#{:a 2 "three" [:four "five"]}

(set? #{:a :b})  ;; -> true
```

Unlike other programming languages, separating items with `,` is optional and rarely seen unless it makes an expression more readable.
Like any other value, a set can never directly be altered. All examples below return a new set in an efficient manner.


## Create a new set

By using the literal notation:

```clojure
#{:a :b}
```

By using a function:

```clojure
(hash-set :a :b)

;; -> #{:a :b}
```

By adding an item to an existing set:

```clojure
(conj #{:a}
      :b)

;; -> #{:a :b}
;;
;; Original set is left intact.


(conj #{:a}
      :a)

;; -> #{:a}
;;
;; Adding an item already present in the set does nothing.


(into #{:a}
      [:b :c :d])

;; -> #{:a :b :c :d}
```

By removing an item from an existing set:

```clojure
(disj #{:a :b}
      :b)

;; -> #{:a}


(disj #{:a :b}
      :c)

;; -> #{:a :b}
;;
;; Removing an item that was not present in the set
;; does nothing.
```


## Check if an item is present

By using the `get` function:

```clojure
(get #{:a :b}
     :a)

;; -> true


(get #{:a :b}
    :c)

;; -> false
```

By using the `contains-key?` function. Conceptually, sets are like [maps](/cvm/data-types/map) where items are keys and
values are `true` or `false`, depending on whether an item is present or not:

```clojure
(contains-key? #{:a :b}
               :a)

;; -> true


(contains-key? #{:a :b}
               :c)

;; -> false
```

By using the set as a function:

```clojure
(#{:a :b} :a)  ;; -> true
(#{:a :b} :c)  ;; -> false
```


## Set functions

Common set functions are available.

Creates a set of all items in the first set but not in others:

```clojure
(difference #{1 2 3 4}
            #{2 4})

;; -> #{1 3}


(difference #{1 2 3 4}
            #{4}
            #{2 4})

;; -> #{1 3}
```

Combine all sets together:

```clojure
(union #{1 2 4}
       #{3}
       #{3 5})

;; -> #{1 2 3 4 5}
```

Creates a set of all items found in all given sets:

```clojure
(intersection #{1 2 3}
              #{2 3 4 5})

;; -> #{2 3}


(intersection #{1 2 3}
              #{2 5 6}
              #{1 2})

;; -> #{2}
```

Check whether a set is a subset of another one (all items in the first set are present in the second one):

```clojure
(subset? #{1 3}
         #{1 2 3 4})

;; -> true


(subset? #{1 3}
         #{2 3 4})

;; -> false
```


## Common collection functions

```clojure
(count #{:a :b :c})   ;; -> 3

(empty? #{})          ;; -> true, there are no items
(empty? #{:a :b})     ;; -> false, there are 2 items
(empty #{:a :b})      ;; -> (), an empty vector

;; Order is unpredictable, but it is stable

(first #{:a :b :c})    ;; -> :c
(second #{:a :b :c})  ;; -> :a
(last #{:a :b :c})    ;; -> :b

(next #{})            ;; -> nil
(next #{:a})          ;; -> nil
(next #{:a :b :c})    ;; -> [:a :b]
                      ;; Vector of remaining items after
                      ;; removing the first one
```

Sets can be looped over as described in the [section about loops](/cvm/building-blocks/loops).
