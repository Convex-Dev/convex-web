Blob maps are a specialized type of [maps](/cvm/data-types/map). Everything described for regular maps applies 
to blob maps. The only difference is that blob maps only accept [blobs](/cvm/data-types/blob) as keys.

They are provided only for performance reasons. Although regular maps accept any type of keys, blobs as well, using a
blob map is more efficient if it is known that only blobs will be used as keys. One common use case is the need to map
addresses to some values, keeping in mind that an [address](/cvm/data-types/address) is a specialized type of blob.

Overall, this is a more advanced feature that beginners will probably not need.

Blob maps are created through a dedicated function:

```clojure
(blob-map 0xff1234 "some value"
          #42      :another-value)
```

All functions showcased for [regular maps](/cvm/data-types/map) can be applied:

```clojure
(get (blob-map #42 :some-value)
     #42)

;; :some-vlaue


(get (blob-map #42 :some-value)
     0xff1234)

;; nil


(assoc (blob-map)
       42
       :error!)

;; Error, key 42 is a long, not a blob.

;; Etc...
```
