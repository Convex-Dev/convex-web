Maps associates a finite set of keys with a value for each. Written between `{ }`, they are associative collections formed by key-value pairs:

```clojure
{}

{:name   "Convex"
 :status :cool}

(map? {:a "b"})  ;; True
```

They are used abundantly due to their flexibility. When writing Convex Lisp, sooner than later appears the need to map a value to another value.
Keys and values can be of any type, collections as well. However, as seen in the following examples, it is very common to use [keywords](/cvm/data-types/keyword)
as keys.

Unlike [lists](/cvm/data-types/list) and [vectors](/cvm/data-types/vector), maps do not have a predictable order. However, order of key-values is stable:
when working with a given map, key-values always remain at their position.

Unlike other programming languages, separating items with `,` is optional and rarely seen unless it makes an expression more readable:

```clojure
{:name "Convex", :status :cool}
```


## Create a new map

By using the literal notation:

```clojure
{"this is" :a-map}
```

By using a function:

```clojure
(hash-map "this is" :a-map)

;; {"this is" :a-map}
```

By associating a value to a key (new or existing one):

```clojure
(assoc {:name "Convex"}
       :status
       :cool)

;; {:name "Convex", :status :cool}


(assoc-in {:store {:apple {:quantity 10, :price 42}}}
          [:store
           :apple
           :price]
          60)

;; {:store {:apple {:quantity 10, price 60}}}
;;
;; Nested `assoc` altering the price of an apple, a "path" of keys is provided as a vector.


(conj {:name "Convex"}
      [:status :cool])

;; {:name "Convex", :status :cool}
;;
;; In more advanced use cases, using `conj` with key-values pairs can be useful.
;; Key-value pairs are written as vectors of 2 items: [Key Value].


(into {:name "Convex"}
      [[:status :cool]
       [:blockchain? true]])

;; {:name "Convex", :status :cool, :blockchain? true}
;;
;; Similarly to `conj`, a sequence of key-values can be added at once using `into`.
```

By removing a key-value pair from an existing map:

```clojure
(dissoc {:name "Convex", :status :cool}
        :name)
        
;; {:status :cool}
```


# Accessing values

By retrieving nthiest one (count starts at 0). Remmember that order in maps is unpredictable, however it is stable until a key-value
is added or removed. Hence, `nth` can be used in [loops](/cvm/loops):

```clojure
(nth {:name   "Convex"
      :status :cool}
     1)

;; [:status cool]


(nth {:a :b}
     42)

;; Error, requested position beyond the limits of the map.


([:a :b] 1)

;; :a
;;
;; Vectors can also behave like functions, which has the same effect as `nth`.
```

By requesting a key:

```clojure
(get {:name "Convex"}
     :name)

;; "Convex"


(get {:name "Convex"}
     :status)

;; nil


(get-in {:store {:apple {:price 60, quantity 100}}}
        [:store
         :apple
         :price])

;; 60
;;
;; Nested `get`, follows a "path" of keys up to the price of an apple.
```


## Common collection functions

```clojure
;; Defines a map under `m`, so that it is easier following examples.
;;
(def m
     {:name        "Convex"
      :status      :cool
      :blockchain? true})
```

```clojure
(count m)       ;; 3


(empty? {})     ;; True, there are no items
(empty? m)      ;; False, there are 2 items
(empty m)       ;; (), an empty vector

;; Order is unpredictable, but stable

(first m)       ;; [:blockchain? true]
(second m)      ;; [:name "Convex"]
(last m)        ;; [:status :cool]

(next {})       ;; nil
(next {:a :b})  ;; nil
(next m)        ;; [[:name "Convex"], [:blockchain? true]]  ; remaining key-values after removing the first one

(keys m)        ;; [:blockchain? :name :status]
(values m)      ;; [true "Convex" :cool]         ;; Order is consistent with `keys`
```

Maps can be looped over as described in the [section about loops](/cvm/loops).
