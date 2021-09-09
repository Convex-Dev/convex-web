[https://github.com/Convex-Dev/convex](https://github.com/Convex-Dev/convex)

Any aspect of the Convex stack is available to JVM languages through the core Java libraries.

Besides Java, those utilities provide a decisive advantage to programming languages such as Clojure, Kotling, or Scala.

Common use cases are:

**dApps and various clients**. Peers natively speak a fast binary TCP protocol. The binary client is more efficient and more versatile than using the
[REST API](/tools/rest-api). It allows connecting to any peer, isolated or synced to the test network. `convex.world` exposes its peer at
`convex.world:18888`.

**Off-chain computation**. Since all [CVM](/cvm) utilities such as [data types](/cvm/data-types) are available as Java classes with well-defined interfaces,
off-chain computation has never been easier. Anything that is achievable in a regular transaction via Convex Lisp can be emulated directly from a JVM language
with excellent performance, and much more. This opens to new possibilities and can bring drastic cost savings.

**Embedding a peer.** This very website, `convex.world`, is a Clojure application which runs the website, provides the [REST API](/tools/rest-api), and runs a
peer at the same time. It is a concrete example of a broader application embedding a peer in order to augment its capabilities.

**Miscellaneous utilities**. Such as key pair generation and management.
