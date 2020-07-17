## Convex Lisp

The CVM includes a small, dynamically typed, embedded Lisp suitable for general purpose programming within the CVM environment.

Lisp was chosen as the first language implementation in Convex for the following reasons:

- It can be constructed using a very small number of axiomatic primitives, which in turn are based on the Lambda Calculus. This provides a robust logical and mathematical foundation, suitable for the type of verifiable, deterministic computations that the CVM must support.
- Lisp has a very simple regular syntax, homoiconic nature of code and ability to implement powerful macros. We hope this provides the basis for innovative new languages and domain-specific langauges (DSLs) on the CVM.
- Lisp compilers are small enough and practical enough to include as a capability within the CVM, avoiding the need for external compilers and tools to generate CVM code.
- It is comparatively simple to implement, reducing the risk of bugs in the CVM implementation (which may require a protocol update to correct).
- Lisp is well suited for interactive usage at a REPL prompt. This facilitates rapid prototyping and development of Actors in a way that we believe is a significant advantage for decentralised application builders looking to test and prototype new ideas.

Developers using the Convex system are not required to use Convex Lisp: It is perfectly possible to create alternative language front-ends that target the CVM.

Convex Lisp draws inspiration from Common Lisp, Racket and Clojure. It is designed as primarily a functional language, with fully immutable data structures, as it is our belief that functional programming forms a strong foundation for building robust, secure systems.