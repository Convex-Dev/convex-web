Very often, some task needs to be repeated. Programming languages typically offer several ways of repeating a piece of code as long as required.


## Recursion

The most basic but versatile form of repetition is [recursion](https://en.wikipedia.org/wiki/Recursion):

```clojure
(loop [i 5
       v []]
  (if (< i
         1)
    v
    (recur (dec i)
           (conj v
                 i))))

;; [5 4 3 2 1]
```

Starting from `5` and an empty [vector](/cvm/data-types/vector), previous example uses `loop` and `recur` to create an vector of numbers
from `5` to `1`.

First, local bindings are created exactly as described in [local definitions](/cvm/definitions?section=Local%20definitions). Initially, symbol
`i` points to `5` while symbol `v` points to an empty vector. `if` at some point `i` becomes lesser than `1`, then `v` is returned and looping
stops. Otherwise, `recur` is used to updates bindings with new values and start again.

On each iteration, `i` is decremented on each iteration (`1` is subtracted) meaning it will become lesser than `1` at some point. Meanwhile,
the value of `i` is also stored in the vector `v` each time. Overall, this is what happens with the bindings:

```clojure
[i 5
 v []]

;; (< 5 1)? No, then continue

[i 4
 v [5]]

;; (< 4 1)? No, then continue

[i 3
 v [5 4]]

;; (< 3 1)? No, then continue

[i 2
 v [5 4 3]]

;; (< 2 1)? No, then continue

[i 1
 v [5 4 3 2]]

;; (< 1 1)? No, then continue

[i 0
 v [5 4 3 2 1]]

;; (< 0 1)? Yes! Return `v`
;;
;; -> [5 4 3 2 1]
```

Similarly, a defined [function](/cvm/functions) can apply itself:

```clojure
(defn to-1

  [i v]

  (if (< i
         1)
    v
    (to-1 (dec i)
          (conj v
                i))))


(to-1 5)

;; -> [5 4 3 2 1]
```

The previous example has a major drawback. At some point, it might provoke a stack overflow. In simpler terms, functions applying functions are like nested Russian dolls.
At some point, we run out of dolls to nest. Fortunately, akin to `loop`, functions create a recursion point relative to their parameters. Previously example can
be rewritten into this much more efficient version which uses `recur`:

```clojure
(defn to-1

  [i v]

  (if (< i
         1)
    v
    (recur (dec i)
           (conj v
                 i))))


(to-1 5)

;; -> [5 4 3 2 1]
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

;; -> 120
;;
;; Try to mentally unfold how it works.
```

Any examples in this section can be written in terms of `loop` and `recur`. However, when relevant, we recommend using the following constructs.


## Reduce

Looping is often about producing a result by traversing items in a collection. While `loop` can be used for such cases, `reduce` is provided as a convenient
and versatile alternative. It requires 3 elements:

- A collection, such as a [vector](/cvm/data-types/vector) or [map](/cvm/data-types/map)
- An initial result
- A function that takes the initial result, the first item in the collection, and produces an intermediary result; then the process is repeated with all remaining items

```clojure
(reduce +
        0
        [1 2 3 4 5])

;; -> 15
;;
;; If we unfold the whole operation, here is what happens:
;;
;;   (+ 0 1)   ; 1
;;   (+ 1 2)   ; 3
;;   (+ 3 3)   ; 6
;;   (+ 6 4)   ; 10
;;   (+ 10 5)  ; 15
```

This variant sums only numbers greater than or equal to `0`, leaving the intermediary result intact otherwise:

```clojure
(reduce (fn [result item]
          (if (>= item
                  0)
            (+ result
               item)
            result))
        [-1 1 -10 2 3])

;; -> 6
```

Sometimes, not all items in a collection need to be processed. At any time, `reduced` can be used to return a final result. The remaining items will never be processed.
This variant stops summing numbers when it encounters `:stop`:

```clojure
(reduce (fn [result item]
          (if (= item
                 :stop)
            (reduced result)
            (+ result
               item)))
        [1 2 3 :stop 4 5])

;; -> 6
```


## Transforming collections

A few standard ways of transforming collections are provided for common use cases:

Transforming all items in a collection:

```clojure
(map (fn [x]
       (* x x))
     [1 2 3])

;; -> [1 4 9]
;;
;; Always returns a vector of squares.
;; Not to be confused with `hash-map` which creates a map data type.
```

Keeping only selected items:

```clojure
(filter (fn [x]
          (> x
             0))
        [-10 -5 0 1 2 3 -500])

;; -> [1 2 3]
;;
;; Always returns a vector.
;; From the given vector were only kept numbers greater than 0
;; as specified by the given function.
```
