## Account

An Account is a record of identification and ownership within Convex. Accounts may be either:

* **User Accounts**: Accounts that are controlled by external users, where access is controlled by digital signatures on transactions.
* **Actor Accounts**: Accounts that are managed by an autonomous Actor, where behavior is 100% deterministic according the associated CVM code.

## Account Key

An Account Key is a 32-byte Value used as a public key to control access to an Account.

It is generally shown as a hexadecimal string, looking something like:

`0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C`

Technically, the Account Key of a User Account is an `Ed25519` Public Key. You must be in possession of the corresponding private key in order to digitally sign transactions for that Account. Actor Accounts have Addresses that are generated via SHA3-256 hash functions (and therefore do not have a corresponding private key, and no transactions can be submitted for them).

## Actor

An autonomous entity implemented in CVM code on the Convex Network.

An Actor is defined with exactly one Account, but may send messages to and control assets managed by other Actors / Accounts.

## Address

An Address is a numerical value used to refer to Accounts. An Address is valid if it refers to an existing Account (User or Actor) in the CVM State.

An Address is conventionally displayed as a number with with a `#` prefix e.g.:

`#1245`

Addresses are issued sequentially for new Accounts by the CVM.


## Belief

A Belief is a specialised Value containing a Peer's combined view of Consensus.

The Belief data structure most notably contains Orderings of Blocks either confirmed in Consensus or proposed for Consensus by different Peers.

## Belief Merge Function

A specialised function that can be used to merge Beliefs from different Peers.

Each Peer runs a Belief Merge function as part of the Consensus Algorithm.

## Blob

A Value representing an arbitrary sequence of bytes.

## Block

A Block in Convex is a collection of transactions submitted simultaneously by a Peer.

Unlike Blockchains, a Block in Convex does *not* contain a hash of the previous block.

A Block must be digitally signed by the proposing Peer to be valid for inclusion in consensus.

## Blockchain

A system that maintains an appendable sequence of Blocks where each block contains a cryptographic hash of the previous block (and hence its integrity can be validated recursively all the way back to the original block).

Technically, Convex is not a blockchain because Blocks are not required to contain a hash of any previous Block. This gives Convex a technical advantage because Blocks can therefore be handled in parallel and re-ordered by the higher level Consensus Algorithm after creation.

## Consensus Algorithm

The Convex Consensus Algorithm is obtains consensus through the use of a convergent Belief Merge Function. This algorithm is called Convergent Proof of Stake (CPoS), and is described in more detail in the [White Paper](convex-whitepaper.md).

## Consensus Point

The greatest position in the Ordering of Blocks produced by the Consensus Algorithm which has been confirmed as being in Consensus. Each Peer maintains it's own view of the Consensus Point based on observed consensus proposals from other Peers.

The Consensus Point cannot be rolled back according to the rules of the Protocol (any attempt to do so would therefore constitute a Fork). However, some Peers may advance their Consensus Point slightly before others.

Users transacting on the Convex network should use the Consensus Point of a trusted Peer to confirm that their transactions have been successfully executed on the Convex Network.

## Convex Network

A network of Peers, maintaining a consistent global state and executing state transitions according to the Consensus Algorithm and rules of the CVM.

## Convex Lisp

A programming language based on Lisp, that is available by default as part of the CVM.

Convex Lisp prioritises features that are well suited to the development of decentralised economic systems. This includes:

* Emphasis on functional programming to reduce error and improve logical clarity
* Use of immutable, persistent data structures
* Actor-based model enabling trusted autonomous execution of code for Smart Contracts

## CRDT

Acronym for Conflict-free Replicated Data Type, a data structure that can be replicated across many computers in a network and is guaranteed (mathematically) to reach eventual consistency.

The Consensus Algorithm makes use of what is effectively a CRDT (of Beliefs) to guarantee convergence on a single consensus.

## CVM

Acronym for Convex Virtual Machine. This is a general purpose computational environment that can be used to implement the State transitions triggered by Transactions in the Convex Network.

The CVM is Turing complete, and is capable of executing arbitrary logic. It enforces constraints upon computation costs and memory usage to ensure that Users are unable to abuse shared resources (making Denial of Service attacks prohibitively expensive, for example).

## CVM Code

A representation of computer code that can be executed natively on the CVM. CVM code is based on a small number of core primitives that map to the Lambda Calculus, which can be composed in a tree data structure to represent arbitrary Turing-complete code.

Different languages may be compiled to CVM code.

## DApp

A dApp is a decentralised application.

