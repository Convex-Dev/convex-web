Very often, some task needs to be repeated. Programming languages typically offer several ways of executing a piece of code as long required.


## Recursion

The most basic form of repetition is [recursion](https://en.wikipedia.org/wiki/Recursion):

```clojure
(loop [i 5
       v []]
  (if (>= i
          0)
    v
    (recur (dec i)
           (conj v
                 i))))

;; [5 4 3 2 1 0]
```

This example uses `loop` to create local bindings, exactly like `let` described in [local definitions](/cvm/definitions). Bindings locally define
`i`, a counter starting from `5`, and `v`, an empty vector. As long as `i` is greater than or equal to `0`, `recur` is used to replace those bindings
with new values and re-execute code contained in `loop`. Each time, `i` is decremented (`1` is subtracted) and its value is also added to `v`. At some point,
`i` becomes lesser than 0 and `v` is returned according to `if`. As a result, `v` contains all numbers from `5` to `0`.

Similarly, a [named function](/cvm/functions) can apply itself:

```clojure
(defn to-0

  [i v]

  (if (>= i
          0)
    (to-0 (dec i)
          (conj v
                i))
    v))


(to-0 5)

;; [5 4 3 2 1 0]
```

Previous example has a major drawback. At some point, it might provoke a stack overflow. In simpler terms, functions applying functions are like nested russian dolls.
At some point, we run out of dolls to nest. Fortunately, akin to `loop`, functions create a recursion point relative to their parameters. Previously example can
be rewritten into this much more efficient version which uses `recur`:

```clojure
(defn to-0

  [i v]

  (if (>= i
          0)
    (recur (dec i)
           (conj v
                 i))
    v))


(to-0 5)

;; [5 4 3 2 1 0]
```

Following example defines a [multi-function](/cvm/functions) for elegantly computing a [factorial](https://en.wikipedia.org/wiki/Factorial):

```clojure
(defn factorial


  ([x]

   (factorial 1
              x))


  ([result x]

   (if (> x
          1)
     (recur (* result
               x)
            (dec x))
     result)))


(factorial 5)

;; 120
;;
;; Try to mentally unfold how it works.
```


## Reduce

Looping is often about producing a result by traversing items in a collection. While `loop` can be used for such case, `reduce` is provided as a convenient
and versatile alternative. It requires 3 elements:

- A collection, such as a [vector](/cvm/data-types/vector) or [map](/cvm/data-types/map)
- An initial result
- A function which takes the initial result, the first item in the collection, and produces an intermediary result ; then the process is repeated with all remaining items

```clojure
(reduce +
        0
        [1 2 3 4 5])

;; 15
;;
;; If we unfold the whole operation, here is what happens:
;;
;;   (+ 0 1)   ; 1
;;   (+ 1 2)   ; 3
;;   (+ 3 3)   ; 6
;;   (+ 6 4)   ; 10
;;   (+ 10 5)  ; 15
```

This variant sums only number greater than or equal to `0`, leaving the intermediary result intact otherwise:

```clojure
(reduce (fn [result item]
          (if (>= item
                  0)
            (+ result
               item)
            result))
        [-1 1 -10 2 3])

;; 6
```

Sometimes, not all items in a collection need to be processed. At any time, `reduced` can be used to result a final result. Remaining items will never be processed.
This variant stops summing numbers when it encounters `:stop`:

```clojure
(reduce (fn [result item]
          (if (= item
                 :stop)
            (reduced result)
            (+ result
               item)))
        [1 2 3 :stop 4 5])

;; 6
```
