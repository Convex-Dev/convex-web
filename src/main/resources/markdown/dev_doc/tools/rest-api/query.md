**POST** *https://convex.world/api/v1/query*

Executes a query against the current state of the test network.

Unlike a transaction, a query does not require a digital signature and does not incur fees. It is run without consensus
to produce a result which is not replicated among other peers. Hence, the state of the network is left intact.


### Payload

- `address`: account address encoded as a JSON number (ownership of that account is not needed)
- `source`: Convex Lisp source executed as a query (string)

For example:

```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response:

- `value`: CVM return value encoded as JSON - the result of evaluating `source`. In the case of a CVM error, this will contain the message.

For example:

 ```json
{
  "value": [2, 3, 4]
}
```

The `errorCode` key is present if the response is an error:

 ```json
{
  "errorCode": "CAST",
  "value": "Can't convert 1 to class convex.core.data.ASequence"
}
```

Whether or not there is a CVM error, the HTTP status is always **200** indicating that the query was executed.
