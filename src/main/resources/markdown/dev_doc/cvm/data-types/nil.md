Sometimes, data is missing or might not be available yet. Sometimes, a result cannot be produced. Overall, not every computation should necessarily return a concrete result.

For such a case, `nil` is used and represents the absence of value. While it appears abstract, it is heavily used in practice and going through
the developer guides will make a clear case for it. Comparable ideas exist in most programming languages, often subject of debate. In Convex Lisp, `nil` is tightly integrated with
the language and using it explicitly is commonly expected.

```clojure
nil

(nil? nil)  ;; -> true, only nil is nil
(nil? 42)   ;; -> false, any other value cannot be nil
```
