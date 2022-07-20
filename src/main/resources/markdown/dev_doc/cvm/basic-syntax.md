Suppose this simple computation:

```
4 + 2 * 3
```

Simple maths taught at elementary school dictates the answer is `10`, since the `*` operator has higher precedence than the `+` operator. Hence, `2 * 3` is executed first,
and then `4 + 6`. Beyond this simple example and those simple arithmetic operators, some languages have dozens of similar arbitrary rules, resulting in tremendous complexity.
In contrast, Convex Lisp, the language used to write transactions for the Convex Virtual Machine, relies on one general rule. Understanding that single rule means understanding
the core of the Convex Lisp syntax.

Once again, simple maths dictates that one can be fully explicit by using parens. Indeed, parenthesis (parens) force the order of operations:

```
(4 + (2 * 3))
```

Now, without knowing anything about the order of operations but knowing about parens, we know for sure that `(2 * 3)` is computed first, and then `(4 + 6)`. No other rule
must be remembered but this one: inner parens are executed before outer parens.

Instead of talking about mathematical operators, the primary unit of computation in Convex Lisp is the [function](/cvm/building-blocks/functions). Values provided to functions are called
**parameters**. Later sections will describe how you can write your own functions and do anything you can imagine. Functions are always specified first in so-called
**prefix notation** and then parameters are provided. Hence, here is the above example written in Convex Lisp:

```clojure
(+ 4 (* 2 3))
```

One of the advantages of using prefix notation is that it provides a standard way of executing functions that takes less or more than 2 parameters. Indeed, the `+` function
used above has been designed to be **variadic**, meaning it accepts a variable number of parameters:

```clojure
(+ 2)         ;; 2
(+ 2 3)       ;; 5
(+ 2 3 10)    ;; 15
(+ 2 3 10 5)  ;; 20
```

Examples in all developer's guides heavily use functions for showcasing how to write code and handle values given their central role in describing computation.
