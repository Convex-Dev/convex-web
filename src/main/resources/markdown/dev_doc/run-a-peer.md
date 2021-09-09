A peer must be declared and staked on the network prior to joining it, as described in [peer operations](/cvm/peer-operations).

Then, the following solutions can be used to run it on your machine:

- [Command Line Interface](/tools/command-line-interface)
- [Clojure toolchain](/tools/clojure-toolchain)
- [Core JVM libraries](/tools/core)

The most straightforward way is to use the command line interface as it is a standalone application and does not require knowledge
regarding a particular programming languages.

The core JVM libraries are most useful when embedding a peer in a broader JVM application. The very same comment could be applied to the Clojure
toolchain which wraps those JVM libraries. However, working with a Clojure REPL is a very good exercise for understanding the Convex stack
and its interactive experience is both productive and enjoyable. As such, it is the recommended way for any Clojurist.
