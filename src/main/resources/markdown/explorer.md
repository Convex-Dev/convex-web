# Convex Explorer 

The Convex Explorer contains utilities to help you examine and analyse the state of the Convex network. We need utilities to help us do this because Convex is has at it's heart a large decentralised database which supports complex data structures that would be extremely hard to inspect manually!

The key utilities implemented so far are listed below:

## Accounts

The Accounts utility lets you browse and examine all Accounts that exist in the network. Accounts can be either User Accounts or autonomous Actors.

Useful information that you may wish to examine for each Account includes:

- The current coin balance of the Account
- The available Memory Allowance
- The symbols that are defined in the Account's environment

## Blocks

The Blocks utility lets you inspect all blocks that have been accepted into the official network consensus, right back to the initialisation of the network.

## Transactions

The Transactions utility lets you examine all the transactions that have been executed - which in turn must have been included in a Block.

You can see all information relevant to a transaction, including:

- Block number and transaction number at which the transaction was submitted 
- Sequence Number (used to give each transaction a unique id and prevent replay attacks)
- Type of transaction (Invoke, Transfer etc.)
- Timestamp at which the transaction was included
- Address of the signer
- Any value associated (e.g. what code was executed in an Invoke transaction)