We can distinguish between two forms of Dapp:

- **Pure dApp** - the Dapp consists only of client code and on-chain implementation (i.e. the Dapp depends on the Convex network and nothing else). Such Dapps are simple to build and maintain, and minimise the risk of relying on centralised systems
- **Hybrid dApp** - the Dapp uses client code, on-chain-implementation and one or more off-chain servers. This is more complex to build and maintain, but is necessary if additional servers are required (e.g. to store private information, or to integrate with external systems)

## Digital Signature

A cryptographic technique where a piece of data

Digital signatures in Convex use the Ed25519 algorithm. The data that is signed is the Value ID of a CVM Data Object (which in turn is the SHA3-256 hash of the Encoding)

## Encoding

Every CVM Data Object has an Encoding, which is a representation of the Object as a sequence of bytes.

Encodings are designed to be:

- Small in size (to minimise storage and network bandwidth requirements)
- Efficient for serialisation and deserialisation
- Canonical (i.e. any Data Object has one and only one valid Encoding)

The maximum Encoding size is 8191 byes. Larger Data Objects are broken down into multiple Cells which each have their individual Encoding - however this is handled automatically by Convex and not usually a relevant concern for users or developers.

## Environment

An Environment on the CVM is a mapping from Symbols to defined values.

The Convex environment should be familiar to those who study the formal semantics of programming languages. It is implemented as a functional, immutable map, where new definitions result in the creation and usage of a new Environment.

Each Account receives it's own independent Environment for programmatic usage. If the Account is an Actor, exported definitions in the environment define the behavior of the Actor.

## Etch

Etch is the underlying Convex storage subsystem - "A database for information that needs to be carved in stone".

Etch implements Converge Immutable Storage for Data Objects.

## Fork

A Fork in a consensus system is, in general, where two or more different groups diverge in agreement on the value of shared Global State.

This creates significant problems with a system of value exchange because assets may have different ownership in different forks - which in some cases could cause major economic loss (e.g. the infamous "double spend problem")

Convex is designed to prevent forks. In the unlikely event of a fork created by malicious actors or software / network failures, the Convex Network will follow the largest majority among known, trusted Peers (this is a governance decision outside the scope of the Protocol).

## Function

A Function is a Data Object that represents a first-class function on the CVM.

Functions may be passed as arguments to other functions, and invoked with arbitrary arguments. They may be anonymous, or given a name within an Environment. They may also be closures, i.e. capture lexical values from the point of creation.

Functions can support multiple arities on the CVM (e.g. `+`, although many functions only support a specific arity.)

## Identicon

An Icon generated in a pre-defined way that can be used to visually confirm if a value is identical to another value. Identicons are used in Convex to provide additional security for similar Addresses that might be hard to distinguish by the hexadecimal strings alone.

## Memory

Memory in Convex is a second native cryptocurrency (in addition to Convex Coins) that can be used to purchase on-chain storage capacity.

Users need to buy Memory if they want to execute transactions that increase the size of the State. USers get a refund if they execute Transactions that reduce the size of the State - creating a good  incentive to use on-chain resources efficiently.

## Memory Accounting

Memory Accounting is the process by which changes in Memory usage are attributed and charged to Users.

This is a necessary feature of Convex to create the right incentives to utilise on-chain memory efficiently. Without a system of Memory Accounting, there would be a risk of careless usage of Memory leading to ever-increasing size of the Global State (sometimes termed the "state growth problem" in Blockchains).

## On-chain

Data or code is considered to be "on-chain" if is contained within or affects the current State of the CVM.

On-chain data is the *only* information that is visible to the CVM. It can be accessed and used by Actors, e.g. as part of the management of smart contracts and digital assets.

As a general principle, on-chain data should be kept to the *absolute minimum necessary*. This is because:

- It has a real cost (in terms of both coins and memory)
- It is effectively public information so should exclude any confidential or private information

## Ordering

An Ordering defines the sequence in which Blocks of Transactions are to be executed. A key purpose role of the Consensus Algorithm is to ensure that all good PEers agree on the same Consensus Ordering, and hence all calculate the same Consensus State.

In normal use of the Convex system, the Ordering maintained by a Peer will be confirmed in Consensus only up to a certain point (the Consensus Point). Blocks after this point are not yet confirmed, but are in the process of being proposed for consensus.

## Peer

A Peer is a system that participates in the operation of the decentralised Convex Network, and in particular helps to achieve Consnesus.

