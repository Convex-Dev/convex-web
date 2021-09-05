**POST** *https://convex.world/api/v1/faucet*

Since Convex Coins does not have any real value on the test network, it is possible to request some
for an account.


### Payload

- `address`: account address encoded as a JSON number
- `amount`: requested amount in Convex Coins as a JSON number (maximum allowed is `100,000,000`)

```json
{
  "address": 9,
  "amount": 10000
}
```


### Reponse

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


### Errors

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
