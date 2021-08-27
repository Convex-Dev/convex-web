As stated in the [section about accounts](/cvm/accounts), an actor is an account which does not have a public key. As such, actors cannot submit
transactions autonomously. Code is executed when an actor is deployed and this code determines everything this actor hosts and offers, from
[smart contracts](/cvm/smart-contracts) to various utilities. This section reviews common patterns.


## Deploying a new actor

The [section about code is data](/cvm/code-is-data) describes into detail how to write code which is not evaluated by using `quote` and `quasiquote`.
When deploying an actor, code is provided. An new account is created by the CVM and this code is executed in the context of that new account, as if
it were submitted as a transaction by that new account.

```clojure
(def my-actor
     (deploy '(def x
                   42)))

;; `deploy` returns the address of the newly created account.

(actor? my-actor)

;; True, as for any account which does not have a public key.

x

;; Error! `x` is not defined in original account.

my-actor/x

;; 42
;;
;; Great, `x` has indeed been defined in this new actor at initialization.
```


## Library functions

Programming languages typically have a notion about libraries or namespaces which provides reusable utilities. In Convex Lisp, actors can provide
reusable functions:

```clojure
(def my-lib
     (deploy '(defn square
                [x]
                (* x x))))


(my-lib/square 3)

;; 9
```

**Attention.** As stated in the [section about functions](/cvm/functions), users must only apply known functions from trusted accounts since they
run under the control of the executing account and have access to all its funds and assets. Not knowing exactly what a function does is like giving
your home keys to a shady stranger and walking away.


## Smart contracts

An actor, being an account, has an environment and can [define symbols](/cvm/definitions). In other words, it can manage state persisted in the
decentralized database. [Smart contracts](/cvm/smart-contracts) provide access control and describe how this state is effectively managed.

Following example of an actor implementing a minimalistic oracle where the creator account can append values to a [vector](/cvm/vector):

```clojure
(def oracle-sc
     (deploy '(do
                ;; Remembers its creator.
                (def trust-addr
                     *caller*)

                ;; Vector of values, initally empty.
                (def values
                     [])

                ;; Smart contract for adding values.
                ;; Enforces proper access. Anyone could try to call this contract, hence `*caller*` might not be the same address
                ;; as the creator remembered under `trust-addr` during initialization.
                ;;
                (defn add-value
                  ^{:callable? true}
                  [x]
                  (when-not (= *caller*
                               trust-addr)
                    (fail :TRUST
                          "Only original creator can add values."))
                  (def values
                       (conj values
                             x))))))


oracle-sc/values

;; []

(call oracle-sc
      (add-value 1))

oracle-sc/values

;; [1]

(call oracle-sc
      (add-value :b))

oracle-sc/values

;; [1 :b]
```

Some smart contracts can be well scoped within a single actor. More complex schemes might require several actors and sophisticated interactions.
However, the core buildings blocks are still the same.


## Clean APIs over smart contracts

Depending on the target users, interacting directly with smart contracts may appear as a [leaky abstraction](https://en.wikipedia.org/wiki/Leaky_abstraction).
Sometimes, some features involves calling several contracts, maybe even over several accounts. Overall, it is adviced deploying another actor, a pure one
whose only job is to provide a clean and unified API.

Although our previous example is extremely simple, a clean API could look like this:

```clojure
;; Assuming `oracle-sc` is still defined from previous example.

;; New actor code is prepared using `quasiquote` so that `oracle-sc` is embedded in the code using `unquote`.
;; Remember a new actor starts from scratch with its own environment.
(def oracle-api
     (deploy `(do
                (defn add-value
                  [x]
                  (call ~oracle-sc
                        (add-value x)))

                (defn get-values
                  [x]
                  (lookup ~oracle-sc
                          values)))))

(oracle-api/get-values)

;; [1 :b]

(oracle-api/add-value "three")

(oracle-api/get-values)

