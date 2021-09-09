## Overview

This small example creates a custom fungible token and transfers `500` units it to user `#57`.

```clojure
(do
  (import convex.asset    :as asset)
  (import convex.fungible :as fun)
  (def my-token
       (deploy (fun/build-token {:initial-holder *address*
                                 :supply         1000000})))
  (asset/transfer #57
                  [my-token 500]))
  
```


## Creating a custom fungible token

Many different ways could be envisioned to implement a fungible token. This section showcases how to create one in a
standardized manner by using `convex.fungible`, an official library provided by the Convex Foundation.

```clojure
(import convex.fungible :as fun)
```

A fungible token is created as an [actor](/cvm/accounts/actors) that manages a total supply of tokens initially owned by
a specified account.

```clojure
(def my-token
     (deploy (fun/build-token {:initial-holder *address*
                               :supply         1000000})))
```


## Asset operations

This token actor implements a standard interface. Inspecting its environment reveals a series of
[callable functions](/cvm/accounts/callable-functions). Those functions are implemented as described in `convex.asset`,
another official library deployed by the Convex Foundation that provides a common interface for exchanging all kind of assets
as long as they implement this interface.

```clojure
(import convex.asset :as asset)
```

Let us suppose account `#57` is another user account. Feel free to substitute this value.

```clojure
(def my-friend
     #57)
```

Transferring `500` tokens:

```clojure
(asset/transfer my-friend
                [my-token 500])

;; -> 500
```

Checking balance after transfer:

```clojure
(asset/balance my-token
               *address*)

;; -> 999500
;;
;; Initially hold total supply of 1000000.
;; Minus 500 after the transfer.
```

Indeed, our friend received some of our tokens:

```clojure
(asset/balance my-token
               my-friend)

;; -> 500
```
