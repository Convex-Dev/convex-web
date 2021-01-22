# Convex White Paper

## Abstract

Decentralised networks offer the opportunity to provide a true peer-to-peer system for the value exchange involving digital assets and services, protected at the protocol level by cryptographic keys that can be issued an managed on a self-sovereign basis - a vision that might well be called the "Internet Of Value". 

However, existing decentralised networks have notable weaknesses including poor performance, high energy consumption, long transactions confirmation times, vulnerability to "front-running" attacks and/or lack of truly decentralised security.

We present Convex, a fully decentralised platform for the Internet of Value. Convex achieves consensus with a novel technical solution based upon merging beliefs shared by peers using a function that is idempotent, commutative and associative, and thus creates a system that provably converges to consensus by forming a conflict-free replicated data type (CRDT). By augmenting this with a system of economic staking, it is possible to guarantee convergence to consensus even in the presence of malicious / faulty peers. We call this combined scheme "Convergent Proof of Stake" (CPoS).

We augment this system with an execution engine, building on the lambda calculus, immutable persistent data structures and content addressable storage. Coupled with the consensus algorithm, this provides a fully decentralised, global computer capable of executing arbitrary smart contracts with decentralised ownership (the "Convex Virtual Machine").

## Context

### Towards a Digital Economy

Towards the end of the 20th Century, the foundational ideas were created for the Digital Economy. The start of the Information Age saw a wealth of innovation, as significant sectors of economic activity moved to the Internet:

* Online shops and marketplaces were developed, some of which became giant businesses (Amazon)
* Information and advertisements moved online (Google)
* Entertainment, games, media, communication and social activity moved online (Facebook, Twitter, Instagram)
* Many business activities and tools moved online (GitHub, Salesforce, Slack)

At the same time ideas were generated that hinted at the potential for economic value exchange *itself* to move to the Internet:

* Digital currencies were proposed and implemented
* Key concepts able to enforce terms on Digital transactions such as Smart Contracts were introduced. 
* Innovations (both technical and legal) were developed to allow use of mechanisms such as digital signatures.

A problem with moving Value Exchange to the Internet, however, is that parts of the economic transaction still rely on pre-Internet mechanisms: traditional fiat currencies, paper-based contracts and centuries-old legal systems for enforcement. Execution of complete transactions usually depends on  trusting a single centralised entity to operate the interfaces between the Traditional and Internet worlds - handling legal issues, settling payments etc. Under such a model, economics of scale and network effects tend to favor a few commercial giants at the expense of smaller companies. This is a net loss to the economy: stifling innovation, excluding new competition, allowing monopolistic behaviour and "locking in" consumers without many practical choices.

### The Internet of Value

We envision a system that enables value exchange mechanisms while solving the problems of traditional approaches - a network of economic agents that serves the needs of the digital economy.

The seven key properties that are essential to this vision are that it must be:

* **Global** - supporting the the whole world
* **Open** - accessible to everyone, with no unjust barriers to inclusion and participation, and no centralised control. Openness *requires* decentralisation.
* **Automated** - able to execute end-to-end transactions in a reliable way while automatically enforcing any rules relating to the digital assets involved.
* **Secure** - providing strong protection against security threats (malicious or otherwise) so that holders of digital assets can maintain trust in their value.
* **Extensible** - capable of supporting new kinds of digital assets and innovation
* **Fast** - sufficiently scalable and efficient to serve Internet-scale usage, and meet the performance expectations of all reasonable use cases 
* **Cheap** - usable with low economic costs, to maximise the potential to create economic opportunity and value without being stifled by transaction costs.

Convex has been designed from the ground up to provide these properties.

### Applications

The Internet of Value's primary purpose is to enable **decentralised applications** that typically involve digital assets and value exchange. Just as anyone can create a website on the Internet, anyone can create a decentralised application for the Internet of Value. Convex is therefore designed to make the process as simple and effective as possible.

There is no practical limit to the ideas that could be implemented given an open and extensible system. Some notable ideas include:

* Implementation of cryptocurrencies, utility tokens, and other forms of decentralised assets
* Economic transactions where terms and conditions are automatically guaranteed by Smart Contracts
* Games and entertainment where rules include ownership of digital assets
* Educational environments for collaborative and interactive programming
* Immutable records of document / data provenance
* Publicly accessible databases and registries

### Why is Convex needed?

Convex is needed because, despite the vast potential of the Internet Of Value, a technology did not previously exist to enable it in a satisfactory way. We need a system that is *simultaneously* Global, Open, Automated, Secure, Extensible, Fast and Cheap. Previous systems fall short on one or more of these points.

Convex builds on ideas around decentralised technology popularised through blockchain innovations in recent years, but was motivated by a desire to build something better than current incarnations of blockchain technology can offer. For example, Bitcoin does well on Global, Open and Secure, but is definitely not Fast or Cheap. 

Key additional motivations for the creation of Convex, while not strictly necessary for the Internet of Value, include:

* Help the environment by supplanting systems based on Proof of Work. 
* Make decentralised software development more productive, engaging and fun
* Establish new paradigms of decentralised programming around globally shared, consistent state
  
### Prior Innovation

The space of decentralised technology has seen massive innovation in recent years, many of which have inspired the Convex project. It is not the purpose of this White Paper to chronicle all these exciting developments in detail, but some particularly significant events are worth noting:

In 2009, Bitcoin was launched by Satoshi Nakamoto, which demonstrated for the first time that a digital currency could be operated on a fully decentralised, secure network using a Proof of Work consensus algorithm. The ability to prevent "double spending" using a purely decentralised, online method was a revelation that hinted at the possibility of entire economic systems migrating to the Internet.

In 2015, Ethereum was launched, which extended the concept of the Bitcoin network with a decentralised virtual machine (EVM) capable of executing Turing-complete smart contracts with a global state machine. This enabled a wave of innovations such as tokenisation of assets, and the first attempts at Decentralised Autonomous Organisations.

These innovations paved the way for significant experimentation in the space of digital currencies, tokenisation and cryptoeconomics. The space has attracted massive investment and seen vigorous innovation in recent years, hinting at the enormous opportunities presented by digital value exchange.

### Technical Challenges

However, Blockchain technologies suffer from a range of issues which have proved hard to resolve. On the technical side, Ethereum founder Vitalik Buterin noted the "Scalability Trilemma" which is that is extremely hard to achieve the combination of:

* **Scalability** - Ability to offer performance comparable to traditional payment systems such as VISA
* **Security** - Resistance to attacks on assets and information integrity (such as double spending of digital currency)
* **Decentralisation** - Ability to operate free from centralised control by a single entity or group of powerful entities

Given that security is essential, and that without decentralisation blockchains offer no compelling reason to switch from centralised solutions, blockchains have generally **sacrificed scalability** to the extent that they are not practical for large scale use cases.

Other technical challenges became apparent over time. Some notable issues:

