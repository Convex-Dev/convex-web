**GET** *https://convex.world/api/v1/accounts/**<address>***

Where **<address>** is the numeric value of the address.

Returns general information about an account on the test network according to the latest consensus state.


### Response

- `accountKey`: public key used for this account
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
  "accountKey":"50757f7994b1A9024EA1E35af5845aA6E15d6eE731D437aeBEdF58E69DA182aA",
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


### Errors

Fails with an HTTP status **404** if the given account does not exist, carrying the following payload:

```json
{
  "errorCode": "NOBODY",
  "source": "Server",
  "value": "The Account requested does not exist."
}
```
