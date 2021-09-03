When the CVM cannot continue execution because of an error, an **exception** is thrown.

When such an exception occurs, it is returned immediately, the transaction is aborted, and any change that occured is simply discarded. Afterwards, it is
as if nothing happened.

Many programming languages offer a way of *catching* such exceptions, handling them, and resuming execution. However, not Convex Lisp. This is because in
practice, exception handling is typically poorly done and can lead to important bugs and errors, which is intolerable in the context of a language designed
to efficiently compute blockchain transactions.

Instead, a *let it crash* approach is promoted. This section reviews what exceptions are and how they can also be created on purpose.

An exception contains 3 elements:

- A **code**, typically a keyword such as `:ASSERT` or `:STATE` but can be any value
- A **message**, typically a human readble string briefly explaining what happened but can be any value
- A **stack trace** (optional), a vector of strings describing which functions where applied before throwing the exception

Detailed information is available in [Convex Design Document 005](https://github.com/Convex-Dev/design/tree/main/cad/011_errors).


## Unforeseen errors

Suppose the following situation is somehow reached:

```clojure
(+ [] 10)
```

It does not make any sense. A vector is not a number, it cannot be part of an addition with a number. Such errors mean either a bug has been written
in the logic, leading to such absurd situations, or wrong values are being manipulated. Hence, the only logical thing to do for the CVM is to throw
an exception as it cannot go any further.


## Throwing an exception

We encourage users to think thoroughly about how their code might fail. Most bugs and hacks do not happen because code does not succeed as planned but
rather because code does not fail as it should. Hence, a *fail fast* approach is recommmended: if something feels off, it should fail as soon as possible.

`assert` throws an exception with code `:ASSERT` if any of its logical test does not pass (see section about [logic](/cvm/logic). It is used for ensuring
that some conditions are respected, typically involving [definitions](/cvm/definitions) of any kind:

```clojure
(assert (< 1 2))

;; Nothing happens, true


(assert (get {:a 42}
             a)
        (> 42 1))

;; Nothing happens, both test return truthy values


(assert (> 1 1000))

;; Exception! 1 is not greater than 1000, condition is not respected.
```

Alternatively, it is a good idea explicitly providing a code and a message as to better describe why an exception is thrown:

```clojure
;; Given a definition `some-amount` representing some amount
;; to transfer.
;;
(when-not (< some-amount
             1000)
  (fail :FUNDS
        "Insufficient funds."))
```
