The Convex Lisp Runner is a more advanced way of running Convex Lisp code. It is meant to be a developer tool for seasoned programmers who have a sufficient
understanding of Convex Lisp. For learning about Convex Lisp and experimenting with simple transactions over the current test network, we recommend the [sandbox](./sandbox).
However, for writing smart contracts and building prototypes of any kind, we recommend using the Runner. Mainly because you can use your own coding editor with a quick feedback loop, which makes the coding and testing experience very interactive.

[More information can be found on this Github repo](https://github.com/Convex-Dev/convex.cljc/tree/main/project/run).

It is a terminal-based tool where transactions are executed locally, on your own machine. Furthermore, between transactions, users can request special operations not currently supported by the Convex
Virtual Machine. Those extra operations, such as reading and writing files, turn Convex Lisp into a more general scripting language, which provides developers with a great developing
experience. The Runner also provides utilities commonly used by developers such as a unit testing library as well as some advanced utilities such as the time-travel feature.
