Maps associates a finite set of keys with a value for each. Written between `{ }`, they are associative collections formed by key-value pairs:

```clojure
{}

{:name   "Convex"
 :status :cool}

(map? {:a "b"})  ;; -> true
```

They are used abundantly due to their flexibility. When writing Convex Lisp, sooner than later appears the need to map a value to another value.
Keys and values can be of any type, collections as well. However, as seen in the following examples, it is very common to use [keywords](/cvm/data-types/keyword)
as keys.

Unlike [lists](/cvm/data-types/list) and [vectors](/cvm/data-types/vector), maps do not have a predictable order. However, it is stable, meaning
any item within a map will always stay at its position.

Unlike other programming languages, separating items with `,` is optional and rarely seen unless it makes an expression more readable:

```clojure
{:name "Convex", :status :cool}
```

Like any other value, a map can never directly be altered. All examples below return a new map in an efficient manner.


## Create a new map

By using the literal notation:

```clojure
{"this is" :a-map}

;; Collections and complex items can be keys as well.
{{:complex "key"} [\a 42]}
```

By using a function:

```clojure
(hash-map "this is" :a-map)

;; -> {"this is" :a-map}
```

By associating a value to a key (new or existing one):

```clojure
(assoc {:name "Convex"}
       :status
       :cool)

;; -> {:name "Convex", :status :cool}
;;
;; Old map is left intact.


(assoc-in {:store {:apple {:quantity 10, :price 42}}}
          [:store
           :apple
           :price]
          60)

;; -> {:store {:apple {:quantity 10, price 60}}}
;;
;; Nested `assoc` altering the price of an apple, a "path" of
;; keys is provided as a vector.


(conj {:name "Convex"}
      [:status :cool])

;; -> {:name "Convex", :status :cool}
;;
;; In more advanced use cases, using `conj` with key-values
;; pairs can be useful.
;; Key-value pairs are written as vectors of 2 items: [Key Value].


(into {:name "Convex"}
      [[:status :cool]
       [:blockchain? true]])

;; -> {:name "Convex", :status :cool, :blockchain? true}
;;
;; Similarly to `conj`, a sequence of key-values can be added
;; at once using `into`.
```

By removing a key-value pair from an existing map:

```clojure
(dissoc {:name "Convex", :status :cool}
        :name)
        
;; -> {:status :cool}


(dissoc {:a "a", :b "b"}
        :c)

;; -> {:a "a", :b "b"}
;;
;; Removing a key which is not present in a map does nothing.
```


# Accessing values

By retrieving nthiest one (count starts at 0). Remember that order in maps is unpredictable. However it is stable.
Hence, `nth` can be used in [loops](/cvm/loops):

```clojure
(nth {:name   "Convex"
      :status :cool}
     1)

;; -> [:status cool]


(nth {:a :b}
     42)

;; Error! Requested position beyond the limits of the map.
```

By requesting a key:

```clojure
(get {:name "Convex"}
     :name)

;; -> "Convex"


(get {:name "Convex"}
     :status)

;; -> nil


(get-in {:store {:apple {:price 60, quantity 100}}}
        [:store
         :apple
         :price])

;; -> 60
;;
;; Nested `get`, follows a "path" of keys up to the price of
;; an apple.


({:x 42} :x)

;; -> 42
;;
;; Maps can also behave like functions, which has the same
;; effect as `get`.
```


## Map functions

Following functions only works with maps:

```clojure
(keys m)        ;; -> [:blockchain? :name :status]
(values m)      ;; -> [true "Convex" :cool]
                ;; Order of `keys` is consistent with `values`

(merge {:a :b}
       {1 2}    ;; -> {:a :b, 1 2}
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
(count m)       ;; -> 3


(empty? {})     ;; -> true, there are no items
(empty? m)      ;; -> false, there are 2 items
(empty m)       ;; -> {}, an empty map

;; Order is unpredictable, but stable
;;
(first m)        ;; -> [:blockchain? true]
(second m)      ;; -> [:name "Convex"]
(last m)        ;; -> [:status :cool]

(next {})       ;; -> nil
(next {:a :b})  ;; -> nil
(next m)        ;; -> [[:name "Convex"], [:blockchain? true]]
                ;; Remaining key-values after removed the
                ;; first one
```

Maps can be looped over as described in the [section about loops](/cvm/loops).