* **Energy wastage** - The use of "Proof of Work" consensus algorithms has resulted in vast and wasteful energy consumption. This is particularly apparent in the Bitcoin and Ethereum 1.0 networks, which rely on vast amounts of computing power dedicated to hashing.
* **Unbounded state growth** has been noted in many contexts - on-chain state must in a blockchain in many cases be preserved indefinitely, but the economic costs of this often do not fall on those responsible for creating new state - leading to an instance of "the tragedy of the commons". Over time, this may make it impossible for normal machines to run a node, effectively halting decentralisation.
* **Front-Running** is a particularly important problem in decentralised finance, where it is possible to steal value from others by quickly inserting a transaction before that of another user, and is exacerbated by the problem of long block times
* **Cross chain integration** presents a particular problem where different decentralised platforms provide different specialised capabilities, but need to be integrated to form a combined solution. The problems of maintaining consensus, security, reliability etc. are magnified in such situations.
* **Latency** - The time taken for most blockchains to confirm final consensus is frequently too long to offer a positive user experience. This inability to provide quick confirmation and feedback is a significant barrier to mainstream user adoption of decentralised applications.
* **Upgradability** - Both networks themselves, and the specific implementations of smart contracts, are difficult to upgrade, in some cases requiring a "hard fork" of the network.

Convex presents a solution to most of these challenges, and as such we believe it allows a significant evolution "Beyond Blockchain" to deliver the Internet of Value. The remainder of this White Paper explains how we achieve this.

## Convex Overview

### The Convex Solution

Convex solves many of the technical challenges of Blockchains. With reference to the Scalability Trilemma, Convex offers:

* **Thousands of transactions per second** - Convex offers the capability to operate at VISA-like transaction levels and sub-second block times even *before* scalability solutions such as Layer 2 solutions, state sharding or optimistic lookahead approaches are applied. Early benchmarking suggests peers running commodity hardware may be able to handle in excess of 40,000 transactions per second.
* **Byzantine Fault Tolerance** - Convex meets the strongest possible threshold for security under the model of Byzantine threats. Consensus formation is guaranteed (and stable) as long as at least 2/3 of the effective voting power of the network follows the protocol honestly.
* **Fully Decentralised** - The network operates under a permissionless Peer-to-Peer model: Anyone can operate a Peer in the network, anyone can submit a transaction for execution, and transactions cannot be censored (subject to the usual security assumptions). 

But Convex is not simply a faster Blockchain - it is a platform for building digital economic systems. As such, it combines a number of capabilities that together enable construction of new classes of applications.

Some technical highlights of the Convex design that support such applications include:

* **Actors**: Programs that execute autonomously in the Convex environment with deterministic and verifiable behaviour, suitable for managing assets and enforcing Smart Contracts
* **Convex Virtual Machine (CVM)** - a fully Turing complete programming and execution environment, with a novel combination of language features to facilitate writing decentralised applications. We manage to implement a working Lisp compiler "on-chain".
* **Decentralised Data Value Model** - A data model supporting powerful features such as orthogonal persistence, memory accounting, incremental data sharing and cryptographic verification
* **Performance**: High throughput, low latency execution (many thousands of transactions per second, ~1 second or below latency)
* **Security**: Cryptographic security for control over all user accounts and assets, byzantine fault tolerance at the level of the decentralised network.

The main sections of this White Paper describe the key subsystems of Convex that make all this possible:

* The **Consensus Algorithm** which enables the Convex network of Peers to agree on a consistent, replicated state of the world - essential to provide reliable confirmation of transactions in a decentralised system 
* The **Execution Engine** performs computations necessary to implement secured transactions on the Convex Network, and is the mechanism by which global state is updated and smart contracts are enforced.
* The **Storage System** enables Peers to store and manage data volumes with a level of scale and performance necessary to support a high throughput of transactions and data volume.

We summarise these three areas below:

### Consensus Algorithm

Convex, like other decentralised systems, depends upon a consensus algorithm to ensure that everyone agrees on a single version of the truth - this is a precondition for any decentralised economic system that needs to enforce ownership of digital assets.

Similar to blockchain technology, Convex allows **Blocks** consisting of multiple transactions to be submitted to the network. In contract to blockchains however, the blocks are not linked to previous blocks - there is no "chain" as such. Relaxing this requirement enables Convex to handle new block submission concurrently, significantly improving performance.

The role of the consensus algorithm is to create an **Ordering** of blocks. A stable order solves the famous "double spend" problem by ensuring that only the first transaction is able to spend any given funds or assets. Any later transaction that attempts to spend the same funds will fail.

The algorithm operates by implementing a novel variant of a **Conflict-free Replicated Data Type** (CRDT), which can be proven to converge to a stable consensus through a few rounds of random gossip between Peers. This approach is efficient, robust to temporary failures, and provably secure even in the presence of malicious or faulty peers (i.e. it is "Byzantine Fault Tolerant" under reasonable security assumptions).

The Convex consensus algorithm also makes use of **Proof of Stake**, a mechanism by which peers are required to deposit an economically significant stake to ensure their good behaviour and be granted participation rights in the consensus protocol. This avoids the wasteful use of resources and energy that plagues systems based on "Proof of Work". As well as offering substantially improved performance, this means that Convex presents an environmentally friendly alternative to previous models such as Bitcoin or Ethereum.

### Execution Engine

Convex implements a full virtual machine for smart contracts, the **Convex Virtual Machine (CVM)**. The CVM is designed to facilitate digital economic transactions. Given an initial State and an ordering of Blocks (and therefore transactions) from the consensus algorithm, the CVM is able to process the transactions and compute a new updated State. The latest state contains information of interest to users of the Convex network, in particular the record of ownership of digital assets.

The CVM has the capability to execute arbitrary, Turing-complete **smart contracts** which in turn can be used to express the logic of digital assets and decentralised applications.

Some particular innovations of interest to facilitate the development of decentralised applications:

* **Decentralised Data Values (DDVs)**  - A system of data types and structures enabling efficient and secure replication of data across the Convex network, and supporting the implementation of the CVM. The CVM works with a wide variety of data types enabling construction of powerful applications with optimised performance. 
* **Convex Lisp** - A powerful language where CVM code is itself expressed as Decentralised Data Values. The compiler itself executes on-chain - giving developers and Actors the power to construct, compile and deploy new actors on-chain without external tools. This enables systems of on-chain MetaActors - actors who can autonomously create and manage other actors.
* **Scheduled Execution** - The protocol allows for deterministic execution of Actor code at any future point in time. This allows for more advanced, time-based processes to be implemented on chain (without such a feature, smart contracts would need external systems and events to trigger execution at specific times, such as the Ethereum Alarm Clock )
* **Execution Worlds** - Each account on the network (external user or Actor) is granted a secure, scriptable code execution environment with its own database. This enables highly interactive use of the CVM by advanced users.

### Storage System

Convex implemented a novel storage scheme, specifically designed to support the requirements of Convex DDVs. Key features of this system include:

* **Content addressable storage (CAS)** - The key for every value in the database is the cryptographic hash of its encoding.
* **Smart References** - references to data that can be lazily loaded and verified, allowing just a small required subset of data to be accessed on demand.
* **Transparent Persistence** - Decentralised Data Values used in Convex (such as the CVM state) are stored in a virtual database which may be much larger than main memory. This opens up interesting opportunities for future scalability and sophisticated Actors capable of working with large databases.
* **Novelty Detection** - The design of the storage system enables Convex to detect *novel* information when it is written to storage. This is important to reduce bandwidth requirements: only novel information will typically need to be broadcast to the Peer network.
* **Proofed Persistence** - Certain proofs relating the the validation of data are persisted along with the data itself. This is an important optimisation: Entire large data structures can be verified in O(1) time by checking the cached proof.

An important feature *excluded* from the storage system is that of "update". Once written, data values are immutable and cannot be changed. This limitation is appropriate given that we are working with cryptographic hashes of keys: finding a different data value that maps to the same key would require breaking SHA3-256.

