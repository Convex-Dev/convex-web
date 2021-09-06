There are only 2 possible values for a boolean: `true` or `false`. While very basic, their role is crucial and users will often encounter booleans throughout the developer guides.

```clojure
(boolean? true)   ;; -> true
(boolean? false)  ;; -> true
```


## Casts

Any value can be cast to a boolean.

`false` and `nil` are considered to be "falsey":

```clojure
(boolean false)  ;; -> false
(boolean nil)    ;; -> false
```

While all other values are considered to be "truthy":

```clojure
(boolean 42)      ;; -> true
(boolean 0)       ;; -> true
(boolean "text")  ;; -> true
```

The distinction between "falsey" and "truthy" becomes important in the [section about logic](/cvm/logic).

All values can be negated to the opposite boolean value:

```clojure
(not false)   ;; -> true
(not nil)     ;; -> true

(not true)    ;; -> false
(not 42)      ;; -> false
(not "text")  ;; -> false
```


## Predicates

By convention, although this is not mandatory, names for booleans or functions that return a boolean end with `?`. Such functions are commonly called **predicates**.

The following list shows an overview. The actual examples are scattered throughout the developer guides.

Testing the type of a value:

```clojure
blob?
boolean?
coll?
fn?
keyword?
list?
long?
map?
number?
nil?
set?
str?
symbol?
syntax?
vector?
```

Math:

```clojure
<
<=
==
>=
>
nan?
zero?
```

Accounts and environments:

```clojure
account?
actor?
defined?
```


Collections:

```clojure
contains-key?
empty?
subset?
```
