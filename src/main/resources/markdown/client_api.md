## Account

*GET* https://convex.world/api/v1/accounts/<address>

### Response

Examples:
 ```json
{
  "address": "7E66429CA9c10e68eFae2dCBF1804f0F6B3369c7164a3187D6233683c258710f",
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
- `address`: ED25519 public key of your key pair.
- `amount`: The requested amount.

Examples:
```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "amount": 10000
}
```

### Response

Examples:
 ```json
{
  "id": 1,
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "amount": 10000,
  "value": ""
}
```

## Query 

*POST* https://convex.world/api/v1/query

### Payload
- `address`: ED25519 public key of your key pair.
- `source`: Convex Lisp source that you want to execute.

Examples:
```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "source": "(map inc [1 2 3])"
}
```

### Response

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
- `address`: ED25519 public key of your key pair.
- `source`: Convex Lisp source that you want to execute.
- (Optional) `sequence_number`: The sequence number used to create the transaction. 

Examples:
```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "source": "(map inc [1 2 3])"
}
```

### Response

Examples:
 ```json
{
  "sequence_number": 1,
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "source": "(map inc [1 2 3])"
}
```

## Submit Transaction

*POST* https://convex.world/api/v1/transaction/submit

### Payload
- `address`: ED25519 public key of your key pair.
- `hash`: Prepare Transaction response's hash.
- `sig`: ED25519 signature of the hash using your key pair.

Examples:
```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
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