The database engine itself is called Etch. Etch is an embedded database engine optimised for these specific requirements. We believe that building a customised engine is a worthwhile investment, because of the specific feature requirements and performance improvements possible - at least an order of magnitude faster than using more traditional, general purpose databases.

The Storage System supports optional garbage collection for Peers that wish to compact storage size. A Peer is only required to maintain the current state, and a short history sufficient to participate in the consensus algorithm. Of course, Peers may choose to retain additional information for historical analysis.


## Technical Description

### Design Rationale

It is worth reflecting on the logic behind the design of Convex: this logic has driven the majority of key design decisions in the construction of the Convex system.

Convex works on the principle of proving a globally shared state on a permissionless decentralised network which has the ability to execute instructions (transactions) on behalf of users: a "public computer" that everyone can access but where nobody has absolute control. 

The motivation for the development of a system of this nature is because it can act as the foundation for the Internet of Value - a system of decentralised value exchange built in the open spirit of the original Internet. But is this necessary? Could it not be done in a simpler way? In this section we argue that these capabilities are necessary and sufficient (given the additional obvious assumption of adequate performance).

It is important to consider why the Internet itself does not already function as an "Internet of Value". The key insight here is that the original Internet is primarily a **stateless** system that enables communication between different participants. A system of digital value exchange requires **state** - at the very minimum it must be able to record the ownership of digital assets (in Bitcoin, for example, this state is manifested in the set of UXTOs). While clients and servers on the Internet can certainly store state, such state is inadequate for decentralised value exchange because it is susceptible to corruption or arbitrary modification by the party that controls the system.

If we are unable to trust centralised actors as the sole arbiters of truth, the solution must therefore involve decentralised verification. It must furthermore ensure **consensus** in order that the whole network sees the same verified state - assets cannot reliably be used for value exchange if there is ambiguity over ownership (the "double spend" problem). To provide the basis for a global, open network, the consensus itself must be **global** and available to all participants. 

Because we wish to ensure openness and avoid the issue of centralised control over the system, it must also be **permissionless** in the sense that any actor can participate on an equal basis and not be excluded or censored by other participants wielding excessive power. This ideal of decentralisation presents the problem however that some actors in the system may be malicious and actively attempting to defraud other actors - a significant problem when valuable digital assets are at stake. Furthermore, even if not malicious, software or hardware failures can potentially disrupt the system. We therefore require the property of **Byzantine Fault Tolerance** - which can be informally  characterised as resistance of the consensus mechanism to malicious attacks and failures. Theoretical results (e.g. the seminal work by Leslie Lamport) have demonstrated that such consensus requires 2/3 of participants to be good actors - that is, byzantine fault tolerance is possible to achieve as long as no more than 1/3 of actors are malicious or faulty. We would like the Internet of Value to operate with a consensus algorithm that achieves this theoretical optimum level of security.

We now consider the nature of Digital Assets, and what is necessary to implement them. A key observation is that for assets to be meaningful, they must obey rules. The most obvious example is that of ownership: only the owner of an asset should be able to use it in an economic transaction. But other rules may also apply, e.g. a financial option includes a right to "exercise" the option in exchange for some other underlying asset. Since assets only have value if owners trust that their rules will be enforced, we need a system of encoding and executing these rules in an automated, verifiable way as part of the decentralised protocol - such rules must be implemented as **smart contracts**. 

While it would be possible to create a simple system of smart contracts that tackle many useful applications without full programmability, the Internet of Value calls for extensibility to new forms of assets that may not be originally anticipated by the designers of the network. We therefore require a **Turing complete** smart contract execution model (capable of performing any computation) if we are to avoid the risk of future limitations preventing important digital asset classes from being created.

To ensure that only valid transactions are executed on digital assets with the authorisation of the owner, we need a secure way to validate the identity and . This is fortunately a well-studied problem that can be solved with cryptographic techniques, and in particular **digital signatures**, where the authenticity of a transaction can be validated through the use of a secret private key held by users, and a public key that is visible to all.

Maintenance of the network consensus invariably requires resources: powerful servers with compute capability, significant storage and network bandwidth are necessary to operate the consensus algorithm and execute transactions 24/7 at global scale. These resources are ultimately not free, and to compensate their operators for the economic cost of their services there is a need to impose **transaction fees** for usage. Without some economic cost of transacting, the network could be swamped by low-value or malicious transactions that consume excessive resources. The need to charge a transaction fee naturally leads to the conclusion that some form of **native currency** is required, being a digital currency that is valid to pay for the usage of network services (which can be enforced at the protocol level). 

Finally, we note some practical considerations. Information will need to be durably maintained and frequently communicated as part of the consensus algorithm and in serving users of the network, which requires systems for **storage** and **transmission** of data. Ideally, such systems need to be **efficient** to provide necessary storage, and they must ensure **integrity** to allow recovery from faults and malicious tampering.

The remainder of this White Paper describes the technical implementation of Convex, which implements all of the above capabilities in order to provide a foundation for the Internet of Value.

### A note on Values

As a distributed information system, Convex must deal with the representation of data. We therefore rely frequently on the definition of a Decentralised Data Value (DDV) which has the following properties:

- It is immutable: the information content cannot be changed once constructed
- It has a unique canonical encoding as a sequence of bytes, which is used for both network transmission and storage
- It can be assigned a unique Value ID (VID) which is defined as the SHA3-256 cryptographic hash of the value's encoding. This serves multiple purposes, most importantly for cryptographic verification and as a key for various data structures (e.g. hash maps and Content Addressable Storage).
- Other DDVs can be recursively nested inside - in essence forming a Merkle Tree, which becomes a Merkle DAG given structural sharing of identical children.

Where we refer to "value" or "data value" in this document, we are generally referring to DDVs.

### Consensus Algorithm

#### State

At the core of the Convex consensus design is the concept of the State.

The State is a representation of the complete information in the on-chain execution environment at any point in time. The Convex network as a whole operates as a globally replicated state machine, where new updates cause changes to the current State. Updates are defined on a globally consistent basis according to the sequence of transactions confirmed through the Consensus algorithm.

The State is represented as an immutable Decentralised Data Value (DDV) that includes:
*	All Account information and balances
*	All Actor code and state
*	All information relating to active Peers and staking
*	Global information (such as the latest Block timestamp)

Since it is a DDV, it follows that a State is a Merkle Tree, and has a unique VID. This means that if two Peers compute a State update and announce the VIDs, it is possible to immediately validate if they computed the same resulting State.

#### Reduction to Block Ordering Problem

Consensus in the state of a decentralised machine can be achieved with the combination of:

* Agreement on some initial State: `S[0]`
* Consensus over the complete ordering of Blocks of transactions `B[n]` 
* A deterministic state update function, which updates the state according to the transactions contained in a Block: `S[n+1] = f(S[n],B[n])`

This construction reduces the problem of generalised consensus to the problem of determining consensus over Block ordering. The CVM execution environment provides the state update function, which is orthogonal to the consensus algorithm but provides the necessary state updates given a total ordering of Blocks.

We define the **consensus point** to be the number of Blocks confirmed by the consensus algorithm, and the **consensus state** to be the state obtained after applying the state update function up to the consensus point.

#### Peers and the Consensus Set

Convex defines the set of actors that participate in the consensus algorithm as Peers in the network.

