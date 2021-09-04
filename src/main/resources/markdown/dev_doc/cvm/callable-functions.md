[Functions](/cvm/functions) have been encountered many times before and *applying a function* meant executing it by providing optional
parameters. Applying a function is always executed in the account which signed the transaction. This section introduces the concept
of a **callable function**, a crucial notion for understanding smart contracts.

For terminology, **caller** is the account calling a callable function whereas **callee** is the account hosting it in its environment.

Executing a callable function temporarily switches the context from the caller to the callee which is typically an [actor](/cvm/actor).
It is as if for the duration of the call, the callee was doing its own transaction within your transaction. While abstract at first,
this provides the very basic building blocks of smart contracts since a callee, acting under its own account, can define and modify
values in its own environment.

As such, an account can host some data needed for the logic of a smart contract, while proposing callable functions which alter this
data under specified conditions. There is a direct parallel between this principle and typical contracts where humans would manage
some assets and enforce rules around those assets. However, unlike humans, contracts on the Convex network are executed exactly
as described, without ambiguity, without trusting anyone to carry on operations in your own best interest, and in a tamperproof
manneer.


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


## Defining and using callable functions

Creating new accounts which host callable functions is explained in details in the section about [actors](/cvm/actors). For
the time being, let us suppose the following fictitious accounts:

```clojure
;;;;; ACCOUNT #100 (caller)

*address*  ;; #100
*caller*   ;; nil
*origin*   ;; #100



;;;;; ACCOUNT #42 (callee)

;; Very simple function which defines a value under `x`.
;; Returns a map with addresses described above.
;;
;; `:callable?` metadata is set to true to indicate this function
;; is indeed a callable.

(defn set-x
  ^{:callable? true}
  [new-x]
  (def x
       new-x)
  {:address *address*
   :caller  *caller*
   :origin  *origin*})



;;;;; ACCOUNT #100 (caller)

;; Calling the callable function using `call`.
;;
(call #42
      (set-x :my-value))

;; -> {:address #42
;;    :caller  #100
;;    :origin  #100}
;;
;; During the call, when executing `set-x` as a callable function
;; in #42, executing account *address* became #42 and *caller*
;; was set to #100.


*address*

;; -> #100
;;
;; Now it is back to #100.

*caller*

;; nil

x

;; Error! Not defined in #100.

#42/x

;; -> :my-value
;;
;; Perfect, `x` was indeed defined in the callee account.
```

Callable functions can themselves use callable functions from other accounts, allowing for arbitrarily complex interactions to emerge.


## Access control

**Attention.** Previous example presents a major flaw: anyone can call the `set-x` callable function and change the value of `x` in `#42`.

In the vast majority of cases, access must be constrained. While more sophisticated examples are described in the section about [actors](/cvm/actors),
it typically boils down to checking if `*caller*` is a trusted account. Naturally, other conditions can be enforced as well.

```clojure
;; Improving our callable function in account #42.
;;
(defn set-x
  ^{:callable? true}
  [new-x]
  (when-not (= *caller*
               #100)
    (fail :TRUST
          "Unauthorized."))
  (def x
       new-x))


;; Ensures only account #100 can (re)define `x` in callee account.
;; Then proceed to redefine `x`.
```

**Attention.** Checking `*caller*` is usually a lot safer than checking `*origin*`. Indeed, callable functions can use callable functions from other accounts.
Let us suppose this chain of calls:

```
A -> B -> C
```

Let us also suppose that `C` checks by `*origin*` and allows `A` only. In that configuration, `B` can successfully call `C`, because `*origin*` is `A`. This behavior
is sometimes desirable but most likely, the intention when writing `C` was to strictly restrict access to `A` only. If `B` happens to be a rogue contract (by mistake or by
carelessness), it could have dire consequences.
