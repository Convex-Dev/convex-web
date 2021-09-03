In Convex Lisp, code is first and foremost data. It can be composed of all the [data types described previously](/cvm/data-types/overview). The CVM takes data and
evaluates it in order to produce a result, which is data as well.

The following section will seem abstract at first. However, concepts exposed here are of great importance for understanding [actors](/cvm/actors), as well as
[macros](/cvm/macros). For instance, they are needed for writing code that can be deployed later and executed in the context of a smart contract.


## Quote

At this point, many examples have touched upon the idea of **evaluation**.

Literals are [data](/cvm/data-types) elements that evaluate to themselves:

```clojure
42       ;; Evaluates to 42, a long
"text"   ;; Evaluates to "text", a string
[:a :b]  ;; Evaluates to [:a :b], a vector
```

[Function](/cvm/functions) application consists of a list where the first item designates a function and other items are parameters:

```clojure
(+ 2 3)       ;; -> 5
[:a (+ 2 3)]  ;; -> [:a 5]
```

However, evaluation can be prevented by using `quote`:

```clojure
(quote (+ 2 3))       ;; -> (+ 2 3)
(quote [:a (+ 2 3)])  ;; -> [:a (+ 2 3)]


(1 2 3)

;; Error! 1 is not a function


(quote (1 2 3))

;; -> (1 2 3)
;;
;; A list with 3 items, not considered to be function application.
```

A quoted list remains a list, the CVM does not try to apply a function. Any quoted value remains as it is.

Quoting is so common for some situations that an actual **'** can be used as a shorthand for `quote`:

```clojure
'(+ 2 3)

;; -> (+ 2 3)


'[:a (+ 2 3)]

;; -> [:a (+ 2 3)


[:a
 (+ 2 3)
 '(+ 2 3)]

;; -> [:a 5 (+ 2 3)]
```


## Quasiquote and unquote

Templating code is an important feature, especially in the context of smart contracts. Once understood, it becomes
a powerful ally.

In that respect, `quasiquote` is very similar to `quote` as it prevents evaluation. However, it allows using `unquote`
for selectively evaluating and preparing bits of data where needed:

```clojure
(let [x 2]
  (quasiquote (+ 1
                 (unquote x)
                 (unquote (* 2 2)))))

;; -> (+ 1 2 4)
;;
;; List as a whole is not evaluated, only those bits
;; wrapped in `unquote` are.
```

As a shorthand, `quasiquote` can be written as **backtick** and `unquote` as a **tilde**:

```clojure
(let [x 2]
  `(+ 1
      ~x
      ~(* 2 2)))

;; -> (+ 1 2 4)
```

Alternatively, a simple example like that can be reproduced using the `list` function:

```clojure
(let [x 2]
  (list '+ 1 x (* 2 2)))

;; -> (+ 1 2 4)
```

However, it can quickly become difficult to work with, to read, and any non-trivial case will usually benefit from being
written with `quasiquote`.
