This document gives you a quick guided tour of Convex. We'll take you through everything from creating your first Account to launching your very own digital token!

You don't need any particular programming langauge experience to follow this guide, but if you want to follow using the Sandbox you will need to *precisely* enter some commands in Convex Lisp. If you want to know more about Convex Lisp as a programming language, check out the [Convex Lisp Tutorial](https://convex.world/#/documentation/convex-lisp).

## Making an Account

To get started, you'll need to create an **Account**, if you don't have one already. Press `Create Account` to get a new free account. An account is your identity on the Convex system, and is named using an **Address** which is a 64-character hexadecimal string that looks something like this:

```
0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C
```

Accounts are free and disposable on the test system. Make as many as you like. Just be aware that we will periodically delete all accounts as we roll out and test upgrades to the system over the next few months - so don't get too attached to it!

## Coins

Every Account has a **Balance** denominated in Convex Coins. Coins can be used in various ways:

- To pay a small transaction fee when actions are performed on the Convex Network
- To participate in running the network, e.g. staking on a Peer
- As a virtual currency to exchange for digital assets and services

Your coin balance *cannot be spent* by anyone who doesn't have access to your Account. The Convex system keeps it safe through extremely strong cryptographic protections that make it impossible to access the account without a Ed25519 digital signature, signed using your private key. 

You can have multiple Accounts: the collection of accounts that you use is a **Wallet**. In the current Convex test network, the `convex.world` server looks after your wallet for you for convenience.

Every new account on the network gets an initial balance `100,000,000` (100 silver coins) which is more than enough to try out everything in all the tutorials! But if you need more you can top up with the `Faucet` tool.

## Enter the Sandbox

The sandbox is an interactive tool that lets you test and explore all the features of the Convex Network. Click on `Sandbox` on the left navigation bar and you should see a screen like this:

```
TODO: Picture
```

### Running Commands

The box at the bottom centre is where you can enter commands to the convex system. The large box in the centre is where the results are displayed after each command. To test it out, try entering the following command:

```
*address*
=> 0xC62f714fBCf8EcdF235aC6FB9da250343e566b669E622c85A8d755Dd162f764b
```

Run the command, and you should see your current Address returned. The results display also shows some helpful information about the values returned, such as the account balance if an Address refers to an Account.

### Sandbox modes

There are two modes for the Sandbox:

- **Transaction** : the command will be executed, a result will be returned and you will pay any transaction costs required. This will update the state of the Convex Network.
- **Query** : the command will be executed, a result will be returned by the Convex Network state will be left unchanged. Queries are not transmitted to the network and do not cost any coins.

The basic rule is to use queries when you only want to read information from Convex and make no changes. For all other actions, you will want to use transactions so that your changes will have effect.

### Language Selection

You can also currently choose two different languages for entering commands:

- **Convex Lisp (default)** : A variant of the Lisp language
- **Convex Scrypt** : a simple scripting language based on JavaScript syntax

For this guide, we will use Convex Lisp. For more details on how to use Scrypt, see **TODO Add Link**. In the future, Convex is likely to support a wide range of languages.


## Moving Funds

As a medium of exchange, it is important that it is easy (but also secure!) to move funds around between your accounts (or also to your friends or business partners).

