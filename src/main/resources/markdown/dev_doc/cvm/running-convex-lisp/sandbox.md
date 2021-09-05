The [sandbox](/sandbox) is the easiest way to run transactions against the current test network and learn Convex Lisp. It is an interactive tool
used for submitting transactions under a test account.

![Sandbox](/images/sandbox.png)


## Creating an account

Any transaction submitted on the network must be [signed](/glossary?section=Digital%20Signature) by an [account](/glossary?section=Account). Hence the very first step is to create one and the sandbox will invite you to do so by merely pressing a button.
Accounts represent digital identities on the current test network. They are free and disposable, but also volatile since the test network is unstable and meant to be used only for learning and prototyping purposes. Users
must expect test accounts to be deleted periodically, after an important update in the system for instance.

New accounts have a sufficient amount of Convex Coins for carrying out many operations.


## Running transactions

The bottom area is where the transaction code is entered. All results are displayed in the upper area. To test it out, try entering the following piece of code:

```clojure
(+ 2 2)
```


## Modes

There are two execution modes for the sandbox:

- **Query**: code is executed, a result is returned but any change in the network is discarded; it if as if nothing happened; it does not incur fees in Convex Coins
- **Transaction** (default): code is executed, a result will be returned; it incurs fees in Convex Coins

Hence, queries are useful when reading information without making any change, while actual transactions must be used for potential changes in the network's state to take effect. Most of the time, when learning or prototyping in Convex, users
should not have to worry about those modes.
