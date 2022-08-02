Symbols are composed of 1 to 64 characters without anything around:

```clojure
hello
hello-world
this-is-a-symbol
```

Unlike [strings](/cvm/data-types/text), they cannot contain spaces and are used mostly used as names for variables.

Their usage is described in greater detail in [the section about definitions](/cvm/building-blocks/definitions). By default,
symbols are evaluated and must be defined in the environment (pointing to a value). Otherwise, an [error](/cvm/building-blocks/errors) will occur. The section about
[code is data](/cvm/building-blocks/code-is-data) explains the concept of **quoting** which prevents evaluation. Consulting both sections, now or at a later point,
will provide a deeper explanation as to what symbols are and why they exist.

```clojure
my-symbol                    ;; Error! Undefined in the environment

(quote my-symbol)            ;; -> my-symbol (no evaluation)
(symbol? (quote my-symbol))  ;; -> true
```


## Special symbols

A series of special symbols can be accessed at any moment. They start with `*` and end with `*`.

The following list is only provided as an overview, actual examples are scattered in the relevant developer guides:

```clojure
*address*
*balance*
*caller*
*depth*
*holdings*
*initial-expander*
*juice*
*key*
*memory*
*offer*
*origin*
*registry*
*sequence*
*state*
*timestamp*
```
