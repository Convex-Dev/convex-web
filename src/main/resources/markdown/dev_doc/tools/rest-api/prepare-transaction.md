**POST** *https://convex.world/api/v1/transaction/prepare*

A transaction requires its hash to be digitally signed by the executing account prior to submission.

Given Convex Lisp source representing a transaction, this endpoint returns the hash to sign. Afterwards, the transaction
can be [submitted](/tools/rest-api/submit-transaction).


### Payload

- `address`: address of the executing account as a JSON number (will be hardcoded in the transaction)
- `source`: Convex Lisp source representing the transaction
- `sequence`: sequence number used to create the Transaction (optional, server will attempt to determine it if not provided)

For example:

```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response

- `address`: same as in payload
- `hash`: unique hash of the transaction (string of 64 hex characters)
- `sequence`: same as is payload (orginally provided by the user or retrieved by the server)
- `source`: same as in payload

For example:
 ```json
{
  "address": 9,
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sequence": 1,
  "source": "(map inc [1 2 3])"
}
```
