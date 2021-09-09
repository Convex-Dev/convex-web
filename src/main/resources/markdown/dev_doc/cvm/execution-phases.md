The [code is data](/cvm/code-is-data) section introduced the idea that Convex Lisp code starts as data. A few times, the `eval` function has been
mentioned, exposing how data can be executed:

```clojure
(eval (list '+ 2 2))

;; -> 4
```

This section reviews all steps involved in the process.


## Expansion

As described in the section about [macros](/cvm/macros), before execution, data representing code can be modified. The first step
is called **expansion** as the macro expands some data into another form of data by following defined macros.

This principle will remain opaque until more is learnt about macros but we can already notice that `when` is a macro and get a hint
as to what macros and expansion are:

```clojure
(def my-code
     '(when (< 1 2)
        :okay))

(list? my-code)

;; -> true, `my-code` is a list representing a `when` form


(def expanded
     (expand my-code))

;; -> (cond (< 1 2)
;;     (do :okay))


(list? expanded)

;; -> true
;;
;; Data representing `when` was mapped to a lower-level
;; representation based on `cond`.

```


## Compilation

After expansion, data can be compiled to an **op**, one of the fundamental operations forming the basic building blocks of how the CVM does
computation:

```clojure
(def my-addition
     '(+ 2 2))

(list? my-addition)

;; -> true


(def compiled
     (compile '(+ 2 2)))

(list? compiled)

;; -> false, data has been compiled into a proper operation.


(= 4
   compile)

;; -> false, operation is ready for execution but not yet
;;          executed
```

Compilation ensures that `expand` is performed if needed.


## Execution

A compiled operation is now ready to be executed and produce a result:

```clojure
(eval compiled)

;; -> 4, compiled operation from last example
```

The `eval` function takes care of expansion and compilation if required:

```clojure
(eval '(+ 2 2))

;; -> 4
```

**Attention.** Evaluating any arbitrary code is dangerous for the same reasons as outlined in the section about [functions](/cvm/functions).
In general, `eval` is only used in a limited set of circumstances by advanced users. It is not a common feature. 


## Learn more

For the client application, understanding these steps can lead to cost savings. The CVM hosts an on-chain compiler, a unique feature.
As such, a transaction can be submitted as pure data and the CVM can expand, compile, and ultimately execute that transaction.
However, expansion and compilation incur additional costs as they mean additional computation. Hence, by pre-compiling a transaction,
cost of transaction can significantly drop.

Additional technical documents for understanding thoroughly these steps:

- [Convex Architecture Design 005](https://github.com/Convex-Dev/design/tree/main/cad/005_cvmex) about execution model and operations
- [Convex Architecture Design 008](https://github.com/Convex-Dev/design/tree/main/cad/008_compiler) about compilation
- [Convex Architecture Design 009](https://github.com/Convex-Dev/design/tree/main/cad/009_expanders) about expansion
