# Concepts

## Decentralisation

Convex enables the construction of decentralised applications. 

By **decentralised** we mean that control and decision-making rights and capabilities are distributed away from centralised points of control. In practice this means:

- You are in control of your own data. No other party can modify or delete your data.
- You can rely on trusted, digital assets that cannot be confiscated by any 3rd party.
- There is no need for trusted intermediaries - you can transact directly with anyone on the network
- Your transactions cannot be blocked or censored

Perhaps nothing in life in guaranteed. There are two important provisos:

1. You need to maintain security of your *private key* - if someone else is able to obtain your private key, they can sign transactions that impersonate you or steal your assets. This is a real risk, and you are ultimately responsible for the security of your private keys, but assuming you take good precautions (e.g. keeping valuable keys offline / in a hardware wallet) you can feel pretty safe.
2. The *integrity of the network* needs to be maintained. Fortunately, the operation of the network is itself decentralised through the consensus algorithm. And the Convex Foundation is dedicated to protecting and upholding the integrity of the network as new updates are made.




## Smart Contracts

A Smart Contract is a form of contract that is automatically executed according to some pre-defined terms and conditions.

### Use cases

The possible uses of smart contracts are unlimited. Here are some ideas:

- **Auctions** - You could create a smart contract to run a public auction for a valuable item. The person who makes the highest bids are guaranteed to win the ownership rights of the item.
- **Options** - You can create an option contract which gives the owner a right to purchase some other asset for a specified strike price for a certain time period. If the option owner exercises the option, they must pay the strike price but get ownership of the asset in return. If the time limit expires, the option owner need not pay anything but the asset reverts back to the original owner.
- **Utility Tokens** - You can create transferable tokens that provide some usage right to a service when they are redeemed. Owners of these tokens can either use them to purchase services themselves, or keep the tokens and sell them to others that may wish to purchase these services in the future.
- **Insurance** - You can create a smart contract that pays out a significant coin value in the event that some specified adverse event occurs. People wishing to insure against the adverse event can buy insurance using the smart contract, presumably in exchange for some fixed premium.
- **Voting Rights** - You can create smart contracts that collect votes from a certain set of participants, and use the votes to determine the outcome of some decision. Such contracts could be used to govern virtual companies, or nominate individuals to real world positions of power.

### How it works
For smart contracts to be practical, we need:

- A way of specifying the terms of the contract
- A way for parties to enter into an agreement
- A mechanism for executing the contract
- A guarantee that the contract will be successfully executed

Convex is designed to provide all four attributes:

- The terms of contracts are expressed in code which can be deployed to create **Actors** on the Convex Network. Actors are autonomous programs that specify contract rules, and cryptographic techniques are used to ensure the integrity of this code.
- Parties enter into agreements by submitting **Signed Transactions** that allow them to make commitments to enter the contract (typically, this involved placing some valuable e.g. coins or tokens under the control of an Actor). The use of digital signatures is accepted as a proof that a party wishes to participate in some specified smart contract.
- The contract is executed via running Actor code on the **Convex Virtual Machine (CVM)**, which determines precisely how the smart contract execution will be carried out. This might involve distributing ownership of digital assets back to participants subject to certain conditions.
- The **Consensus Algorithm** on the Convex Network guarantees that all execution is ultimately carried out successfully according to contract definitions, and the results made available to all participants.

## Staking

TODO

## 