Any actor may create a Peer by placing an economic **stake**, denominated in the native currency of the network (Convex Coins). The size of the stake determines the relative voting power of the peer in the consensus algorithm, but is at risk of forfeiture in the case that bad behaviour is provably detected, or could be appropriated by malicious actors if the Peer does not maintain strong security for their system (in particular the security of the Peer's private key).

As a reward for helping to operate the network, Peers are entitled to a share of fees for transactions executed on the network. This reward is proportionate to Peer stake.

#### Block proposals

Traditional Blockchain solutions have focused on mechanisms to determine which participant gains the right to propose the next block, which includes a hash of the previous block in order to extend a linked chain of blocks. This was the basis for the original Bitcoin Proof of Work algorithm (which used the ability to mine cryptographic hashes as the basis for allowing a miner to publish a block and claim the corresponding block reward).

This approach of selecting a "leader" to publish a new block has some problems:

* It is hard to determine which participant should be the next leader, at least in a way that simultaneously works efficiently, provides security in the presence of potential byzantine actors, and is guaranteed to make progress in cases such as leaders becoming unavailable.
* Including the hash of the previous block in a chain creates an inherent data dependency that limits the ability to propose blocks in parallel and increases latency - each leader must build upon the work of the previous leader sequentially, which implies a minimum lower bound on the block time (given fundamental physical constraints).
* It is necessary to make sure that the leader possesses the transactions that should be included in a Block. This implies the need for a mechanism to transmit transactions across peers prior to block creation (e.g. with the "mempool" used by Bitcoin to share pending transactions), which in turn implies communication costs and many opportunities for attacker to launch "front running" attacks on transactions that they observe.

Convex therefore eschews the idea of selecting a leader. **Any Peer may propose a new Block at any time**, and the new Block is independent of all previous Blocks, i.e. it does not form a "chain". A consequence of this is that is is possible for multiple Peers to propose valid blocks for inclusion in consensus at the same time.


#### Convergence

Convex uses a variant of Convergent Replicated Data Types (CRDTs)  to provide part of this solution. CRDTs have the provable property of Eventual Consistency - which might be informally defined as a situation where all peers eventually reach the same state given no novel transactions in the network.

This is achieved through:

* A data structure called a **Belief**, which represents a Peer's view of consensus across the whole network, including the most recent proposed Block orderings from other Peers
* A **Belief Merge function** for each Peer, which:
  * Combines any two (or more) Beliefs to create an updated Belief
  * Is idempotent, commutative and associative with respect to the merging of other Beliefs
  * Converges on a stable ordering of Blocks

This effectively forms a join-semilattice for each Peer, and satisfies the conditions required for a CRDT.

There is one subtle point that makes this different from a typical CRDT: Peers are only able to update the part of the overall Belief structure that represents their *own* proposals (this is enforced via digital signatures).

Convergence is guaranteed by a system of continuous voting. At each belief merge step, the Peer computes the total share of stake voting for each proposed Block in the next position after the current consensus point. It is able to do this because it has a view of the orderings proposed by all other Peers. 

This voting is applied iteratively to blocks in following positions, but only counting the votes by Peers that have supported the winning ordering up to this point (i.e. supporting a minority Block causes Peers to be excluded from the considered vote).

Once the overall winning ordering has been determined, the Peer appends any new Blocks it wishes to propose, then adopts this ordering as it's own proposal. The proposal is incorporated into the Peer's own Belief, which is then communicated back to the other Peers.

This procedure naturally converges to a single ordering: Any situation where Peers are voting for different Blocks is unstable and will collapse towards one outcome, since Peers will switch to whichever ordering is observed to have a slight majority. After a few rounds of gossip, the good Peers (at minimum) will align on the same ordering.
 
#### Stability

At some point, we therefore reach a threshold where greater than 2/3 of the Peers (by stake weight) are aligned in proposing the same ordering. This situation is **stable** because of the following logic:

* Since less than 1/3 of Peers are potentially malicious, the number of good peers in the agreed consensus set strictly outweigh the malicious peers.
* Even if all malicious peers were to act in concert and attempt to disrupt consensus (the worst case is where they initially support the ordering, then simultaneously switch to a new ordering), they would fail because they are outnumbered by good peers already in alignment, which would (probabilistically) secure the support of the majority of remaining non-aligned good peers and eventually reach the 2/3 threshold again.


#### Determining consensus

Once the threshold of Peers are observed by any Peer to be voting for the same ordering up to a certain Block number in the ordering, the Peer marks and communicates this as a **proposed consensus point**. And once 2/3 of Peers are observed to have the same proposed consensus point with the same ordering, this value is taken by the Peer as the **new consensus point**. 

This is essentially a decentralised implementation of a 2-phase commit, and is used as a precaution against malicious Peers attempting to reverse consensus by changing their vote at the last minute (this might delay the consensus briefly, but not stop it, since we are already past the tipping point where the good peers supporting the proposed consensus outweigh the bad peers).

#### Illustration

Consider a case where all peers A,B,C,D,E initially agree on an ordering (labeled `1`). At this instant, peer B receives a set of new transactions, and proposes an updated ordering `2`. Initially, this is unknown to all other Peers. We can visualise this situation as a Matrix, where each row is the Belief help by one peer, and each column represents the latest signed belief observed by each peer from another peer. Each Peer also has knowledge of the current consensus defined by `1`.

```
  ABCDE Consensus
 
A 11111 1 
B 12111 1
C 11111 1
D 11111 1
E 11111 1
```

At this point, Peer B "Gossips" its new Belief to other Peers. The other Peers observe that Peer B has proposed a new ordering, and incorporate this into their local view of Peer B:

```
  ABCDE Consensus
 
A 12111 1 
B 12111 1 
C 12111 1
D 12111 1
E 12111 1
```

With this information, all Peers are aware of a new ordering. They validate that this is compatible with the previous consensus, and because it is a valid extension of `1` they automatically adopt it as their own proposed ordering (the diagonal of the matrix).

```
  ABCDE Consensus
 
A 22111 1
B 12111 1
C 12211 1
D 12121 1
E 12112 1
```

Another round of gossip is performed. Now each peer is aware of the latest ordering `2` being communicated by all other Peers:

```
  ABCDE Consensus
 
A 22222 1
B 22222 1
C 22222 1
D 22222 1
E 22222 1
```

At this point, Peers are able to confirm that the proposed ordering appears to meet the threshold for a new consensus. However, they don't yet know that other Peers have confirmed this, so another round of gossip is performed to ensure that the 2-phase commit protocol is completed. After this round, all peers know that the consensus threshold has been met, and update their own view of consensus accordingly.

```
  ABCDE Consensus
 
A 22222 2
B 22222 2
C 22222 2
D 22222 2
E 22222 2
```

The network is now in a new quiescent state, with consensus advanced, and ready to process the next set of proposed transactions.

This illustration is a deliberate simplification. In practice:

* Multiple Peers may propose transactions at the same time. In this case, stake weighed voting would be used to determine which transactions are included first.
* The network might not reach a quiescent state before further new transactions are added. This is not an issue: consensus can be confirmed up to the point of the initial transactions while the new transactions are still being gossiped.
* Some Peers might misbehave, or be disconnected from the network. Again, this is not a problem as long as a sufficient number of good peers are still operating and connected, since the consensus threshold can still be met. Temporarily disconnected or offline Peers can "catch up" later.

#### A note on complexity

At first glance, the Convex consensus algorithm might be considered impractical because of the scale of data structures being shared. Consider a plausible high volume operating scenario:

* n = 1,000 Peers active in the network
* r = 10 new blocks per second
* s = 10k of data for each block (about 100 transactions)
* o = 1,000,000,000 Blocks of transactions in the current ordering (a few years of blocks)

Each Peer would theoretically be holding ~100 petabytes of information for their Belief, which would need to be transmitted in each gossip round, requiring a bandwidth in the order of many exabytes per second. Clearly this is not practical given current hardware or network capacity.

However, we exploit some powerful techniques to minimise this:

* Beliefs are represented as Decentralised Data Values that support **structural sharing**: identical values or subtrees with identical values need only be stored once. Since orderings are identical up to the point of consensus, these can be de-duplicated almost perfectly.
* Peers are only required to actively maintain block data for approx. **1 day** (less than 10GB in this case)
* The Decentralised Data Values support usage where only the **incremental change** needs to be gossiped. 
* The number of outgoing connections for each Peer is **bounded** to a small constant number of Peers that they wish to gossip to (typically around 10, but configurable on a per-peer basis)

With these techniques, Peers only need to gossip the novelty they receive (in this example around 100k per second, plus some accounting overhead) to a small number of other peers. Bandwidth required is therefore on the order of 2-5MB/s (allowing for some overheads and a reasonable number of Peer connections), which is certainly practical for any modern server with a reasonable network connection.

Overall complexity is therefore (factoring out constants):

* $O(r \times s)$ bandwidth, scaling with the rate of new transaction data size
* $O(r \times s)$ storage, scaling with the rate of new transaction data size
* $O(\log n)$ latency, scaling with the logarithm of number of Peers (based on standard analysis of gossip networks)

We believe this is a theoretical optimum for any decentralised network that maintains consensus over a global state. Lower latency could potentially be achieved by communicating to all peers simultaneously, however this would cause problems with bandwidth scaling.

#### A note on "front-running"

Front running is difficult with the Convex consensus algorithm. While theoretically possible, it would require a sophisticated and well-resourced attacker.

The main reason for this is that transactions are not visible to the network until *after* a new block has been proposed by a Peer and gossipped to the network, by which point it is already well on its way to being included in consensus.

A user concerned about front-running attacks should submit vulnerable transactions exclusively via a well connected, well-staked Peer (trusted not to be malicious, i.e. this Peer must itself not be participating in a front-running attack).

In this scenario front running attack would need to:

* Listen to vulnerable transactions broadcast on the network
* Quickly generate a new Block with the transaction(s) used to execute the front-running attack
* Have sufficient influence over consensus formation to ensure that the new Block is somehow re-ordered *before* the original Block (that is already approaching consensus)

Practically, this attack would require the attacker to have more resources (stake and network connectivity) than the original Peer *and all the good Peers it is connected to*, since the original Block would already be ahead by at least one round of gossip. Furthermore, the attack would be publicly visible and traceable to the Peer(s) that launched it: so even if successful the attacker would be vulnerable to blacklisting and/or real world legal repercussions.

Assuming good Peers are well-staked, and connect preferentially to other well-staked, trusted Peers with a known legal identity (which would be good practice, and should be configured as default behaviour), we believe such front running attacks will be generally impractical from an economic perspective.


### Execution Engine

The convex execution engine is referred to as the Convex Virtual Machine (CVM). This is a general purpose computational environment that can be used to implement the state transitions triggered by transactions.

#### Accounts

The fundamental control mechanism for the CVM is via Accounts.

* **User Accounts**: Accounts that are controlled by external users, where access is controlled by Ed25519 digital signatures on transactions.
* **Actor Accounts**: Accounts that are managed by an autonomous Actor, where behaviour is 100% deterministic according the the associated CVM code. Actor functionality may be invoked within a externally submitted transaction, but only if this is initiated and validated via a User Account.

It is important to note particular the two types of Account share a common abstraction. Both User Accounts and Actor Accounts may hold exclusive control over assets, allowing for decentralised value exchange mediated by smart contracts. This common abstraction is useful, because it makes it simple to write code that does not need to distinguish between assets controlled directly by a user and assets managed by a Smart Contract.

#### Information Model

Convex requires a standard data model because in order for consensus to be useful, it must be agreed precisely what the information in consensus represents

Design decisions regarding the information model have been driven by a number of theoretical and pragmatic considerations

- Representing types that are theoretically sound fundamental to computing such as vectors, maps and lambda functions
- Providing types that are generally likely to be useful for developers of decentralised asset systems
- Using types that are conveniently represented in modern computing platforms (e.g. the use of 64-bit Long integers and IEEE 754 double precision floating point values)
- Ensuring that information can be efficiently encoded to minimise storage and bandwidth requirements

Convex therefore implements a comprehensive set of data types, which are utilised both within the CVM and in the broader implementation of a Convex Peer (including the consensus algorithm and communication protocols).

All data types available in the CVM are considered as Decentralised Data Values (DDVs).

##### Primitive types

Eight integral primitive types are supported, consistent with a typical modern language and broadly equivalent to those available on the JVM:

* byte
* short
* int
* long
* float
* double
* char
* boolean

These behave generally as expected, with the important proviso that arithmetic is implemented exclusively using long and double types (other types are automatically upcast to long and double as required).

There is also a set of non-integral primitive objects generally useful for programming on the CVM:

* Keyword (`:foo`)
* Symbol (`bar`)
* String (`"Hello"`)
* Address (32 bytes hex, e.g. `1f6ca3a7a6fccc7a4ee8034a297b559ee7b343a1912aab89586126564dec166a`)
* Hash (32 bytes hex, e.g. `04ef3428895f2bccbf67a19e33f4a0ba0d9f67a0eb5eb1841cea13f6abff3134`)

Data values that are sufficiently small, including all of the above, are **embedded** directly within larger Data Values that contain them. This is an internal implementation detail to reduce the overhead of storing and communicating many small objects independently, which is transparent to CVM code.



##### Blobs

A Blob is an arbitrary-length sequence of bytes, and is considered a first class object on the CVM. 

Internally, Blobs are stored as a Merkle tree of chunks of up to 4096 bytes in length. Blobs may exceed the size of working memory: they can technically be up to 2^63-1 bytes in length. While the main Convex network is not specifically designed for large object storage, these constructs could be used in future as a basis for decentralised file storage, perhaps as a Layer 2 solution, in a manner similar to IPFS.

For many purposes, Addresses and Hashes can be also be treated as small fixed-length Blobs.

##### Data Structures

Convex supports a range of first-class data structures, primarily:

* Vectors (`[1 2 3]`)
* Lists (`(foo bar baz)`)
* Maps (`{:bar 1 :baz 2}`}
* Sets (`#{1 2 3}`

All data structures are immutable, functional data structures that support structural sharing based on an underlying tree representation (in fact, a Merkle tree). This generally provides efficient O(log n) operations for append, access, update etc. These data structures are similar to the data structures frequently found in modern functional programming languages such as Clojure, Scala or Haskell.

A moderately high branching factor (usually 16) is used. This is important because:

* It facilitates faster lookups (less nodes to traverse by a factor of 4 vs. a binary tree)
* It reduces the number of new node allocations required to update a path to a leaf node. 
* It reduces the number of hashes that need to be computed (a performance bottleneck in some cases)
* There is a certain elegance and performance benefit in being able to index the tree using hex digits

##### Syntax Objects

Syntax Objects are wrapped values that contain both a raw value and optional metadata. 

Metadata may be an arbitrary Map, but typically would include such things as:

* Source code references
* Documentation
* Information generated through macro expansion

Syntax Objects are inspired by Racket, and are generally used for code generation and compilation, although they are also available for use in regular CVM code if desired. They are marginally more efficient than storing a value and metadata as two separate fields in a Map, for example.

The primary usages of Syntax Objects within the CVM are:

* Allowing metadata to be attached to values within the dynamic environment of an Account (e.g. documentation for Actor functions)
* Supporting the implementation of the Convex Lisp compiler and macro system

NOTE: In the future Syntax Objects may be extended to implement a gradual type system such as seen in Typed Racket.

##### Nil

Convex supports the value `nil` as a first class value. 

By convention, core runtime functions generally return `nil` to indicate the absence of a value, e.g. looking up a value in a map with a key that is not present.

There is no direct equivalent of a `NullPointerException` since CVM objects do not implement methods, however careless use of nils may result in type casting errors (e.g. `(+ 2 nil)`). 

##### Records

Certain CVM structures are defined as built-in record types, e.g.

* AccountStatus
* PeerStatus
* State

These are primarily used internally by the Convex Peer and CVM implementations, though for convenience they may be accessed and treated as Maps from field names to values in CVM code.

NOTE: Allowing user-defined, row-polymorphic records is under consideration for future implementation.

##### Ops

Ops are low level, programmatic constructs that represent individual instructions on the CVM. They can be considered analogous to machine code instructions on the CVM. Currently the Ops supported are:

- `cond` - conditional evaluation
- `constant` - load a constant value
- `def` - modify environment (map of symbols to values)
- `do` - sequential composition of operations
- `invoke` - execution of a Function
- `lambda` - instantiation of a Function
- `let` - definition of values in a lexical scope
- `lookup` - lookup of a value in the environment

These Ops are similar to the some basic primitives often found in a Lisp machine. It should be noted that certain other important primitives e.g. `cons`, `quote`, `=` etc. are currently implemented as functions in the runtime environment.

##### Functions

Functions are first class objects suitable for use in functional programming. We choose to implement functions directly in the CVM because they are fundamental and powerful constructs that allow the construction of effective programs without having to simulate them with lower level constructs (e.g. a stack based model).

Important features of functions include:

* Support for variable arity function application, e.g. `(+ 1 2 3 4)`
* Full lexical closures (capturing values in the lexical environment at the point of creation)

Some functions are provided by default as part of the runtime environment, which are generally available in the standard library `convex.core`. These functions provide the foundation for construction of higher level functionality.

In addition (adopting an idiom that has proved convenient in the Clojure language), data structures may be used in place of functions in some defined circumstances, e.g.:

* Maps may be used as functions that implement map lookup: `({:foo 1 :bar 2} :bar) => 2`
* Sets may be used as functions to test membership: `(#{1 2 3} 4) => false`
* Vectors may be used to perform indexed lookup: `([1 2 3] 2) => 3`

#### Execution constraints

Since the CVM supports Turing-complete computation, it is necessary to place constraints upon code execution to prevent erroneous, sloppily-written or malicious code from consuming excessive resources. This is particularly important in a decentralised system because such resources are a global, shared cost.

The CVM therefore constrains both **time** and **space**.

##### Time

Convex constrains time by placing a "juice cost" on each CVM operation performed. Any transaction executed has a "juice limit" that places a bound on the total amount of computational work that can be performed within the scope of the transaction.

The originating account for a transaction must reserve juice by paying an amount `[juice limit] x [juice price]` at the start of the transaction. Any unused juice at the end of the transaction is refunded at the same rate. The juice price a dynamically varying price that adjusts with amount of execution performed per unit time by the Convex network as a whole: this is a cryptoeconomic mechanism to disincentivise transactions from being submitted at peak periods, and as a protection from DoS attacks by making it prohibitively expensive to flood the compute capacity of the network for a sustained period of time.

If the juice limit has been exceeded, the CVM terminates transaction execution with an exception, and rolls back any state changes. No juice is refunded in such a situation - this penalises users who attempt excessive resource consumption either carelessly or maliciously.

##### Space

A significant but often overlooked problem facing a global, decentralised database that provides a commitment to preserve data indefinitely is the problem of state growth: if not constrained, the size of the CVM state might grow excessively large.

This is an economic problem: The participants who create additional state are not necessarily the same as those who must bear the cost of ongoing storage. This can create a "Tragedy of the Commons" where participants are careless about creating new state. This could quickly lead to a situation where the state grows too large to be feasible for normal computers to participate as Peers in the Convex networks, which will in turn cause centralisation towards a few large and powerful nodes.

This problem cannot be solved by charging at execution time alone. There is no way to determine at execution time how long a particular set of data will be stored for - it might be deallocated in the very next transaction, or it might need to persist forever. Any "average" estimate will end up penalising those who are being efficient only need the storage briefly, and subsidising those who are careless and consume space forever.

To solve this problem, Convex implements **memory accounting** with the following features:

* Every change to the state tracks the impact on **state size**, measured in bytes, which is (to a close approximation) the amount of memory that would be required to write out the byte encoding of the entire state tree.
* Each account has an allocation of **memory** to utilise. 
* When a transaction is executed, the **change in state size** is computed. An increase in state size reduces the accounts free memory, while a decrease in state size increases the account's free memory.
* If at the end of a transaction the incremental space exceeds free memory then the transaction will fail and be rolled back.
* Accounts may *temporarily* exceed their memory allocation during the course of transaction execution, e.g. through construction of temporary data structures. We can safely allow this because the maximum amount of temporary object allocation is bounded to a constant size by juice limits.

**Note 1**: that in practice, the actual storage size of the CVM state will be significantly smaller than the tracked state size, because the use of immutable persistent data structures allows many equal tree branches to be shared. The effectiveness of this structural sharing needs to be observed over time, but we anticipate perhaps a 2-3x reduction in state size may be possible in the long term.

**Note 2**: Observant system hackers may notice that the memory accounting mechanism means that if Account A causes some memory to be allocated, and Account B causes that same memory to be de-allocated (e.g. through the use of calls to an Actor), then Account B will gain memory from A. We consider this a feature, not a bug: It incentivises participants to clean up state wherever possible, and incentivises the writers of Actor code to consider their memory allocations and deallocations carefully.

To ensure correct economic behaviour, it is necessary for free memory to have an economic cost. Therefore, Convex provides a **memory market** though which memory allocations may be traded. This has the following features:

* An automatic market maker enabling accounts to buy and sell memory at any time, placing an effective price on memory
* A total cap on available memory set at a level that constrains the total state size to an acceptable level
* Ongoing governance to regulate changes in the total cap, which can be adjusted to allow for additional state growth as technology improves average Peer resources, without risking a loss of decentralisation.

The overall cryptoeconomic system of memory accounting and memory markets offers a number of important benefits to the Convex ecosystem:

* A guaranteed cap on state growth, that can safeguard against the growth of storage requirements driving centralisation
* A general incentive for all participants to minimise and manage memory usage. This incentive increases as total state size grows towards the cap.
* A specific incentive for coders to write memory-efficient code, and provide the ability for unused data to be deleted, if they want their Actors to be considered high quality and trustworthy.
* A partial disincentive to hoard memory allocations (since expected future cap additions may devalue large memory holdings).
* When space becomes scarce, there is an incentive for less economically viable applications to wind up operations and sell their freed memory allocation.

For convenience, memory purchases happen automatically if additional allocations are needed within a transaction. This means that in most cases, users need not be concerned with the specifics of managing their memory allowance.

#### Runtime environment

The CVM defines a small runtime system ("core") that provides CVM capabilities to CVM programs. These include:

* Standard language control structures (loops, conditionals, error handling etc.)
* Basic numerical functions (focused on 64-bit integer and IEEE 764 double precision floats)
* Functions to manipulate and manage immutable persistent data structures (vectors, lists, maps sets)
* Control of assets native to the Convex network, such as balances and stake
* Ability to interact with Actors (deploying Actors, calling Actor functions)
* Functionality useful for managing CVM state updates, e.g. transaction rollback
* Language constructs necessary to support Convex Lisp (see below)

The core system is designed so that these low level capabilities can be easily composed to create higher level capabilities, through composition of data and functions.

At the same time, the capabilities of the runtime system are constrained so that they cannot break the rules of CVM execution necessary for deterministic state updates. There is no external IO capability, no ability for non-deterministic behaviour, and no ability to affect CVM state in a way that breaks the security model. CVM code is therefore "sandboxed" from the perspective of the Convex system as a whole.

#### Transparent persistence

The Convex execution engine implements a form of transparent (sometimes also known as orthogonal) persistence. In this model, the CVM state size may exceed the working memory capacity of a Peer, and necessary parts of the state tree are loaded in from persistent storage on demand.

This presents a significant conceptual benefit for the developer: there is no need to write any code to load or unload data from storage in normal CVM code. This imposes some additional implementation complexity for the CVM itself, but this is considered a worthwhile trade-off, especially since it simplifies the logic of other parts of the Convex Peer implementation (e.g. eliminates the need to explicitly handle the memory consumption growth of long block orderings generated by the consensus algorithm over time). 

In the reference implementation, this is achieved with judicious reliance upon the very efficient JVM automatic memory management. This enables the following lifecycle for in-memory data values:

1. Values are initial created with strong (RefDirect) references, which ensure that they are held in memory for as long as they are potentially needed
2. At certain checkpoints (most importantly, after the successful processing of each Block) the current state is *persisted*. All objects which are reachable but not yet persisted are written to storage, and references to them are converted from strong references to soft (RefSoft) references. This happens as an atomic operation.
3. From this point onwards, the persisted objects may be garbage collected at any time by the JVM if memory pressure occurs
4. If an attempt is made to access a value that has been garbage collected, the reference automatically fetches the associated data value from storage. This is guaranteed to succeed assuming that the previous persistence step was successfully completed.
5. Over longer time periods, it is possible to perform garbage collection on the storage itself by compacting the store to remove data that is no longer required by the current consensus state. Peers may choose to do this at their own discretion based on their operational requirements, or alternatively they may decide to preserve all data (for example in order to perform historical analysis)

#### Convex Lisp

The CVM includes a small, dynamically typed, embedded Lisp suitable for general purpose programming within the CVM environment.

Lisp was chosen as the first language implementation in Convex for the following reasons:

* It can be constructed using a very small number of axiomatic primitives, which in turn are based on the Lambda Calculus. This provides a robust logical and mathematical foundation, suitable for the type of verifiable, deterministic computations that the CVM must support.
* Lisp has a very simple regular syntax, homoiconic nature of code and ability to implement powerful macros. We hope this provides the basis for innovative new languages and domain-specific languages (DSLs) on the CVM.
* Lisp compilers are small enough and practical enough to include as a capability within the CVM, avoiding the need for external compilers and tools to generate CVM code.
* It is comparatively simple to implement, reducing the risk of bugs in the CVM implementation (which may require a protocol update to correct).
* Lisp is well suited for interactive usage at a REPL prompt. This facilitates rapid prototyping and development of Actors in a way that we believe is a significant advantage for decentralised application builders looking to test and prototype new ideas.

Developers using the Convex system are not required to use Convex Lisp: It is perfectly possible to create alternative language front-ends that target the CVM (e.g. by constructing trees of Ops directly)

Convex Lisp draws inspiration from Common Lisp, Racket and Clojure. It is designed as primarily a functional language, with fully immutable data structures, as it is our belief that functional programming forms a strong foundation for building robust, secure systems.

#### Environments

A novel feature of the Convex Account model is that each Account receives it's own programmable environment where variables, data structures and code can be dynamically defined and updated. Definitions held within different accounts cannot collide: they have isolated namespaces.

* For User Accounts, this behaves like a small programmable sandbox completely under the control of the user.
* For Actor Accounts, this can be used to store Actor code and state required for the operation of the Actor. The deployment of the Actor is in effect equivalent to creating the Actor Account and initialising the Actor's environment, with subsequent changes to the environment controlled by a set of exported functions that can be externally called.

We believe this is an useful model to encourage rapid development and innovation: for example, a developer can easily experiment with code in their own user account, then capture the same code in the definition of a deployable Actor for production usage.

#### Scheduled execution

The CVM includes a specialised data structure called the Schedule that references CVM code to be executed under a specific Account at a defined future timestamp.

The main purpose of the Schedule is to allow Actors to implement autonomous behaviour without the need to wait for an external transaction to trigger execution. This could be used, for example, to finalise a decentralised auction, to distribute the prize from a random lottery, to trade on a periodic basis, or to unlock assets that have been frozen for a specified period of time. 

The schedule is **unstoppable**, in the sense that once the consensus timestamp advances based a scheduled execution time, the associated code is automatically executed. This execution is guaranteed to happen before any other transactions in a block submitted on or after the same timestamp.

Scheduled executions currently cannot be cancelled, but this is not considered to be a serious limitation: Actors can simply implement code to ignore the event if it is no longer relevant.

#### Garbage collection

The CVM automatically garbage collects objects to which references are no longer maintained. The choice of including garbage collection in the CVM is motivated by the following factors:

* **Convenience** - developers need not be concerned with manual memory management. In general, this is a significant productivity gain.
* **Performance** Given a focus on immutable data, garbage collection offers significant performance advantages because references can be shared internally within the CVM implementation, as opposed to relying on expensive "copy on write" approaches.
* **Security** - Mistakes in memory management are one of the most common types of defect, often resulting in significant security issues (e.g. "buffer overruns"). Such risks are generally unacceptable for smart contract code securing significant digital assets.

Short lived objects are garbage collected by the host runtime (the JVM). This will happen for most temporary objects created during the execution of CVM code. For those data values that are persisted to long term storage (e.g. because they become part of the updated CVM state), the host runtime may garbage-collect the in-memory copy.

Peer operators may also choose to either garbage collect old persisted state from long term storage, or alternatively maintain old state for historical analysis. Peers are only required to maintain object information necessary to execute the consensus algorithm (belief structures plus the proportion of CVM state relating to Peer information and stakes). For more details, see the section on Convergent Immutable Storage.


### Storage System

Convex makes use of a specialised storage system that complements the design of the Execution Engine. This provides significant performance advantages, since the format of data in storage aligns directly to the usage patterns and data structures utilised in the CVM.

The storage system is also used to facilitate serialisation and transport of data across the network in communication between peers and clients.

#### Cells

Storage is constructed out of Cells. 

In most cases, a Cell is an immutable value value that represents an object in the CVM Informational Model.

Normally there is a 1-1 mapping between Cells and CVM DDVs, however there are some exceptions:

- For larger data structures a tree of Cells may be necessary - this is because we need to place a fixed upper bound on the size of each cell.
- Small data values do not require a cell in their own right, since it is more efficient to embed them directly within a larger Cell.
- Some special data structures used in Convex are technically implemented as Cells for the purpose of storage and serialisation, but are unavailable for use within the CVM since the are used externally to the CVM State - for example the `Belief` data structure used in the CPoS consensus algorithm.


#### Encoding

All Cells have a unique Encoding.

The encoding is designed to provide the following properties:

* A bounded maximum encoding size for any Cell (currently 8191 bytes)
* Very fast serialisation and deserialisation, with minimal requirements for temporary object allocation.
* Uniqueness of encoding - there is a 1:1 mapping between cell values and valid encodings. This means, among other useful properties, that Data Value equality can be determined by comparing hashes of encodings.
* Self describing format: given a valid Cell encoding, the Data Value can be reconstructed without additional context

The same encoding is utilised in both durable storage and in network transmission.

#### Embedding

Small Data Values are Embedded within another Cell (e.g. a Cell representing part of a larger data structure). In most cases, this avoids the need to construct and store separate cells for small primitive values, and often small data structures themselves can be fully embedded.

Currently, most data values with an encoding size of up to 140 bytes are automatically embedded. This heuristic may be modified based on further testing and profiling. 

#### Value IDs as storage keys

The cryptographic hash of the Cell encoding is used an an identifier to refer to a Cell, and in particular as a key for addressing data in the storage system. 

This has the important property that it requires all values in the storage system *immutable* - the data value cannot change for a given key, or else the ID will no longer be valid. This restriction may seem limiting at first, but in fact provides significant advantages for the Convex storage implementation:

* No need to re-size values once written: the database can be accumulated in an "append-only" manner. The prevents storage fragmentation.
* No need for cache invalidation or synchronisation of replicas: values cannot change
* Rapid verification: if a hash exists in the store, and the data has already been validated, it must still be valid.

#### Convergence

Given the above design features, we are able to implement a system of immutable storage that is Convergent: Additional storage information may be merged into the store in a manner analogous to a CRDT.

It is a well-known result that taking the union of sets in a purely additive manner (a Grow-only Set) is a valid CRDT. The storage system can be regarded as a growing set of immutable (key, value) pairs, and hence satisfies the CRDT property.

This convergence property is particularly beneficial when combined with the structured of Merkle trees used throughout the CVM: data structures with identical branches are automatically de-duplicated when they are stored, since the existing storage entry can simply be re-used. If effect, the Merkle trees become Merkle DAGs with guaranteed sharing of identical children.

#### Status tagging

In additional to the raw data values, the storage system implements a system of status tagging, used to indicate the level to which a data value has been verified by the Peer.

The basic status levels are:

* **UNKNOWN** - The Peer has an identifier (Hash), but does not know yet if this is consistent with any encoded data
* **STORED** - The Peer has encoded data in storage which is validated to be a well-formed Cell (ignoring children), and consistent with the Hash.
* **PERSISTED** - The Peer has validated the structure of the Cell completely, including recursively validating all its children. At this point, we can rely on a Data Value represented by the Cell to be usable in CVM execution
* **ANNOUNCED** - The Peer has included the data in a publicly broadcast Belief

Some special status levels are also possible, including:

* **EMBEDDED** - A Data Value is able to be embedded within other Cells, and does *not* need to be individually stored.
* **INVALID** - A cell has been proven to be inconsistent with some validation rules. Such values cannot be used in the CVM, but caching the invalid status can be helpful to avoid the need to redo the validation.

This status tagging is compatible with being included in the storage CRDT, since:
* The status level can never go backwards: once verified, the result stands forever.
* Where there are two possible outcomes (valid or invalid, embedded or non-embedded) all Peers that perform correct validation must agree (i.e. it is effectively monotonic for any given data value)


#### Novelty detection

A key feature of the storage system is the ability to detect and apply special handling to novelty. Novelty is defined as a stored value that is moving up to a higher status level for the first time.

Novelty detection is important for the following reasons:

* When information needs to be shared on the network, only the incremental information needs to be transmitted. This is especially important for the consensus algorithm, for example: the transmission of a new Belief need only include the additions to the proposed Ordering, without communicating the complete Ordering (which may be very long, but is already likely to held by all other Peers)
* When validating data, it avoids the need to re-compute validation on parts of the data that have already been validated. This is particularly important when the data structures to be validated are large, but have only a few small changes in comparison with a previously validated data structure (e.g. the entire CVM State)

#### Garbage Collection

If we had infinite storage, we could just keep accumulating values in the database forever. However, practical concerns of storage limits will probably make this infeasible for most Peers.

Peers are only strictly required to maintain:

* Enough information regarding Beliefs to participate in the consensus algorithm (about one day of orderings and transactions - exact limit TBC)
* The current State for the CVM

The storage system therefore allows garbage collection to be performed on a periodic basis, so that storage space containing data that is no longer required can be reclaimed. Garbage collection is done on a mark+copy basis, where currently used storage is copied to a new data file, and after which the old data file can be safely discarded. This could theoretically be performed concurrently with ongoing Peer operation in a future version.

This behaviour is of course configurable by Peer Operators - we expect some will want to maintain and index all historical data for analytical purposes, or in order to provide their clients with additional historical query capabilities.

#### Memory Mapped Implementation

The Convex reference implementation implements the storage system using a specialised memory-mapped database called Etch, which is heavily optimised for the storage of Cells as described here. Assuming sufficient available RAM on a Peer, Etch effectively operates as an in-memory database.

In performance tests, we have observed tens of millions of reads and writes per second. 

### Cryptographic Primitives

Convex uses cryptographic primitives for the following functions:

* Digital Signature (Ed25519)
  * For every Transaction submitted by a Client
  * For every Block proposed by a Peer for consensus
  * For every Ordering constructed and shared by a Peer
  * For every Belief shared by a Peer on the gossip network
* Cryptographic Hashes (SHA3-256)
  * For every Cell which forms part of a Decentralised Data Value, a hash of its byte encoding is computed for storage, identity, indexing and verification purposes. This is effectively equal to the VID.
  * For every key value used in a map data structure, its hash is computed (if necessary)
* Standard approaches used to store and protect keys in common key file formats (e.g. .pem, .pfx)

As an engineering principle, Convex only uses trusted implementations of cryptographic algorithms in well tested libraries (currently Bouncy Castle, and the cryptographic routines available as standard in the JVM). There is no need to "roll our own" with respect to fundamental crypto algorithms.

## Conclusion

Convex presents a new approach to programmable economic systems that provides a powerful combination of scalability, security and decentralisation, suitable for building applications for the Internet of Value.

At the same time, it maintains a certain degree of simplicity. We believe that simple, composable systems offer a more stable and secure foundation to build upon, and Convex therefore features:

* Functional programming on the CVM based on the lambda calculus
* Immutable values for all data structures
* A surprisingly simple consensus algorithm based on CRDTs

It is our hope that the innovations in Convex and the engineering decisions made will provide a practical, high performance platform for a new generation of decentralised applications.

## Contact and Links

To learn more and experiment with the live Convex test network: [Convex World](https://convex.world)

For discussion of this White Paper and other topics relating to Convex, you are very welcome to join our public [Discord Server](https://discord.gg/5j2mPsk).

Email: mike@convex.world

