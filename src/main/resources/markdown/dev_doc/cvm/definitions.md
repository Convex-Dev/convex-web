First and foremost, Convex can be understood as a decentralized database capable of storing [all data types described previously](/cvm/data-types/overview) as well
as [functions](/cvm/functions). This section reviews how arbitrary values can be stored across transactions, as long as required.

Each account has an **environment**. Conceptually, an environment can be understood as a [map](/cvm/data-types/map) where keys are symbols and values can be anything.
A symbol pointing to an arbitrary value is called a **definition**.


## Define a symbol

Creating a definition relies on using `def`:

```clojure
(def x
     42)
```

Once defined, a symbol can be used wherever relevant. During execution, any symbol encountered is resolved by looking in the environment of the executing account:

```clojure
(+ x 10)                    ;; 52

{:name "John Doe", :age x}  ;; {:name "John Doe", :age 42}
```

A defined symbol will be accessible across transactions as long as it is not undefined. When a symbol cannot be resolved in the environment, meaning it undefined,
an error occurs and the transaction is aborted.

A defined symbol can always be redefined:

```clojure
(def x
     10)

(def x
     42)

(+ x 1)

;; 43
```

Alternatively, `lookup` can be used to resolve a symbol. It is primarily useful for accessing a definition defined in another account:

```clojure
(lookup x)      ;; Resolving `x` in the account executing the transaction
(lookup #42 x)  ;; Resolving `x` in account #42
```


## Undefine a symbol

Undefining a symbol means removing its definition from the environment:

```clojure
(def x
     10)

(* 2 x)    ;; 20

(undef x)

(* 2 x)    ;; Error, `x` is now undefined.
```


## Attach metadata

An arbitrary [map](/cvm/data-types/map) can be attached to a definition as metadata. The purpose of metadata is to provide extra information about a definition, one
common example being documentation. It is done by providing a map prefixied with `^` before the defined value:

```clojure
(def mood

  ^{:doc {:description "Keyword defining my current mood."}}

  :happy)
```

Metadata data for a definition can be retrieved by providing a quoted symbol. Quoting is described in the [section about code as data](/cvm/code-as-data).

```clojure
(lookup-meta 'mood)  ;; {:doc {:description "Keyword defining my current mood."}}
```

Redefining a symbol do not erase its existing metadata unless a new map is provided explicitly:

```clojure
(def mood
     :super-happy)

(lookup-meta 'mood)

;; {:doc {:description "Keyword defining my current mood."}}


(def mood

  ^{:foo :bar}

  mood)

mood

;; :super-happy

(lookup-meta 'mood)

;; {:foo :bar}
```


## Local definitions

Often, it is useful storing values only temporarily. Local definitions using `let` are not persisted in the database, they are only accessible within its scope.
In a vector, values are provided for symbols which can be used in that local scope. Those are called **bindings**.

```clojure
(let [x 10]
  (* x x))

;; 100

(* x x)

;; Error, `x` is undefined now.
```

Several expressions can be executed in `let`:

```clojure
(let [x 10]
  (def my-x
       x)
  (+ x 5))

;; 15

my-x

;; 10
```

Latter bindings can rely on former bindings:

```clojure
(let [x 10
      y (* x x)]
  (+ x y))

;; 110
```