Peers are required to use a private key to sign certain messages as they participate in the Consensus Protocol. Because of this, a Peer's Stake may be at risk if the system is not adequately secured. Peer operators therefore have a strong incentive to maintain good security for their systems.

## Private Key

A cryptographic key that can be used to digitally sign transactions.

Private Keys must be kept secure in order to prevent unauthorised access to Accounts and Digital Assets controlled by that Account.

## Public Key

A cryptographic key that can be used to validate transactions.

Public Keys may be safely shared with others, as they do not allow digital signatures to be created without the corresponding private key. User Accounts in Convex use an Ed25519 Public Key as the Account Keys, which enables any Peer to validate that a transaction for a given user has been signed with the correct Private Key.

## Schedule

The Schedule is a feature in the CVM enabling CVM code to be scheduled for future execution. Once included in the Schedule, such code is *unstoppable* - it's execution is guaranteed by the protocol.

Scheduled code may be used to implement actors that take periodic actions, smart contracts that have defined behavior after a certain period of time etc.

## Smart Contract

A Smart Contract is a self-executing economic contract with the terms of the agreement written into lines of code that are executed deterministically on the CVM. Buyer and sellers can predict exactly how the Smart Contract will behave, and can therefore trust it to enforce contract terms and conditions effectively.

Typically a Smart Contract would be implemented using an Actor, but it is possible for a single Actor to manage many smart contracts, and likewise for a single Smart Contract to be executed across multiple Actors. It may be helpful to think of Smart Contracts as secure economic constructs, and Actors as a lower level implementation mechanism.

## Stake

A Stake is an asset with economic value put at risk by some entity in order to prove commitment to its participation in some economic transaction and / or good future behavior.

Convex uses a mechanism called Delegated Proof of Stake to admit Peers for participation in the Consensus algorithm. Other forms of stakes may be used in Smart Contracts.

## Stake Weighted Voting

Convex uses Stakes to determine the voting weight of each Peer in the Consensus Algorithm.

Benefits for a Peer having a higher effective voting stake are:

* Slightly more influence over which Blocks get ordered first, if two blocks are simultaneously submitted for consensus
* They may also benefit from slightly improved overall latency for Blocks that they submit.

While good Peers are expected to be content neutral, they may legitimately wish to offer better QoS to their partners or customers, and having a higher voting stake can help them to achieve this.

The protocol does not allow Peers to reverse a confirmed consensus, or prevent (censor) a Block from being included in consensus. Their stake may be at risk if they attempt this.


## State

A State is a special (typically large) Value that refers to the complete information managed by execution on the CVM. It is in effect the "Universe" that can be manipulated by Transactions and CVM code.

The latest version of the State is the Consensus Algorithm, and the latest State obtained in Consensus is called the "Consensus State". The Consensus State is particularly important, since it contains the confirmed balances of Convex Coins and other digital assets, as well as the current state of all existing Smart Contracts and other data on the CVM.

## State Transition Function

The State Transition Function is the function that updates the State in response to new Blocks of Transactions after they are confirmed by the Consensus Algorithm. Formally this might be recursively specified as

```
S[n+1] = f(S[n],B[n])

where:
  f is the State Transition Function
  S[n] is the sate after n Blocks have been processed
  B[n] is the Block at position n in the cordering
  S[0] is the pre-defined Initial State
```

## Transaction

A Transaction is an operation that can be submitted by Clients for execution on the Convex Network. A Transaction must be linked to a User Account, and must be digitally signed by the Private Key corresponding to the Account Key in order to be valid.

Transactions must be digitally signed by the owner of the Account in order to be valid.

## Value

A Value is a first-class, immutable piece of information managed by Convex. A Value can be simple (e.g. the number `1`) or composite (e.g., a vector containing other values like `[1 2 [3 4] :foo]`)

Value types include:

* Primitive values (numbers, strings, symbols, binary Blobs)
* Data Structures representing composites of many values (including other data structure)
* Executable CVM code ("Ops")
* Some special values used by the CVM, e.g. Blocks

Values may be processed by code within the CVM, and are the fundamental building blocks for on-chain systems such as smart contracts.

## Wallet

A Wallet is an application or device that stores keys (especially private keys) for Convex Accounts, enabling access and control over digital assets held by those accounts.

Wallet functionality may be provided by a Dapp, or embedded in any system that communicates with the Convex Network. It may also be a specialised hardware device (Hardware Wallet).

Wallet security is paramount: if access to the private keys in a wallet is compromised, any on-chain digital assets (coins, tokens, smart contract rights etc.) may be at risk.