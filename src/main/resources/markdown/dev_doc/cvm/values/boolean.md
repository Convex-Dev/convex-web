There are only 2 possible values for a boolean: `true` or `false`. While very basic, their role is crucial and users will often encounter booleans throughout the follow sections. 


## Predicates

By convention, although this is not mandatory, names for booleans or functions that return a boolean end with `?`. Such functions are also called **predicates**.
Here are a few examples of standard predicates:

```clojure
(number? 42)      ;; True, 42 is a number
(number? "text")  ;; False, as seen later, "text" is a string, no a number
(zero? 42)        ;; False, 42 is not equal to 0
```

Only a limit set of standard functions do not end with `?` for the sake of brevety:

```clojure
(< 4 42)          ;; True, 4 is lesser than 42
(= 42 "text")     ;; False, value "text" is not equal to value 42
(= 2 2)           ;; True, 2 is equal to 2
```


## Casts

Any value can be cast to a boolean.

`false` and `nil` are consided to be "falsey":

```clojure
(boolean false)  ;; False
(boolean nil)    ;; False
```

While any other value is considered to be "truthy":

```clojure
(boolean 42)      ;; True
(boolean 0)       ;; True
(boolean "text")  ;; True
```

The distinction between "falsey" and "truthy" becomes important in the [section about conditionals](/cvm/conditionals.md).
