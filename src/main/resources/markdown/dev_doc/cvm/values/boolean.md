There are only 2 possible values for a boolean: `true` or `false`. While very basic, their role is crucial and users will often encounter booleans throughout the developer guides.


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


## Predicates

By convention, although this is not mandatory, names for booleans or functions that return a boolean end with `?`. Such functions are also called **predicates**.

The following lists are an overview, actual examples are scattered throughout the developer guides.

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
