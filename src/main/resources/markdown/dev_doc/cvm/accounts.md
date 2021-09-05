At this point, accounts have already been mentioned several times. This section provides a more formal definition.

An account:

- Is bound to an [address](/cvm/data-types/address)
- Is attached to a public key (which can be replaced)
- Can execute transactions, any arbitrary code (signed by the private key), provided it has enough funds to cover executing fees
- Can persist values in the [decentralized database via its environment](/cvm/definitions)
- Can use [callable functions](/cvm/callable-functions) (special functions defined in other accounts)

Information about an account can be obtained by using the `account` function:

```clojure
(account *address*)  ;; Or any other address
```

The result is a [map](/cvm/data-types/map). Key-values are described in the following paragraphs.


## `:allowance`

Storing values in the environment of an account as [definitions](/cvm/definitions) requires memory. Memory allowance is a [long](/cvm/data-types/numbers)
indicating memory left (in bytes) to be used by that account. The current memory allowance of the account executing a transaction can also be queried
more readily by using a special symbol:

```clojure
*memory*
```

If a transaction implies storing more bytes than remaining, additional memory is automatically bought for Convex Coins (see `:balance`). If the balance is insufficient,
the transaction is aborted. When a definition is removed using `undef`, memory is released and put back into the memory allowance of the account. Alternatively,
memory can be bought or sold for Convex Coins explicitly:

```clojure
(set-memory (+ *memory* 1000))

;; Buys 1000 bytes.


(set-memory (- *memory* 1000))

;; Sells a 1000 bytes (or error if the current balance is insufficient).
```

Memory allowance is also transferable to other accounts:

```clojure
(transfer #42
          1000)

;; Transferred 1000 bytes to account under address #42.
```

Thus, unlike in other decentralized technologies, the memory becomes a tradable commodity. It is a scarce resource with an incentive to use only what is needed.
For more details about the memory allowance model, see [Convex Architecture Document 006](https://github.com/Convex-Dev/design/tree/main/cad/006_memory).


## `:balance`

Transactions ultimately incur fees that prevent flooding while rewarding peers (machines that carry out transactions). The native token used
on the Convex network is the Convex Coin. A balance is a positive [long](/cvm/data-types/numbers) indicating how much Convex Coins an account possess.

The current balance of the account executing the transaction can be queried using a special symbol:

```clojure
*balance*
```

A dedicated function can be used to query more readily the balance of any account:

```clojure
(balance *address*)  ;; Or any other address.
```

Convex Coins are easily transferable:

```clojure
(transfer #42
          1000)

;; Transferred 1000 Convex Coins to account under address #42.
```

Fees are actually not paid directly in Convex Coins. While the following scheme is currently almost transparent on the test network, before a transaction
is executed, an amount of **juice** must be bought using Convex Coins. Each operation in a transaction costs a certain amount of juice and the transaction is
aborted if there is not enough juice to carry it out to completion. This places a cap on how much can be executed on the transaction, avoiding bad surprises.
The remaining juice, if any, is refunded in Convex Coins after completion. More information on juice can be found in
[Convex Architecture Document 007](https://github.com/Convex-Dev/design/tree/main/cad/007_juice).

The remaining juice at a particular point in a transaction can be queried using a special symbol:

```clojure
*juice*
```


## `:controller`

The controller of an account is the address of another account. Effectively, a controller can execute any code on behalf of the account it controls.

**Attention.** This is a dangerous feature. It is most useful with [actors](/cvm/actors) and only in some particular cases. The following example is both
a demonstration and a warning:

```clojure
;; Suppose account #42 gives control to account #100
;;
(get (account *address*)
     :controller)

;; -> nil


(set-controller #100)

(get (account *address*)
     :controller)

;; -> #100


;;
;; Now let us switch to account #100
;;

(eval-as #42
         '(transfer #100
                    *balance*))
```

As simple as that, account `#100` stole all coins from account `#42`.

`eval-as` executes quoted code in the context of the given account if and only if it is controlled by the currently executing account.
It is as if the given code was a transaction submitted by account `#42`, momentarily.


## `:environment`

Internally, the environment of an account is a [map](/cvm/data-types/map) where keys are [symbols](/cvm/data-types/symbol) and values can be of any type, as
described in the section about [definitions](/cvm/definitions). Only an account can directly modify its environment. The section about
[callable functions](/cvm/callable_functions) exposes how an account can allow some of its functions to be executed on its behalf under certain
conditions, leading the way to implementing smart contracts.


## `:holdings`

Any account can attach a holding on any other account, even on itself. A holding can be a value of any type often representing some sort of asset. Holdings
attached to an account form a [blob map](/cvm/data-types/blob-map) where keys are [addresses](/cvm/data-types/address) and values are arbitrary.

Holdings of the account executing a transaction are readily accessible via a special symbol:

```clojure
*holdings*

;; -> {}, empty since no one put a holding on the current account.
```

Setting and getting a holding on any account:

```clojure
(set-holding #42
             ["test" :some-value])

(get-holding #42)

;; -> ["test" :some-value]
;;
;; Returns the holding put on the request account by the
;; currently executing account.
```


## `:key`

When a public key (32-byte [blob](/cvm/data-types/blob)) is attached to an account, it is a **user** account. Anyone owning the matching private key can sign
transactions and execute them in the context of that account. This is why creating a user account requires a public key:

```clojure
(create-account 0x453F12D3BF453F12D3BF453F12D3BF453F12D3BF453F12D3BF453F12D3BF45DA)
```

When a private key may have been compromised or a user wishes to look more anonymous, a new keypair can be generated and the public key of an existing account
replaced:

```clojure
(set-key 0x3F12D3BF453F12D3BF453F12D3BF453F12D3BF453F12D3BF453F12D3BF45DA9E)
```

**Attention.** Changing a public key means that any new transaction must be signed with the matching new private key. There is no other alternative. Changing
the public key without owning the matching private key means you will get locked out of that account.

An account without a public key is an **actor**. Actors play an important role on the Convex network, especially when it comes to writing smart contracts.
A [whole section is dedicated to creating and managing them](/cvm/actors).

Removing a key by running `(set-key nil)` turns a user account into an actor. Conversely, setting a public key on an actor converts it to a user account since
anyone with the matching private key can sign transactions on their behalf.


## `:metadata`

As described earlier, [definitions](/cvm/definitions) can host optional metadata. Internally, such metadata values are stored in this [map](/cvm/data-types/map).
