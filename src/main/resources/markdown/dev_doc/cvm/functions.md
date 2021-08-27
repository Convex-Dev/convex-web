At this point, functions have already been encountered many times. They form the basic building blocks of computation by taking values called **parameters**, executing
its **body** (one or several expression), and returning a value. However, up to now, they were blackboxes. This section reviews how users can create their own functions.


## Anonymous functions

Any function needs a [vector](/cvm/data-types/vector) of parameters and a body:

```clojure
(fn [x]
  (* x x))

;; Function producing a square by multipying a parameter `x` by itself.
```

Such a function is anonymous since it does not have a name. More precisely, it is not part of a [definition](/cvm/definitions).
It can be used right away as a first item in a list, as seen before:

```clojure
((fn [x] (* x x)) 2)

;; 4
```


## Definitions

Any anonymous function can be define so that is easily accessible and reusable.

A [definition](/cvm/definitions) allows a function to remain accessible across transactions by storing it in the environment of the
executing account:

```clojure
(def square
     (fn [x]
       (* x x)))

(square 4)

;; 16
```

This is so common that `defn` is provided as a shorthand:

```clojure
(defn square
  [x]
  (* x x))
```

Just like `def`, `defn` supports metadata, most notoriously used for documentation when relevant. More information about documentation
metadata can be found in [Convex Architecture Document 013](https://github.com/Convex-Dev/design/tree/main/cad/013_metadata).

```clojure
(defn square

  ^{:doc {:description "Returns the square of the given number."
          :examples    [{:code   "(square 4)"
                         :return "16"}]
          :signature   [{:params [x]}]}}

  [x]

  (* x
     x))
```

A function from another account can be applied, as described in the [section about definitions](/cvm/definitions). Supposing `square` is
defined in account `#42`:

```clojure
(#42/square 2)

;; Or

(def lib
     #42)

(lib/square 2)
```

**ATTENTION.** Applying functions from other accounts can be dangerous. For instance, such a foreign function could transfer all you coins
and assets. It is of the greatest importance to only use well-known functions from trusted accounts only. Just like you would not sign a contract
with a blank page, do not apply a function unless you know exactly what it does.


## Local definitions

A function can be defined temporarily as a [local definition](/cvm/definitions):

```clojure
(let [square (fn [x]
               (* x x))
      a      (square 3)]
  (+ a
     (square 4)))

;; 25

square

;; Error, `square` is undefind outside of `let`.
```


## Closures

Functions have the unique ability to close over values. In other words, local definitions that go out of scope when leaving `let` can still
be accessed by a function defined in that very same `let`:


```clojure
(def add-5
     (let [x 5]
       (fn [y]
         (+ y
            x))))

;; `def` is used this example.
;; A function "captures" local `x` and is defined under `add-5`.

x

;; Error! `x` is undefined, not accessible outside its `let`.

(add-5 42)

;; 47
;;
;; However, our function originally created in that same `let` is still able to access `x`.
```

Effectively, while not directly accessible, captured values are persisted in the decentralized database, akin to using `def`. They are removed
automatically when the function(s) that depend on them are themselves removed.

```clojure
(undef add-5)

;; Now, `x` is removed alongside `add-5`, automatically since it not used elsewhere.
```


## Higher-order functions

Some programming languages treat functions as specific black boxes. In Convex Lisp, functions remain values that can be passed around and
persisted in the decentralized database just like any [data type](/cvm/data-types/overview). For instance, functions can take other functions
as parameters, allowing for powerful data analysis and transformation.

A good example is `filter`, a standard function which removes items from a collection based on a given [predicate](/cvm/data-types/boolean)
(function that takes a parameter and returns a truthy or a falsey value):

```clojure
(filter (fn [x]
          (> x
             0))
        [-10 -5 0 1 2 3 -500])

;; [1 2 3]
;;
;; From the given vector were only kept numbers greater than 0 as specified by the given function.
```


## Multi-functions

Albeit not obvious at first, it is sometimes useful for a single function to have more than one implementation, more than one pair of parameters-body.
most of the time, such a multi-function is used to specify default values for optional parameters:

```clojure
(defn my-transfer

  ([address]
   (my-transfer address
                1000))

  ([address amount]
   (transfer address
             amount)))
```

Previous example specifies 2 implementations. Implementation with 2 arguments simply transfer some coins to an address. Implementation with 1 argument,
taking only an address, calls itself by providing a default value of `1000` coins to transfer. Both implementations are encompassed between `( )`.

In reality, it is as if 2 totally different functions were defined under the same name. Which is used depends solely on given parameters.
