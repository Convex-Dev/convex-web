On the Convex network, a smart contract is nothing more than a [function](/cvm/functions) with special metadata attached.

However, calling a contract has a very different effect from applying a regular function. A contract is always executed in the context of the
account where it is defined. It cannot have any effect on the caller account, it can simply return a value. As soon as a value is returned, control
is given back to the caller account.

For terminology, **caller** is the account calling a contract whereas **callee** is the account hosting the contract.

Overall, it is as if during a contract call, the transaction was being executed by the callee. As a consequence, any [definition](/cvm/definitions)
occuring during that call are relative to the environment of the callee only. In other words, accounts provide access control to managing state.


## Addresses and relationship

Three special symbols can be accessed to gain insight as to who does what.

Address of the currently executing account:

```clojure
*address*
```

Address of the account calling a contract (`nil` if no contract is being called):

```clojure
*caller*
```

Address of the orginal account who signed the whole transaction:

```clojure
*origin*
```


## Defining and calling a contract

Creating new accounts which host smart contracts is explained in details in the [section about actors](/cvm/actors). For
the time being, let us suppose:

```clojure
;;;;; ACCOUNT #100 (caller)

*address*  ;; #100
*caller*   ;; nil
*origin*   ;; #100



;;;;; ACCOUNT #42 (callee)

;; Very simple contract which defines a value under `x`.
;; Returns a map with addresses described above.
;;
;; `:callable?` metadata is set to true to indicate this function is indeed a contract.

(defn set-x

  ^{:callable? true}

  [new-x]

  (def x
       new-x)
  {:address *address*
   :caller  *caller*
   :origin  *origin*})



;;;;; ACCOUNT #100 (caller)

;; Calling the contract.

(call #42
      (set-x :my-value))

;; {:address #42
;;  :caller  #100
;;  :origin  #100}
;;
;; During the call, when executing `set-x` as a contract, executing account became #42 and *caller* was set to #100.

*address*

;; #100
;;
;; Now it is back to #100.

*caller*

;; nil

x

;; Error! Not defined in #100.

#42/x

;; :my-value
;;
;; Perfect, `x` was indeed defined in the callee account.
```

Contracts can themselves call other contracts. User must always know what a contract is supposed to do, especially if that contract manages
digital assets.


## Access control

**Attention.** Previous example presents a major flaw: anyone can call the `set-x` contract and change the value of `x` in `#42`.

In the vast majority of cases, access must be constrained. While more sophisticated examples are described in the [section about actors](/cvm/actors),
it typically boils down to checking if `*caller*` is an trusted account. Naturally, other conditions can be enforced as well.

```clojure
(defn set-x

  ^{:callable? true}

  [new-x]

  (when-not (= *caller*
               #100)
    (fail :TRUST
          "Unauthorized."))
  (when-not (keyword? new-x)
    (fail :ARGUMENT
          "`x` must be a keyword."))
  (def x
       new-x))


;; 1) Ensures only account #100 can (re)define `x` in callee account.
;; 2) Ensures argument is a keyword.
;; 3) Okay, all is fine, `x` can be defined with the given argument.
```

**Attention.** Checking `*caller*` is usually a lot safer than checking `*origin*`. Indeed, contracts can call other contracts. Let us suppose this chain of contracts:

```
A -> B -> C
```

Let us also suppose that `C` checks by `*origin*` and allows `A` only. In that configuration, `B` can successfully call `C`, because `*origin*` is `A`.. This behavior
is sometimes desirable but most likely, the intention when writing `C` was to strictly restrict access to `A`. If `B` happens to be a rogue contract (by mistake or by
carelessness), it could have dire consequences.
