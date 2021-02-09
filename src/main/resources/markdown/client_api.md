## Create Account

Create a new Account on the network.

*POST* https://convex.world/api/v1/create-account

### Payload
- `public_key`: Ed25519 public key HEX string.

### Response

- `address`: Address number.

Examples:
 ```json
{
  "address": 10
}
```

## Account Details

*GET* https://convex.world/api/v1/accounts/<address>

### Response

- `address`: Address number.
- `is_library`: `true` if Account is a library.
- `is_actor`: `true` if Account is an Actor.
- `memory_size`: Ed25519 public key HEX string.
- `allowance`: Ed25519 public key HEX string.
- `type`: One of `"user"`, `"library"`, `"actor"`.
- `balance`: 
- `sequence`:

Examples:
 ```json
{
  "address": 9,
  "is_library": false,
  "is_actor": false,
  "memory_size": 75,
  "allowance": 10000000,
  "type": "user",
  "balance": 10000000000,
  "sequence": 0,
  "environment": {}
}
```

## Faucet

*POST* https://convex.world/api/v1/faucet

### Payload
- `address`: Address number.
- `amount`: The requested amount.

Examples:
```json
{
  "address": 9,
  "amount": 10000
}
```

### Response

- `id`: ID of the Transaction.
- `address`: Address number.
- `amount`: Requested amount.
- `value`: Actual value returned by the Transaction - it can be less than `amount`.

Examples:
 ```json
{
  "id": 1,
  "address": 9,
  "amount": 10000,
  "value": 10000
}
```

## Query 

*POST* https://convex.world/api/v1/query

### Payload
- `address`: Address number.
- `source`: Convex Lisp source that you want to execute.

Examples:
```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response

- `value`: A Convex Lisp value - the result of evaluating `source`. 

Examples:
 ```json
{
  "value": [2, 3, 4]
}
```

The `error-code` key is present if the response is an error:

 ```json
{
  "id": 18,
  "value": "Can't convert 1 to class class convex.core.data.ASequence",
  "error-code": "CAST"
}
```

## Prepare Transaction 

*POST* https://convex.world/api/v1/transaction/prepare

### Payload
- `address`: Address number.
- `source`: Convex Lisp source that you want to execute.
- (Optional) `sequence_number`: The sequence number used to create the transaction. 

Examples:
```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response

- `sequence_number`: Last sequence number of the Account.
- `address`: Address number of the Account that will execute the Transaction.
- `source`: Convex Lisp source of the Transaction.
- `hash`: Unique hash of the Transaction.

Examples:
 ```json
{
  "sequence_number": 1,
  "address": 9,
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "source": "(map inc [1 2 3])"
}
```

## Submit Transaction

*POST* https://convex.world/api/v1/transaction/submit

### Payload
- `address`: Address number of the Account that will execute the Transaction.
- `account_key`: Ed25519 public key HEX string associated with Address.
- `hash`: Prepare Transaction response's hash string.
- `sig`: Ed25519 signature of the hash using your key pair.

Examples:
```json
{
  "address": 9,
  "account_key": "1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sig": "ce31365976f0c5a5922f65f47999907f5fab475cce1fdad0ff53baaf800036a4ed1783b6dbb98b14a25e1bfffd140749223f6914b86533e6fa9811de0733cc0b"
}
```

### Response

Examples:

 ```json
{
  "id": 1,
  "value": [1, 2, 3]
}
```

The `error-code` key is present if the response is an error:

 ```json
{
  "id": 18,
  "value": "Can't convert 1 to class class convex.core.data.ASequence",
  "error-code": "CAST"
}
```