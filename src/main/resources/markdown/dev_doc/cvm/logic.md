The section about [booleans](/cvm/data-types/boolean) introduced 2 values: `true` and `false`. Alongside, 2 concepts: being **truthy** or being **falsey**. Those concepts
are crucial when for executing code only when some condition appears to be true.


## Control flow

Several ways are described for deciding which code to execute or which result to provide. In reality, they are all based on `cond` which works with pairs of test-results.
A **test** is an expression which must return a truthy value for its **result** to be executed and returned:

```clojure
(def a
     5)

(def b
     1)

(cond
  (< a b)  :lesser
  (== a b) :equal
  :else    :greater)

;; -> :greater, because `a` is neither :lesser than nor :equal to `b`
```

If a test (formatted  on the left side for clarity) returns a truthy value, then its corresponding result is selected and returned right away. Other potential results
are not even considered nor executed, as proven by this experiment:

```clojure
(cond
  false (def x 1))

x

;; Error! `x` is undefined


(cond
  false (def x 1)
  true  (def x 2)
  false (def x 3))

x

;; -> 2
```

If no test returns a truthy value, then the value returned by `cond` is [nil](/cvm/data-types/nil):

```clojure
(cond
  (< 10 1) :a
  (> 1 42) :b)

;; -> nil
```

A default value can be provided in case no test passes:

```clojure
(cond
  (< 10 1) :a
  (> 1 42) :b
  :my-default-value)

;; -> :my-default-value
```

Based on this idea is derived `if`, well-known in other programming languages:

```clojure
(if (< 1 2)
  :okay
  :not-okay)

;; -> :okay


(if (< 2 1)
  :okay
  :not-okay)

;; -> :not-okay
```

As well as `when` which executes one or several expressions if its test passes:

```clojure
(when (< 1 2)
  (def my-result
       true)
  (+ 10 5))

;; -> 15

my-result

;; -> true


(when (> 1 42)
  :passed)

;; -> nil
;;
;; When test returns false, nothing is executed in `when`
;; and nil is returned.
```


## Combining tests

`and` is used when several tests must return a truthy value. Any falsey value encountered is returned and there is no point in executing any other test:

```clojure
(and (< 1 2)
     (get {:a 42}
          :a))

;; -> :a


(and (get {:a 42}
          :b)
     :never-executed)

;; -> nil, the result of that `get`, a falsey value


(if (and (< 1 2)
         (> 10 5))
  :okay
  :not-okay)

;; -> :okay, both tests pass
```

`or` is used when at least one test among several ones must return a truthy value. It returns the first truthy value it finds:

```clojure
(or (get {:a 42}
         :b)
    :not-found)

;; -> :not-found, since `get` returns nil, a falsey value


(if (or (< 1000 5)
        (> 42 1))
  :okay
  :not-okay)

;; -> :okay, because (> 42 1) is true
```
