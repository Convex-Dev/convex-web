First and foremost, Convex can be understood as a decentralized database capable of storing **cells**. A cell can be any type of values encountered in these guides,
from [data types](/cvm/data-types) to [functions](/cvm/building-blocks/functions). This section reviews how arbitrary cells can be stored across transactions, as long as required.

Each account has an **environment**. Conceptually, an environment can be understood as a [map](/cvm/data-types/map) where keys are [symbols](/cvm/data-types/symbol)
and values can be anything. A symbol pointing to an arbitrary value is called a **definition**.


## Define a symbol

Creating a definition relies on using `def`:

```clojure
(def x
     42)
```

Once defined, a symbol can be used wherever relevant. During execution, any symbol encountered is resolved by looking in the environment of the executing account:

```clojure
(+ x 10)                    ;; -> 52

{:name "John Doe", :age x}  ;; -> {:name "John Doe", :age 42}
```

A defined symbol will be accessible across transactions as long as it is not undefined. When a symbol cannot be resolved in the environment, meaning it is undefined,
an error occurs and the transaction is aborted.

A defined symbol can always be redefined:

```clojure
(def x
     10)

(def x
     42)

(+ x 1)

;; -> 43
```

Alternatively, `lookup` can be used to resolve a symbol. It is primarily useful for accessing a definition defined in another account:

```clojure
(lookup x)

;; Resolving `x` in the account executing the transaction


(lookup #42 x)

;; Resolving `x` in account #42


#42/x

;; Same, but shorter


(def addr
     #42)

addr/x

;; Addresses can be defined as well.


(lookup (get-address) x)

;; Here, address is fetched by applying a supposedly defined function
```


## Undefining a symbol

Undefining a symbol means removing its definition from the environment of the executing account:

```clojure
(def x
     10)

(* 2 x)

;; -> 20


(undef x)

(* 2 x)

;; Error! `x` is now undefined.
```


## Attaching metadata

An arbitrary [map](/cvm/data-types/map) can be attached to a definition as metadata. The purpose of metadata is to provide extra information about a definition. For example, we can use metadata for documenting our code. This is done by providing a map prefixed with `^` before the defined value:

```clojure
(def mood
  ^{:doc {:description "Keyword defining my current mood."}}
  :happy)
```

Metadata data for a definition can be retrieved by providing a quoted symbol. Quoting is described in the section about [code is data](/cvm/building-blocks/code-is-data).

```clojure
(lookup-meta 'mood)

;; -> {:doc {:description "Keyword defining my current mood."}}
```

Redefining a symbol does not erase its existing metadata unless a new map is provided explicitly:

```clojure
(def mood
     :super-happy)

(lookup-meta 'mood)

;; -> {:doc {:description "Keyword defining my current mood."}}


(def mood
  ^{:foo :bar}
  mood)

mood

;; -> :super-happy, we did not change the value


(lookup-meta 'mood)

;; -> {:foo :bar}, we did change the metadata
```


## Local definitions

Often, it is useful storing values only temporarily. Local definitions using `let` are not persisted in the database, they are only accessible within its scope.
In a vector, values are provided for symbols, which can be used in that local scope. Those are called **bindings**.

```clojure
(let [x 10]
  (* x x))

;; -> 100


(* x x)

;; Error! `x` is undefined now outside of its `let`.
```

Several expressions can be executed in `let`:

```clojure
(let [x 10]
  (def my-x
       x)
  (+ x 5))

;; -> 15

my-x

;; -> 10
```

Latter bindings can rely on former bindings:

```clojure
(let [x 10
      y (* x x)]
  (+ x y))

;; -> 110
```
