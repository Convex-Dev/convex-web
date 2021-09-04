The section about [execution phases](/cvm/execution-phases) introduced the concept of expansion: code described as data can be mapped to some other data before
being compiled and executed.

While abstract at first, this concept is uniquely powerful as it allows to extend Convex Lisp, providing new capabilities and syntax. This section
exposes how to create and use macros which are specialized functions used during expansion.


The section about [logic](/cvm/logic) exposed `cond` for executing code selectively based on whether some conditions are true or not. On top of `cond`,
a series of features have been built to provide constructs found in other languages: `if`, `when`, `when-not`, etc. Those are perfect examples on how
basic capabilities can be extended to higher-level ones since they are simply macros built on top of `cond`.

A macro is written similarly to a function which takes some data and returns data representing code:

```clojure
(defmacro my-macro
  [data]
  data-2)
```

To showcase how much flexbility they can provide and how much they can extend the basic language, let us write a macro which transforms a common
expression written in **infix** notation to the **prefix** notation that Convex Lisp understands. For instance: `(a + b)` -> `(+ a b)`.

```clojure
(defmacro infix
  [param-1 operator param-2]
  (list operator
        param-1
        param-2))

(infix (2 + 2))

;; -> 4
```

When calling a macro, parameters are never evaluated since everything happens during expansion, before execution. This is why `(2 + 2)` does not
have to be quoted as described in the [code is data](/cvm/code-is-data) section.

For understanding what happens, `expand` reveals how data is mapped:

```clojure
(expand '(infix (2 + 2)))

;; -> (+ 2 2), a list
```

While interesting and powerful, macros can be complicated to write and regular functions are prefered most of the time. They remain a feature for
experts users and should be used only for:

- Writing new syntax or extending the language, requiring to work with unevaluated parameters
- Pre-computing values before compilation for avoiding repeatedly performing the same expensive computation at runtime

An additional constraint is that macros have to be defined in earlier transactions. A macro cannot be defined and used in the very same transaction.

More information can be found in [Convex Architecture Document 009](https://github.com/Convex-Dev/design/tree/main/cad/009_expanders).
