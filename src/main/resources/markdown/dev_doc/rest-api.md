`convex.world` hosts a JSON REST API for clients who would like to interact with the test network but cannot
use the Convex binary protocol that peers speak.

A selection of tailored libraries can be found within [Convex libraries](/cvm/running-convex-lisp/clients). However,
any language/platform capable of producing an Ed25519 signature will be able to use this REST API directly.

Payloads and responses are JSON maps as described in the following subsections.

Generally, besides errors in each method, the following HTTP statuses are returned in case of failure:

- **400**: request is incorrectly formatted
- **500**: server encountered an error
