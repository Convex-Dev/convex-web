## Overview

This small example creates an NFT pointing to a picture and transfers it to user `#57`:

```clojure
(do
  (import asset.nft.tokens :as nft)
  (import convex.asset     :as asset)
  (def my-masterpiece
       (call nft
             (create-token {:name "My lifetime masterpiece"
                            :uri  "https://my-website.com/art/master-piece.png"}
                           nil)))
  (asset/transfer #57
                  [nft my-masterpiece]))
```

Let us break it down.


## Creating an NFT

Many different ways could be envisioned to implement NFTs. This section showcases how to create one in a
standardized manner by using `asset.nft.tokens`, an official actor provided by the Convex Foundation.

```clojure
(import asset.nft.tokens :as nft)
```

This actor manages a collection of NFTs where each NFT is an arbitrary [map](/cvm/data-types/map), meaning it can
represent anything. In this example, a name and a fictitious URI pointing to a picture are provided because this
is what we could like to include:

```clojure
(def my-masterpiece
     (call nft
           (create-token {:name "My lifetime masterpiece"
                          :uri  "https://my-website.com/art/master-piece.png"}
                         nil)))
```

In the context of this actor, `my-masterpiece` is an NFT identifier. However, this is an implementation detail.


## Asset operations

Just like [standard fungible tokens](/examples/fungible-token), NFTs created via `asset.nft.tokens` support the
`convex.asset` interface which provides a unified API for exchanging and managing all kind of assets.

```clojure
(import convex.asset :as asset)
```

Let us suppose account #57 is another user account. Feel free to substitute this value.

```clojure
(def my-custumer
     #57)
```

Transferring the NFT:

```clojure
(asset/transfer my-customer
                [nft my-masterpiece])
```

Checking ownership:

```clojure
(asset/owns? *address*
             [nft my-masterpiece])

;; -> false
;;
;; We transferred the NFT so it is not owned anymore.


(asset/owns? my-customer
             [nft my-masterpiece]

;; -> true
;;
;; Recipient is now the owner of that NFT.
```

Similarly to how a user can have a balance in [fungible tokens](/examples/fungible-token), a balance of NFTs
is a [set](/cvm/data-types/set) of all owned NFT identifiers.

```clojure
(asset/balance nft
               *address*)

;; -> #{}
;;
;; Empty set because we transferred the NFT.


(asset/balance nft
               my-customer)

;; -> #{0}
;;
;; A set with the NFT identifier of our masterpiece.
;; Identifier will be another value than 0 since many people run 
;; this example.
```


## NFT informations

These functions retrieve key information about NFTs:

Original creator:

```clojure
(nft/get-token-creator my-masterpiece)

;; -> your address
```

Current owner:

```clojure
(nft/get-token-owner my-masterpiece)

;; -> address of `my-customer` (since NFT got transferred to that
;;    account)
```

NFT data:

```clojure
(nft/get-token-data my-masterpiece)

;; -> {:name "My lifetime masterpiece"
;;    :uri  "https://my-website.com/art/master-piece.png"}
```


## Policies

Second argument when creating an NFT can be a policy map where keys designate actions and values designate addresses
of accounts that are authorized to perform these actions.

A key policy can be:

- `:destroy`, who can destroy the NFT
- `:transfer`, who can transfer the NFT
- `:update`, who can update the NFT data
- `[:update FIELD]`, who can update the given field in the NFT data

A value can be:

- `:creator`, account that created the NFT in the first place
- `:owner`, account that currently owns the NFT
- `:none`, nobody can perform this action
- Any address

By default, value for any action is `:owner`.

Here, a new NFT is created. Arbitrary information is provided: a name, the name of the creator, and a content which
is a [blob](/cvm/blob) that could represent a small file for instance. Its policy map describes that no one can destroy
it, only the owner can transfer it, and no one can update any of its data but the creator regarding the key `:creator-name`.

```clojure
(def my-masterpiece-2
     (call nft
           (create-token {:name         "Another masterpiece"
                          :creator-name "John Doe"
                          :content      0x4598F4CE}
                         {:destroy                :none
                          :transfer               :owner
                          :update                 :none
                          [:update :creator-name] :creator})))
```


## NFT operations

Following NFT operations must behave according to the policies described in previous example.

Attempt to destroy the NFT:

```clojure
(call nft
      (destroy-token my-masterpiece-2))

;; Error! No right to destroy the NFT.
```

Attempt to arbitrarily alter NFT data:

```clojure
(call nft
      (merge-token-data my-masterpiece-2
                        {:creation-date "2021-09-08"}))

;; Error! No right to update this NFT's data.
```

However, creator can update this one specific key as specified previously:

```clojure
(call nft
      (merge-token-data my-masterpiece-2
                        {:creator-name "Jane Doe"}))

;; -> {:name         "Another masterpiece"
;;    :creator-name "Jane Doe"
;;    :content      0x4598F4CE
;;
;; Okay, creator name got updated.
```

Transferring the NFT:

```clojure
(asset/transfer my-customer
                [nft my-masterpiece-2])

;; Works.
```

Attempt to transfer again:

```clojure
(asset/transfer #1000
                [nft my-masterpiece-2])

;; Error! No right to transfer again, only `:owner` can.
;; We lost ownership after the first transfer.
```


## Additional notes

The advantage of this standard approach is interoperability through the `convex.asset` interface, meaning your fungible
token can participate in a much broader asset ecosystem. Naturally, your are free to implement your NFT scheme. How would
you do it?
