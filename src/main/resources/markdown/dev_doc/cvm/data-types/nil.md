Sometimes, data is missing or might not be available yet. Sometimes, a result cannot be produced. Overall, not every computation ought to necessarily return a concrete result.

For such case, `nil` is used and represents the absence of value. While abstract, it is heavily used in practice and going through
the developper guides will make a clear case for it. Comparable ideas exist in most programming languages, often subject of debate. In Convex Lisp, `nil` is tightly integrated with
the language and using it explicitly is commonly expected.

```clojure
nil

(nil? nil)  ;; -> true, only nil is nil
(nil? 42)   ;; -> false, any other value cannot be nil
```
