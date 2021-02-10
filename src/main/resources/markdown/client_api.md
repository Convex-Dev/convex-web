Payload and Response are both JSON Objects. Depending on the API endpoint, this will contain different fields as described below.

## Create Account

Create a new Account on the network. This does not require an existing Account on the Test Network because the server will pay for the cost of constructing the new Account.

**POST** https://convex.world/api/v1/createAccount

### Payload
- `accountKey`: Ed25519 public key HEX string - it should be 64 characters.

### Response

- `address`: New Account Address encoded as a JSON number.

Examples:
 ```json
{
  "address": 10
}
```

## Account Details

This endpoint returns general information about an Account on the Network according to the latest Consensus State.

**GET** https://convex.world/api/v1/accounts/<address>

Where `<address>` is the numeric value of the Address.

### Response

- `address`: Account Address encoded as a JSON number.
- `isLibrary`: `true` if Account is a library, or false otherwise.
- `isActor`: `true` if Account is an Actor, or false otherwise.
- `memorySize`: Storage Memory Size of the Account in bytes.
- `allowance`: Unused Memory Allowance owned by this Account.
- `type`: One of `"user"`, `"library"` or `"actor"`.
- `balance`: Account's balance in Convex Coins.
- `sequence`: Sequence number of the last Transaction executed on this Account, or zero for new Accounts.

Examples:
 ```json
{
  "address": 9,
  "isLibrary": false,
  "isActor": false,
  "memorySize": 75,
  "allowance": 10000000,
  "type": "user",
  "balance": 10000000000,
  "sequence": 0,
  "environment": {}
}
```

### Error cases

If the Account does not exist, an error status 404 with the folloing payload:

```json
{
  "errorCode": "NOBODY",
  "value": "The Account requested does not exist.",
  "source": "Server"
}
```

## Faucet

Request for an amount of free Convex coins on an existing Account.

**POST** https://convex.world/api/v1/faucet

### Payload
- `address`: Account Address encoded as a JSON number.
- `amount`: The requested amount in Convex Coins.

Examples:
```json
{
  "address": 9,
  "amount": 10000
}
```

### Response

- `address`: Account Address encoded as a JSON number.
- `amount`: The requested amount in Convex Coins.
- `value`: Actual value returned by the Transaction - it can be less than `amount`.

Examples:
 ```json
{
  "address": 9,
  "amount": 10000,
  "value": 10000
}
```

## Query

Execute a Query against the current state of the Convex Network. This requires no Transaction fees, or digital signature. 

**POST** https://convex.world/api/v1/query

### Payload
- `address`: Account Address encoded as a JSON number - the Query will be executed 'as-if' this Account ran the Query.
- `source`: Convex Lisp source that you want to execute as a Query.

Examples:
```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response

- `value`: A CVM return value encoded as JSON - the result of evaluating `source`. In the case of a CVM error, this will contain the message. 

Examples:
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

Wheter or not if there's a CVM error, the HTTP status will be 200 indicating that the Query was executed.

### Error cases

HTTP 500 if the server encounters an error.
HTTP 400 if the request is incorrectly formatted.

## Prepare Transaction

When executing a Transaction, the user must sign the hash of the Transaction in order to provide a valid digital signature.
This endpoint can be used to get the hash of Transaction from the server prior to signing.

The user is expected to follow up the Prepare with a Submit including the valid signature.

**POST** https://convex.world/api/v1/transaction/prepare

### Payload
- `address`: Account Address encoded as a JSON number - the Transaction will be constructed using this Address.
- `source`: Convex Lisp source that you want to execute.
- (Optional) `sequence`: The sequence number used to create the Transaction. If not provided, the server will attempet to determine the correct sequence number. 

Examples:
```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

### Response

- `sequence`: The sequence number assigned to the Transaction - this may be the one provided by the caller, or allocated by the server.
- `address`: Address number of the Account that will execute the Transaction.
- `source`: Convex Lisp source of the Transaction as originally provided.
- `hash`: Unique hash of the Transaction as a 64 character HEX string - this must be used for the following Submit request.

Examples:
 ```json
{
  "sequence": 1,
  "address": 9,
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "source": "(map inc [1 2 3])"
}
```

## Submit Transaction

Submit a Transaction to the Convex Network by providing a valid digital signature.

The signature must be the Ed25519 signature of the Transaction hash. This should be the hash returned from the server by a previous Prepare request.

The digital signature must be valid for the Ed25519 public key associated with the Account.

**POST** https://convex.world/api/v1/transaction/submit

### Payload
- `address`: Address number of the Account that will execute the Transaction.
- `accountKey`: Ed25519 public key HEX string encoded as a 64 character associated with Account.
- `hash`: Hash of the Transaction encoded as a 64 character HEX string.
- `sig`: Ed25519 signature of the hash using your key pair encoded as a 128 character HEX string.

Examples:
```json
{
  "address": 9,
  "accountKey": "1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sig": "ce31365976f0c5a5922f65f47999907f5fab475cce1fdad0ff53baaf800036a4ed1783b6dbb98b14a25e1bfffd140749223f6914b86533e6fa9811de0733cc0b"
}
```

### Response

- `value`: A CVM return value encoded as JSON. In the case of a CVM error, this will contain the message.

Examples:

 ```json
{
  "value": [1, 2, 3]
}
```

 ```json
{
  "value": "Can't convert 1 to class class convex.core.data.ASequence",
  "errorCode": "CAST"
}
```

The `errorCode` key is present if the response is an error.

### Error cases

HTTP 500 if the server encounters an error.
HTTP 400 if the request is incorrectly formatted.
HTTP 403 if the digital signature is invalid.
