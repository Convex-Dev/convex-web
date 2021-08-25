Symbols are composed of 1 to 64 characters without anything around:

```clojure
hello
hello-world
this-is-a-keyword
```

Unlike [strings](/cvm/data-types/text), they cannot contain spaces and are used mostly used as names for variables.

Their usage is described in greater detail in [the section about the environment and variables](/cvm/environment-and-variables). By default,
symbols are executed and must be defined in the environment, pointing to a value, otherwise an error will occur. The section about
[code as data](/cvm/code-as-data) explains the concept of **quoting** which prevents execution. Consulting both sections, now or at a later point,
will provide a deeper explaination as to what symbols are and why they exist.

```clojure
this-is-my-symbol            ;; Error, undefined in the environment
(quote this-is-my-symbol)    ;; Fine, does not try resolving to a value

(symbol? (quote my-symbol))  ;; True
```


## Special symbols

A series of special symbols can be accessed at any moment. They start with `*` and end with `*`.

The following list is only provided as an overview, actual examples are scattered in relevant developer guides:

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
