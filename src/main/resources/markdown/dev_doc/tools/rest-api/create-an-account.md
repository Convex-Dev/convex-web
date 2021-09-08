**POST** *https://convex.world/api/v1/createAccount*

Creates a new account associated with the given Ed25519 public key. The endpoint will pay for the fees incurred by
creating an new account since fees are currently fictitious on the test network.


### Payload

- `accountKey`: Ed25519 public key (string of 64 hex characters)

For example:

```json
{
  "accountKey": "1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812"
}
```


### Reponse

- `address`: address of the new account encoded as a JSON number

For example:
 ```json
{
  "address": 10
}
```
