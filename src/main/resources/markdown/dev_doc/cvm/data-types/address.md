An address is a numerical value referring to an account. It is a whole-valued number prefixed with `#`:

```clojure
#42
#100
#123456789

(address? #42)  ;; True
```

Address of the account executing current transaction is retrivied via:

```clojure
*address*
```

The account behind an address can be retrieved via:

```clojure
(account *address*)  ;; Or any other address ; returns nil if account does not exist
```

Convex Coins can be transfered from the current account to any other account, provided funds are sufficient:

```clojure
(transfer #42
          10000)
```

Similarly, memory allowance can be transfered to another account:

```clojure
(transfer-memory #42
                 100)
```

Addresses also play a critical role in access control as described in the [section about actors](/cvm/actors-and-smart-contracts).

Internally, addresses are a specific type of [blob](/cvm/data-types/blob). Hence, they can be used as keys in [blob-maps](/cvm/data-types/blobmap).
