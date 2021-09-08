`convex.world` hosts a JSON REST API for interacting with the test network. It is useful for dApps and any client application
that cannot leverage the binary protocol that peers speak natively.

A selection of tailored libraries can be found within [Convex libraries](/cvm/running-convex-lisp/clients). However,
any language/platform capable of producing an Ed25519 signature will be able to use this REST API directly.

Payloads and responses are JSON maps as described in the following subsections.

Generally, besides errors in each method, the following HTTP statuses are returned in case of failure:

- **400**: request is incorrectly formatted
- **500**: server encountered an error
