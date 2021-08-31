On the Convex network, each account stores data in its environment, a place where arbitrary values can be defined. Values are used to describe anything, any type of service.
A transaction submitted on the network contains code and the purpose of code is to describe how those values are created, modified, and retrieved. No code can be executed
on behalf of an account unless it has been signed by that account only. When a transaction is submitted, peers execute it through the CVM (Convex Virtual Machine)
which predictably and blindly follows submitted operations. Finally, the consensus algorithm ensures that peers converge towards the same result, proving the transaction
was executed strictly as intended by the signing account.

The following guides explore those steps in detail, from the very basics of creating an account to creating complex smart contracts executing automated operations.
While previous programming experience is desirable, it is not a prerogative. We care about demystifying the whole process and hope a broader audience will be empowered
to understand how such systems work.

All examples are written in Convex Lisp, the programming language used to encode transactions. Its unique design results in code that is concise, straight to the point,
less error-prone than many other languages, and we believe it is a fun experience to learn it. Snippets of code show pratical examples and results are often written as
comments:

```clojure
(+ 2 2)

;; -> 4

;; Comments start with at least one ';'
;; They are never executed, there are often used to provide explainations in human language.
;; Everything writen after ';' until the end of the line effectively ignored.
```
