An address is a positive whole-valued number prefixed with `#` referring to an account.

```clojure
#42
#100
#123456789

(address? #42)  ;; -> true
```

Address of the account executing the current transaction is retrieved via:

```clojure
*address*
```

The section about [accounts](/cvm/accounts) showcases how they can be retrieved from addresses, as well as typical account
operations. Addresses also play a critical role in access control as described in the [section about actors](/cvm/actors).

Internally, addresses are a specific type of [blob](/cvm/data-types/blob) data-type. Hence, they can be used as keys in [blob-maps](/cvm/data-types/blob-map).
