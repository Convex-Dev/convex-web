# Introduction to Smart Contracts

## Storage Example

Define your contract initialization function:

```clojure
(defn storage-example-init []
  (def stored-data nil)

  (defn get []
    stored-data)

  (defn set [x]
    (def stored-data x))


  (export get set))
```

Deploy your contract, and assign its address to `storage-example-address`:

```clojure
(def storage-example-address (deploy storage-example-init))
```

Call your contract exported functions:

```clojure
(call storage-example-address (get))
;; => nil

(call storage-example-address (set 1))
;; => 1

(call storage-example-address (get))
;; => 1
```

## Subcurrency Example

```clojure
(defn subcurrency-example-init []
  (def owner *caller*)

  (defn contract-transfer [receiver amount]
    (assert (= owner *caller*))
    (transfer receiver amount))

  (defn contract-balance []
    *balance*)

  (export contract-transfer contract-balance))

(def subcurrency-example-address (deploy subcurrency-example-init))

(call subcurrency-example-address (contract-balance))
;; => 0

(call subcurrency-example-address (contract-transfer *address* 500))
```