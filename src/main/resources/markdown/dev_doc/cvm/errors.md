When the CVM cannot continue execution because of an error, it throws an **exception**.

When such an exception occurs, it is returned immediately, the transaction is aborted, and any change that occured is simply discarded. Afterwards, it is
as if nothing happened.

Many programming languages offer a way of "catching" such exceptions, handling them, and resuming execution. However, not Convex Lisp. This is because in
practice, exception handling is typically poorly done and can lead to important bugs and errors, which intolerable in the context of a language designed
to efficiently compute blockchain transactions.

An exception contains 3 elements:

- A code, typically a keyword such as `:ASSERT` or `:STATE` but can be any value
- A message, typically a human readble string briefly explaining what happened by can be any value
- A stack trace (optional), a vector of string describing which functions where applied before throwing the exception

More information  is available in [Convex Design Document 005](https://github.com/Convex-Dev/design/tree/main/cad/011_errors).


## Unforeseen errors

Suppose the following situation is somehow reached:

```clojure
(+ [] 10)
```

It does not make any sense. A vector is not a number, it cannot be part of an addition. Such errors mean either a bug has been written in the logic,
leading to such absurd situations, or wrong values are being manipulated.


## Throwing an exception

We encourage users to think thoroughly about how their code might fail. Most bugs and hacks do not happen because code does not succeed as planned but
rather because code does not fail as it should. Hence, a "fail-fast" approach is promoted: if something feels off, it should fail as soon as possible.

`assert` throws an exception with code `:ASSERT` if any of its test does not pass (see [section about logic](/cvm/logic). It is used for checkin
if values (often [definitions](/cvm/definitions)) respect some conditions:

```clojure
(assert (< 1 2))

;; Nothing happens, true


(assert (get {:a 42}
             a)
        (> 42 1))

;; Nothing happens, both test return truthy values


(assert (> 1 1000))

;; Exception! test returns false
```

Alternatively, it is a good idea explicitly providing a code and a message as to better describe why an exception is thrown:

```clojure
(when-not (address? x)
  (fail :ARGUMENT
        "An address must be provided))
```
