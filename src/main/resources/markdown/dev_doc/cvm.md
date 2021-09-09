In the Convex network, each account stores data in its own environment, a place where arbitrary values can be defined. Values are used to describe anything such as any type of service, etc.
A transaction submitted over the network contains software code and the purpose of code is to describe how those values are created, modified, and retrieved. One account can execute its set of transactions (own code). And it can execute someone's else code only if that account has previously received authorisation to execute code (via digital signature). When a transaction is submitted, peers execute that transaction through the CVM (Convex Virtual Machine),
which deterministically follows submitted operations. Finally, Convex's consensus algorithm ensures peers converge towards the same result every single time, proving the transaction
was executed strictly as intended by the signing account.

```clojure
(def hello
     "world")
```

The following guides explore those steps in detail, from the very basics of creating an account to creating complex smart contracts executing automated operations.
While previous programming experience is desirable, it is not a prerogative. We care about demystifying the whole process and hope a broader audience will be empowered
to understand how such systems work.

All examples are written in Convex Lisp, the programming language used to encode transactions. Its unique design results in concise code, straight to the point,
less error-prone than many other programming languages, and we believe it is a fun experience to learn it. Snippets of code show practical examples and results are often written as
comments:

```clojure
(+ 2 2)
;; -> 4
```

Comments start with at least one `;`. Anything afterwards until the end of the line is ignored and not executed. They will be used to annotate examples and comment on important points of the business logic.

