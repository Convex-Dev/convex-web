The Convex Lisp Runner is a more advanced way of running Convex Lisp. It is meant to be a developer tool for seasoned programmers having a sufficient
understanding of Convex Lisp. For learning about Convex Lisp and experimenting simple transactions over the current test network, we recommend the [sandbox](./sandbox).
However, for writing smart contracts, prototypes of any kind using your own editor with a quick feedback loop, the Convex Lisp Runner becomes very interesting.

[More information can be found on Github](https://github.com/Convex-Dev/convex.cljc/tree/main/project/run).

It is a terminal-based tool where transactions are executed locally, on your own machine. Furthermore, between transactions, users can request special operations not supported by the Convex
Virtual Machine. Those extra operations, such as reading and writing files, turn Convex Lisp into more of a scripting language, providing a unique developing
experience. It also provides utilities commonly used by developers such as a unit testing library.
