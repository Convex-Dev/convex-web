A blob is a **binary large object** , large meaning size is arbitrary, anywhere in between small ang big.
It is a sequence of bytes written in hexadecimal notation and prefixed with `0x`. Each byte requires
2 digits:

```clojure
0x01
0x42
0xff23789875

0x  ;; Legal, means an empty blob

(blob? 0x42)  ;; -> true
```

Such a sequence of bytes can represent many things and is opaque without any further context.

Alternatively, it might be sometimes useful creating blobs from **hexstrings** (a [string](/cvm/data-types/text) where bytes are
also encoded in hexademical notation):

```clojure
(blob "01")
(blob "42")
(blob "ff23789875")
```

Besides representing arbiratry binary data, such as a file, blobs are typically used to represent cryptographic hashes and keys.


## Not quite a collection

Although blobs are collections of bytes in theory, they are not collections in the sense envisioned in further sections and data types such as [vectors](/cvm/data-types/vector) or
[maps](/cvm/data-types/map).

However, they are countable and it is possible to extract single bytes as longs:

```clojure
(count 0x112233)  ;; -> 3

(nth 0x112233 0)  ;; -> 17 (0x11 in hexadecimal)
(nth 0x112233 1)  ;; -> 34 (0x22 in hexadecimal)
(nth 0x112233 2)  ;; -> 51 (0x33 in hexadecimal)
```
