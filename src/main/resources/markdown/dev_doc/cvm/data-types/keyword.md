Keywords are composed of 1 to 64 characters prefixed by `:`, such as:

```clojure
:hello
:hello-world
:this-is-a-keyword

(keyword? :my-keyword)  ;; True
```

Unlike [strings](/cvm/data-types/text), they cannot contain spaces and are used as:

- Efficient keys in [maps](/cvm/data-types/map)
- [Enums](https://en.wikipedia.org/wiki/Enumerated_type#:~:text=In%20computer%20programming%2C%20an%20enumerated,or%20enumerators%20of%20the%20type.), identifiers

Their usage is described in greater detail in the [section about maps](/cvm/data-types/map).