To test this out we will need **two Accounts**. You can either make a second account yourself (it's easy to switch between them with the widget at the top of the `convex.world` web application), or get a friend to also create an account at the same time (more fun!).

To transfer funds, we use the `transfer` function, which we supply with the Address of the account we want to transfer to, and the amount of coins we want to transfer.

```clojure
(transfer 0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C 10000)
=> 10000
```

Make sure you get the destination Address correct! If you make a transfer to an Account that you don't have access to or use the wrong recipient Address, then you will probably lose your coins....

Check that funds have gone out of your Account, and into the destination Account. **TOP TIP** - if it looks like the coins haven't been moved, make sure you are in "Transaction" mode rather than "Query" mode.

If you try to transfer a particularly large amount, you will get an error saying that you don't have sufficient funds:

```clojure
(transfer 0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C 999999999999999999)
=> ERROR(FUNDS): Insufficient funds in account
```

**NOTE** Error in transactions don't really do any harm. When they occur, everything in the transaction is "rolled back" to the state it was at the start of the transaction, e.g. any coins you transferred early in the transaction (before the error) will be put back in your Account. All you will lose is a small amount of transaction fees.

## The Environment

As well as a coin balance, every Account has its own **Enviornment**. This is a magical space that the Account controls that can be used to store information. It's best to illustrate whis with an example:

```clojure
(def favourite-number 101)
=> 101
```

The `def` command creates a **Definition** in the environment names with the Symbol `favourite-number`. Once created, this definition stays in the Environment forever (unless you remove it or change it). 

Once you have a value defined in the environment, you can use it freely in future commands. For example, if you just want to see what value the definition has you can just type in the symbol itself:

```clojure
favourite-number
=> 101
```

Or if you want to use your defined values in other calculations or commands:

```clojure
(* favourite-number 1000)
=> 101000
```

If you want to delete a definition, you can use `undef`

```clojure
(undef favourite-number)
=> nil
```

Deleting definitions once you no longer need them is a good idea to keep your Environment tidy. It can also get earn you refunds for releasing memory!

The Environment is **secure** in the sense that only your Account can **change** your definitions. However it is technically possible to **observe** definitions in the Environment of anyone else's Account. So you should not use the Environment for any information you want to keep private.


## Building simple applications

**NOTE** If you aren't interested in programming and just want to make magic happen, you can skip this section!

The Environment is your own private sandbox: you can use it to store useful information for later, create Convex Lisp code and functions for future use, even build small applications and games! Convex Lisp is a fully-featured, Turing complete programming language for the Convex Virtual Machine.

Here's a simple TODO list application:

```clojure
;; start with an empty list of TODOs
(def todos [])

;; Create a function that adds a TODO by re-defining the TODO list
(defn add-todo [thing] 
  (def todos (conj todos thing))
  :OK)
```

Using the application is easyL

```
;; Check current TODOs
todos
=> []

;; Add a TODO
(add-todo "Wash the car")
=> :OK

;; Check current TODOs
todos
=> ["Wash the car"]
```

Some points to note:

- This is a Lisp, so all function applications are surrounded in parentheses e.g. `(add-todo "Wash the car")`
- `defn` is a shortcut macro that lets you define a function. Function parameters are specified in square brackets like `[thing]`
- It's totally fine to re-define values in the Environment. We use this trick in the `add-todo` function to update the TODO list.
- You can use `;;` to indicate comments

## Actors on the Stage

So far we've looked mainly at Accounts with coin balances and secure Environments controlled by each User on the Convex system. 

This is a powerful solution if you want to build apps where you have exclusive control, however it presents a problem if we want to build apps to support digital assets more generally - we would have to trust the Account owner to run the app honestly without making modifications (e.g. if you stored balances for a digital token in the Account of User A, then User A would be able to unilaterally transfer tokens to himself by modifying the token balances....). This might work for tracking digital assets among a small group or friends or collaborating companies who trust each other, but would not scale to a digital economy with millions of participants, of which many could be unknown and/or untrusted.

Hence we need **Actors**. Actors are Accounts that are **independent of any User** and operate autonomously on the Convex Network. They can be used a trusted parties to manage digital assets and enforce Smart Contracts.

Actors have many similarities with User Accounts:

- They are identified with an Address
- They have a coin balance and can receive / transfer coins to other Accounts
- They have an Environment, which can contain arbitrary definitions
- They are secure: no other Account can make changes to them (with the one exception of `call` covered below)

The main difference between Actors and User Accounts is that nobody can submit a transaction for the Actor to execute. Instead, you must **call** a function that is **exported** by the Actor. The **only** want to get an Actor to do anything is via calling its exported functions, so by defining the right exported functions you can build an Actor to implement **exactly** the functionality that you want other to be able to access. No more, no less.

### An Empty Actor

The simplest Actor you can build is an empty Actor:

```clojure
;; Deploy an empty Actor, with code defined by 'nil'
(def actor (deploy-once nil))
=> 0x5d53469f20Fef4f8EAB52B88044EDE69C77A6A68a60728609Fc4a65Ff531E7D0
```

This Actor does nothing. It contains no useful information. It has no exported functions, and therefore can never do anything. This is useless, but reassuring! Security is all about the confidence of knowing what harmful things *can't* be be done.

Although not useful, this empty Actor still exists on the Convex Network. You can confirm it is an Actor, and inspect its balance for example:

```clojure
(actor? 0x5d53469f20Fef4f8EAB52B88044EDE69C77A6A68a60728609Fc4a65Ff531E7D0)
=> true

(balance 0x5d53469f20Fef4f8EAB52B88044EDE69C77A6A68a60728609Fc4a65Ff531E7D0)
=> 0
```

### A public 

