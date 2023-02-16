## Convergent Proof of Stake

Convex utilises the revolutionary Convergent Proof of Stake consensus (CPoS) algorithm. By treating consensus as a the problem of merging "Belief" that are communicated and validated by Peers on the network, Convex creates a decentralised Conflict-free Replicated Data Type that provably converges to a stable ordering of transactions, effectively solving the double-spend problem. Some major advantages this approach include:
- Zero block delay - Peers can publish blocks instantaneously and concurrently, without waiting for a previous block to be confirmed
- Simple implementation - The only key operation is the Belief Merge Function. Minimising the number of "moving parts" in the consensus algorithm maximises performance and minimises the potential for defects.
- Byzantine Fault Tolerance - We are able to offer the strongest possible security guarantees for any decentralised system

For more information, read our [White Paper](https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf)

## Convex Virtual Machine (CVM)

Convex executes transactions using a deterministic virtual machine (the CVM) which is based on the Lambda Calculus. Some notable features of the CVM include:
- First class functions, enabling excellent support for Functional Programming
- A fully on-chain compiler supporting code generation. Smart contracts can write other smart contracts with no need for external tooling.
- Memory accounting, creating economic incentives to utilise on-chain memory effectively
- Account based security and trust model

## Immutable Storage

Convex operates a system of convergent, content-addressable storage. All data values are referenced via a cryptographic hash, forming a Merkle DAG. We persist this data in a highly optimised memory-mapped database called Etch, which enables us to achieve millions of operations per second.

## CADs

Convex is developed as an open standard. We define core Convex specifications using a system of Convex Architecture Documents (CADs). Contributions and improvements are welcome, and may be eligible for Convex Foundation rewards. We recommend discussing proposed enhancements on the [Convex Discord](https://discord.com/invite/fsnCxEM)
