**POST** *https://convex.world/api/v1/transaction/submit*

Submits a transaction to the current test network after [preparing an Ed25519 signature of its hash](/rest-api/prepare-transaction).

Signature must be performed with the private key matching the public key associated with the executing account.


### Payload

- `accountKey`: Ed25519 public key hew string encoded as a 64 character associated with Account.
- `address`:  address of the executing account encoded as a JSON number
- `hash`: hash of the transaction encoded (string of 64 hex characters)
- `sig`: Ed25519 signature of the hash using your key pair encoded (string of 128 hex characters)

For example:

```json
{
  "address": 9,
  "accountKey": "1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sig": "ce31365976f0c5a5922f65f47999907f5fab475cce1fdad0ff53baaf800036a4ed1783b6dbb98b14a25e1bfffd140749223f6914b86533e6fa9811de0733cc0b"
}
```

### Response

- `value`: CVM return value printed as a JSON string

For example:

 ```json
{
  "value": "[1, 2, 3]"
}
```

In case of error, `value` will contain an error message and payload will look similarly to:

 ```json
{
  "value": "Can't convert 1 to class class convex.core.data.ASequence",
  "errorCode": "CAST"
}
```

### Errors

Fails with an HTTP status **403** if the digital signature is invalid.
