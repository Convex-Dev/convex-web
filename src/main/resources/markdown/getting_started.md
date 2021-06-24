This page provides a quick guided tour of Convex. We'll take you through everything from creating your first Account to launching your very own digital token!

You don't need any particular programming language experience to follow this guide, but if you want to follow using the Sandbox you will need to *precisely* enter some commands in Convex Lisp. If you want to learn more about Convex Lisp as a programming language, check out the [Convex Lisp Tutorial](https://convex.world/#/documentation/convex-lisp).

## Making an Account

To get started, you'll need to create an **Account**, if you don't have one already. Press `Create Account` to get a new free account. An account is your identity on the Convex system, and is named using a numeric **Address** which is looks something like this:

```
#123
```

Conventionally, we display Addresses with a `#` to distinguish them from other numbers.

Accounts are free and disposable on the Test Network. Make as many as you like. Just be aware that we will periodically delete all accounts as we roll out and test upgrades to the system over the next few months - so don't get too attached to it!

## Coins

Every Account has a **Balance** denominated in Convex Coins. Coins can be used in various ways:

- As a virtual currency to exchange for digital assets and services
- To pay a small transaction fee when actions are performed on the Convex Network
- To participate in running the network, e.g. staking on a Peer

Your coin balance *cannot be spent* by anyone who doesn't have access to your Account. The Convex system keeps it safe through extremely strong cryptographic protections that make it impossible to access the account without an Ed25519 digital signature, signed using your private key. 

You can have multiple Accounts: the collection of accounts that you use is a **Wallet**. In the current Convex test network, the `convex.world` server manages the wallet's keys for your for convenience.

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

The basic rule is to use queries when you only want to read information from Convex and make no changes. For all other actions, you will want to use transactions so that your changes will take effect.

### Language Selection

You can also currently choose two different languages for entering commands:

- **Convex Lisp (default)** : A variant of the Lisp language
- **Convex Scrypt** : a simple scripting language based on JavaScript syntax

For this guide, we will use Convex Lisp. For more details on how to use Scrypt, see **TODO Add Link**. In the future, Convex may support a wider range of languages on the CVM.


## Moving Funds

As a medium of exchange, it should be very easy (but also secure!) to move funds around between your accounts (or also to your friends or business partners).

