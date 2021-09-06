Convex provides a uniquely flexible **data model**. By understanding the following sections revolving around the different types of **values** supported by the CVM, users will
be able to combine those types to represent pretty much anything they could imagine. Once again, we strive for a minimalistic set of principles that can be used and reused at will. In turn, this provides a fun, pragmatic, and robust experience.

A particular data type is used for its intrinsic properties. For instance, numbers are used for mathematical computations while strings are used to represent text. Vectors are used to group together several values. Each section describes the properties of a particular data type with concrete examples, going from simpler ones to more complex ones.
Examples demonstrate how values can be manipulated using functions. As such, it is expected that users have a minimum understanding of what a function is as described in the
[basic syntax section](/cvm/basic-syntax).

All values are **immutable**. In other words, unlike in many programming languages, a value can never be directly altered. Only a new one can be created. Convex Lisp and its associated
decentralized database have been designed for great performance under such conditions. This principle provides a powerful data model which eliminates whole classes of bugs and limitations. For example, removing
many barriers when writing smart contracts, allowing developers to focus more on the ideas they want to bring to life rather than on their implementations.
