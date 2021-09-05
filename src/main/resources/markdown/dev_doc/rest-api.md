`convex.world` hosts a JSON REST API for clients who would like to interact with the test network but cannot
use the Convex binary protocol that peers speak.

A selection of tailored libraries can be found within [Convex libraries](/cvm/running-convex-lisp/clients). However,
any language/platform capable of producing an Ed25519 signature will be able to use this REST API directly.

Payloads and responses are JSON maps as described below.


## Create an account

Creates a new account associated with the given Ed25519 public key. The endpoint will pay for the fees incurred by
creating an new account since fees are currently fictitious on the test network.

**POST** *https://convex.world/api/v1/createAccount*

Payload:
- `accountKey`: Ed25519 public key (string of 64 hex characters)

Reponse:

- `address`: address of the new account encoded as a JSON number

For example:
 ```json
{
  "address": 10
}
```

## Account details

Returns general information about an account on the test network according to the latest consensus state.

**GET** *https://convex.world/api/v1/accounts/**<address>***

Where **<address>** is the numeric value of the Address.

Response:

- `address`: account address encoded as a JSON number
- `allowance`: unused memory allowance owned by this ccount
- `balance`: account's balance in Convex Coins.
- `isActor`: `true` if account is an actor, or `false` otherwise
- `isLibrary`: `true` if account is a library, `false` otherwise.
- `memorySize`: storage memory size of the account in bytes
- `sequence`: sequence number of the last Transaction executed on this account, or zero for new accounts
- `type`: one of `"user"`, `"library"`, or `"actor"`

For example:
 ```json
{
  "address": 9,
  "allowance": 10000000,
  "balance": 10000000000,
  "isActor": false,
  "isLibrary": false,
  "memorySize": 75,
  "sequence": 0,
  "type": "user"
}
```

Fails with an HTTP status **404** if the given account does not exist, carrying the following payload:

```json
{
  "errorCode": "NOBODY",
  "source": "Server",
  "value": "The Account requested does not exist."
}
```


## Request coins

Since Convex Coins does not have any real value on the test network, it is possible to request some
for an account.

**POST** *https://convex.world/api/v1/faucet*

Payload:

- `address`: account address encoded as a JSON number
- `amount`: requested amount in Convex Coins as a JSON number (maximum allowed is `100,000,000`)

```json
{
  "address": 9,
  "amount": 10000
}
```

Reponse:

- `address`: account address encoded as a JSON number
- `amount`: requested amount in Convex Coins
- `value`: actual value returned by the transaction (can be less than `amount`).

 ```json
{
  "address": 9,
  "amount": 10000,
  "value": 10000
}
```

Fails with an HTTP status **400** if requested amount is negative, carrying the following payload
(supposing an original amount of `-1`):

```json
{
  "errorCode": "INCORRECT",
  "source": "Server",
  "value": "Invalid amount: -1"
}
```

Fails with an HTTP status **404** if requested amount is abobe `100,000,000`, carrying the following
payload:

```json
{
  "errorCode": "INCORRECT",
  "source": "Server",
  "value": "You can't request more than 100,000,000."
}
```


## Query

Executes a query against the current state of the test network.

Unlike a transaction, a query does not require a digital signature and does not incur fees. It is run without consensus
to produce a result which is not replicated among other peers. Hence, the state of the network is left intact.

**POST** *https://convex.world/api/v1/query*

Payload:
- `address`: account address encoded as a JSON number (ownership of that account is not needed)
- `source`: Convex Lisp source executed as a query (string)

For example:

```json
{
  "address": 9,
  "source": "(map inc [1 2 3])"
}
```

Response:

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


## Prepare a transaction

A transaction requires its hash to be digitally signed by the executing account prior to submission.

Given Convex Lisp source representing a transaction, this endpoint returns the hash to sign. Afterwards, the transaction
can be submitted.

**POST** *https://convex.world/api/v1/transaction/prepare*

Payload:

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

Response:

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


## Submit Transaction

Submits a transaction to the current test network by providing an Ed25519 signature of its hash (see previous section).

Signature must be performed with the private key matching the public key associated with the executing account.

**POST** *https://convex.world/api/v1/transaction/submit*

Payload:

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

Response:

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

Fails with an HTTP status **403** if the digital signature is invalid.


## Errors

Besides aforementioned errors, the following HTTP statuses are returned in case of error:

- **400**: request is incorrectly formatted
- **500**: server encounters an error
