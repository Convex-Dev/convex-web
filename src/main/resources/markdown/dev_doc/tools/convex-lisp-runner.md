The Convex Lisp Runner is a terminal application written in Clojure for executing Convex Lisp directly, without having to connect to any peer.
It also provides a [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop), a common experience in Lisp languages.

This tool supercharges any dev environment and becomes particularly valuable when writing and testing smart contracts. Besides capabilities
offered by Convex Lisp, the runner hosts a series of features typically needed for development. Indeed, in between transactions which are handled
deterministically by the [CVM](/cvm), users can requests special operations such as file IO, advancing time, or restoring previous state. It
offers an unmatched level of productivity and insight by turning Convex Lisp into more of a scripting language.

More information can be found on [convex-dev/convex.cljc](https://github.com/Convex-Dev/convex.cljc/tree/main/project/run).
