`convex.world` hosts a JSON REST API for interacting with the test network. It is useful for dApps and any client application
that cannot leverage the binary protocol that peers speak natively.

A selection of tailored libraries can be found within [Convex libraries](https://github.com/Convex-Dev). However,
any language/platform capable of producing an Ed25519 signature will be able to use this REST API directly.

Payloads and responses are JSON maps as described in the following subsections.

Generally, besides errors in each method, the following HTTP statuses are returned in case of failure:

- **400**: request is incorrectly formatted
- **500**: server encountered an error


## Libraries

Here is a selection of libraries providing an interface for the REST API as well as Ed25519 signing. This list is
growing, contributions welcome.

**C:** [billbsing/convex-api-c](https://github.com/billbsing/convex-api-c.git) (in development)

**Java:** [convex-dev/convex-java](https://github.com/Convex-Dev/convex-java)

**NodeJS:** [convex-dev/convex-api-js](https://github.com/Convex-Dev/convex-api-js)

**Python:** [convex-dev/convex-api-py](https://github.com/Convex-Dev/convex-api-py)

**Ruby:** [billbsing/convex-api-ruby](https://github.com/billbsing/convex-api-ruby.git) (in development)