;; [1 :b "three"]
```


## Repeated deployments

The ability to use `quasiquote` for templating code means it is trivial creating functions which prepare code, notably meant for prepare actor code:

```clojure
(defn multiplier-code

  [x]

  `(defn mult
     [y]
     (* ~x y)))


(def my-lib
     (deploy (multiplier-code 5)))


(my-lib/mult 3)   ;; 15
(my-lib/mult 10)  ;; 50
```


## Upgradable actors

By default, actor code is run when new account is initiliazed and they is no other way to potentially change anything within an actor but by calling
a smart contract which does exactly what its implementation describes. However, when needed, 2 generic solutions exist for keeping an actor under
control.

First solution is to set a controller at initialization, as described in the [section about accounts](/cvm/accounts):

```clojure
(def my-actor
     (deploy '(set-controller *caller*)))


my-actor/x

;; Error! `x` is undefined in this actor.


(eval-as my-actor
         '(def x
               42))

my-actor/x

;; 42
;;
;; At initiliazation, *caller* is set to the address of the creator.
;; By being its controller, we can use `eval-as` and submit any code we desire to be executed by our actor, like a puppet.
```

Second solution is to write a smart contract which explicitly evaluates code. The `eval` function is explained in greater details in the [section
about compiler phases](/cvm/compiler-phases). In short, it allows to execute any arbitrary piece of code:

```clojure
;; New actor defines a smart contract `do-anything` which is simply a reference to the `eval` function.
(def my-actor-2
     (deploy '(def do-anything
                ^{:callable? true}
                eval)))

my-actor-2/x

;; Error! `x` is undefined in this actor.


(call my-actor-2
      (do-anything '(def x
                         42)))

my-actor-2/x

;; 42
```

**Attention.** Beware when interacting with upgradable actors. It means that at any time, someone can redefine behavior without notice. It is probably
a very bad idea to interact with such actors unless accouts with that kind of powers are entirely trusted, without any doubt.


## Transferring funds to actors

Like any account, actors have their own balance of Convex Coins. However, by default, they are not capable of directly receiving Convex Coins:

```clojure
(def my-actor
     (deploy nil))

;; Deployed an empty actor, no code is executed at initialization.

(transfer my-actor
          100)

;; Error! Detects it is an actor and complains.
```

This is because unless an actor is programmed to somehow manage its balance, those coins would be lost forever.

After taking precautions, a `receive-coin` smart contract can be defined for explicitly accepting coins use the `accept` function:

```clojure
(def my-actor
     (deploy '(do
                ;; Remembering creator.
                (def trust-addr
                     *caller*)

                ;; Contract allowing creator to transfer funds so that coins are not lost.
                ;;
                (defn transfer-funds
                  ^{:callable? true}
                  [addr]
                  (when-not (= *caller*
                               trust-addr)
                    (fail :TRUST
                          "Only creator can transfer funds."))
                  (transfer addr
                            *balance*))

                ;; Contract accepting incoming transfers
                ;; Total amount is accepted.
                ;; 3rd argument is nil at the moment, but mandatory.
                ;;
                (defn receive-coin
                  ^{:callable? true}
                  [sender amount _]
                  (accept amount)))))


(transfer my-actor
          100)

;; Works.

(call my-actor
      (transfer-funds *address*))

;; Get back all funds.
```

Alternatively, offered coins are always available under `*offer*`.


## Tipping an actor

A tip can be provided when using `call`. Once again, `accept` is used to accept any amount of offered coins.
An actor could enforce a mandatory tip:

```clojure
;; Should also have a contract such as `transfer-funds` from previous example.
;; Omitted for brievety.
;;
(def my-actor
     (deploy '(defn set-x
                ^{:callable? true}
                [new-x]
                (when (< *offer*
                         100)
                  (fail :FUNDS
                        "Mandatory tip of at least 100 coins."))
                (accept 100)
                (def x
                     new-x))))


(call my-actor
      (set-x 42))

;; Error! Rejected! We did not provide any coin.


(call my-actor
      50
      (set-x 42))

;; Error! Rejected! Tip is not big enough.


(call my-actor
      100
      (set-x 42))

;; Okay, tip was big enough.


my-actor/x

;; 42
```
