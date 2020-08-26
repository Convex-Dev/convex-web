## Public HTTP API

### Faucet

POST https://convex.world/api/v1/faucet

#### Payload
- Address: ED25519 public key of your key pair.
- Amount: The requested amount.

```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "amount": 10000
}
```

#### Response

 ```json
{
  "id": 1,
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "amount": 10000,
  "value": ""
}
```

### Prepare Transaction 

POST https://convex.world/api/v1/transaction/prepare

#### Payload
- Address: ED25519 public key of your key pair.
- Source: Convex Lisp source that you want to execute.

```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "source": "(map inc [1 2 3])"
}
```


#### Response
 ```json
{
  "sequence-number": 1,
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "source": "(map inc [1 2 3])"
}
```

### Submit Transaction

POST https://convex.world/api/v1/transaction/submit

#### Payload
- Address: ED25519 public key of your key pair.
- Hash: Prepare Transaction response's hash.
- Sig: ED25519 signature of the hash using your key pair.

```json
{
  "address": "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sig": "ce31365976f0c5a5922f65f47999907f5fab475cce1fdad0ff53baaf800036a4ed1783b6dbb98b14a25e1bfffd140749223f6914b86533e6fa9811de0733cc0b"
}
```

#### Response

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