To test this out we will need **two Accounts**. You can either make a second account yourself (it's easy to switch between them with the widget at the top of the `convex.world` web application), or get a friend to also create an account at the same time (more fun!).

To transfer funds, we use the `transfer` function, which we supply with the Address of the account we want to transfer to, and the amount we want to transfer.

```clojure
(transfer 0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C 10000)
=> 10000
```

Make sure you get the destination Address correct! If you make a transfer to an Account that you don't have access to or use the wrong recipient Address, then you will probably lose your coins.

Check that funds have gone out of your Account, and into the destination Account. **TOP TIP** - if it looks like the coins haven't been moved, make sure you are in "Transaction" mode rather than "Query" mode.

If you try to transfer a particularly large amount, you will get an error saying that you don't have sufficient funds:

```clojure
(transfer 0x8506cc53f9b7dD152C9BB5386d50C360ff85EFD043049aea55B44362D92C0E1C 999999999999999999)
=> ERROR(FUNDS): Insufficient funds in account
```

**NOTE** Error in transactions doesn't really do any harm. When they occur, everything in the transaction is "rolled back" to the state it was at the start of the transaction, e.g. any coins you transferred early in the transaction (before the error) will be put back in your Account. All you will lose is a small amount of transaction fees.

## The Environment

As well as a coin balance, every Account has its own **Environment**. This is a magical space that the Account controls that can be used to store information. It's best to illustrate this with an example:

```clojure
(def favorite-number 101)
=> 101
```

The `def` command creates a **Definition** in the environment names with the Symbol `favorite-number`. Once created, this definition stays in the Environment forever (unless you remove it or change it). 

Once you have a value defined in the environment, you can use it freely in future commands. For example, if you just want to see what value the definition has you can just type in the symbol itself:

```clojure
favorite-number
=> 101
```

Or if you want to use your defined values in other calculations or commands:

```clojure
(* favorite-number 1000)
=> 101000
```

If you want to delete a definition, you can use `undef`

```clojure
(undef favorite-number)
=> nil
```

Deleting definitions once you no longer need them is a good idea to keep your Environment tidy. It can also get earn you refunds for releasing memory!

The Environment is **secure** in the sense that only your Account can **change** your definitions. However, it is technically possible to **observe** definitions in the Environment of anyone else's Account. So you should not use the Environment for any information you want to keep private.


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

Using the application is easy

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

This is a powerful solution if you want to build apps where you have exclusive control. However, it presents a problem if we want to build apps to support digital assets more generally - we would have to trust the Account owner to run the app honestly without making modifications (e.g. if you stored balances for a digital token in the Account of User A, then User A would be able to unilaterally transfer tokens to himself by modifying the token balances....). This might work for tracking digital assets among a small group of friends or collaborating companies who trust each other, but it would not scale to a digital economy with millions of participants, of which many could be unknown and/or untrusted.

Hence we need **Actors**. Actors are Accounts that are **independent of any User** and operate autonomously on the Convex Network. They can be used as trusted parties to manage digital assets and enforce Smart Contracts.

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
(def actor (deploy nil))
=> #1234
```

This Actor does nothing. It contains no useful information. It has no exported functions, and therefore can never do anything. This is a bit useless, but reassuring! Security is all about the confidence of knowing what harmful things *can't* be done.

Although not useful, this empty Actor still exists on the Convex Network. You can confirm it is an Actor, and inspect its balance for example:

```clojure
(actor? #1234)
=> true

(balance #1234)
=> 0
```

### Exported functions

To interact with an Actor, users can `call` special functions which are exported by the Actor. Exported functions are like regular functions, except that they execute in the security context of the Actor itself rather than the user that called them.

A simple example is an Actor that simply counts how many times an `increment` function is called:

```clojure
(def actor (deploy '(do
   ;; A counter in the Actor's environment
   (def counter 0) 
   
   ;; A callable function that increments the counter
   (defn increment []
     (def counter (inc counter)))
     
   ;; A callable function to get the current value of the counter
   (defn get []
     counter)  

   (export increment get))))
```

You can now `call` the actor to get and increment the counter freely:

```clojure
;; check the initial counter value
(call actor (get))
;;=> 0

;; increment the counter
(call actor (increment))

;; check the new counter value
(call actor (get))
;;=> 1
```

### Actor security

**Important security point:** it is critical to remember that exported functions can be called by any Account at any time after the Actor is deployed. The simple Actor above can therefore be called by any other user (or another Actor, for that matter) and have the effect of incrementing the counter. Sometimes this is perfectly safe (e.g. for read-only functions like `get`) and sometimes it is what you want (anyone can access) but if the function provides any control over valuable assets you will definitely want to enforce controls on who can execute certain code.

There are many ways to add sophisticated access controls to Actor functionality in Convex. You might want to consider:

- An "owner" who can perform special administrative actions (e.g. upgrading the Actor with new code)
- A trusted "whitelist" of Users who are allowed to call certain functionality
- Controls over how much a specific user can do (e.g. a digital token asset should not normally allow a User to transfer tokens that they do not own)

Advanced trust issues are beyond the scope of this guide, but if you are interested in the details check out the advanced topics in the documentation.

## Creating a Token

Convex is all about enabling the Internet of Value, and empowering people to create their own digital assets. So we will finish up this tutorial with a quick guide on how to create your very own token!

### Importing the Fungible library

It's possible to build your own Actor to implement a Token from scratch. However - unless you are very experienced this can be a complex task and there are significant security risks involved. That's why we've created the `convex.fungible` library that makes it easy to create secure tokens without any complex coding.

To use the Fungible library, you just need to import it in your Account REPL:

```clojure
(import convex.fungible :as fungible)
```

The name after the `:as` is an *alias*. This can be any name you like, but we recommend using `fungible` so that it is very clear what library code you are using.

### Deploying a Token

You can deploy a new Fungible token in one line!

```clojure
(def my-token (deploy (fungible/build-token {:supply 1000})))
```

This will create and deploy a new Actor which represents your token, and `my-token` is set to hold the Address of the new Actor.

The `:supply` configuration parameter determines how many tokens will exist. Initially, the Account that deployed the token (i.e. your User Account) will control all the tokens.

### Using tokens

You can check your token balance easily using another function in the Fungible library:

```clojure
(fungible/balance my-token *address*)
=> 1000
```

As expected, we are initially in possession of all 1000 tokens. Instead of using your current Address (`*address*`) you can equivalently check the token balance of any other Address, which by default will be 0.

```clojure
(fungible/balance my-token #666)
=> 0
```

If you want to transfer 100 Tokens to another Account:

```clojure
(fungible/transfer my-token #666 100)
```

Now you should be able to observe that your own token balance has been reduced:

```clojure
(fungible/balance my-token *address*)
=> 900
```

And you can also see that the recipient is not the proud owner of 100 tokens:

```clojure
(fungible/balance my-token #666)
=> 100
```

## Summary and next steps

If you made it this far, congratulations! You've covered the basic of the Convex system and launched your very own digital asset!

Where you go from here is up to you. Some ideas:

- Learn more about Convex from the other documentation and guides here at `convex.world`
- Build a mobile dApp that talks to your Convex Actors with the Client API

We're excited to see what people can build using Convex! But most importantly, please let us know your feedback and what you think of Convex so far. You can join the [Convex Discord Server](https://discord.gg/fsnCxEM) to get involved with the discussions:

- [Convex Discord](https://discord.gg/fsnCxEM) 

And also please check out the Convex-Dev discord if you would like to get involved with the open-source development and bounty program!

- [Convex-Dev GitHub](https://github.com/orgs/Convex-Dev)

Happy Building!