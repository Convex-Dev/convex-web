Often, smart contracts require data coming from the outside world. For instance, in IoT projects, data is coming from sensors.
An oracle is a trusted account which is entitled to deliver such data on-chain. For instance, the account of a particular
IoT device.


## Human description

Here is an example of an [actor](/cvm/accounts/actors) managing streams of data where a stream is a [vector](/cvm/data-types/vector)
of values that any account can open. Although it could have been organized differently, this example requires 4 elements.

**Streams.** A [map](/cvm/data-types/map) where keys are identifiers (anything) and values are vectors of arbitrary values
fed by an oracle.

**Owners.** A [map](/cvm/data-types/map) where keys are stream identifiers and values are [addresses](/cvm/data-types/address)
of accounts authorized to feed those streams.

**Open.** A [callable function](/cvm/accounts/callable-functions) for creating a stream and remembering who can feed values
to it.

**Feed.** A [callable function](/cvm/accounts/callable-functions) for actually feeding values to streams.


## Plausible implementation

```clojure
(def oracle-code
     ;; Quoted so that none of it is executed just yet.
     ;;
     (quote
       (do

         ;; Map for remembering who owns a particular stream.
         ;;
         (def owners
              {})

         ;; Map of values organized by stream identifiers.
         ;;
         (def streams
              {})

         ;; Callable function that remembers the owner of the given
         ;; stream id and prepares an empty vector for that stream
         ;; (no values yet).
         ;;
         ;; Returns true if successfully created, false otherwise. 
         ;;
         (defn open
           ^{:callable? true}
           [stream-id]
           (if (contains-key? streams
                              stream-id)
             false
             (do
               (def owners
                    (assoc owners
                           stream-id
                           *caller*))
               (def streams
                    (assoc streams
                           stream-id
                           []))
               true)))

         ;; Callable function for adding a value to stream, provided
         ;; it exists and the calling account owns it.
         ;;
         ;; Unless an error occurs, returns the given value.
         ;;
         (defn feed
           ^{:callable? true}
           [stream-id value]
           (let [owner (get owners
                            stream-id)]
             (when (nil? owner)
               (fail :STATE
                     "Stream does not exist"))
             (when-not (= owner
                          *caller*)
               (fail :TRUST
                     "Unauthorized to feed stream")))
           (def streams
                (assoc streams
                       stream-id
                       (conj (get streams
                                  stream-id)
                             value)))
           value))))
```

This oracle code can now be deployed as an actor:

```clojure
(def simple-oracle
     (deploy oracle-code))
```

Let us imagine we are an IoT device periodically feeding temperatures in °C.

First, a stream with an arbitrary identifier must be opened:

```clojure
(call simple-oracle
      (open :temperature-watcher))

;; -> true
;;
;; New stream created.
```

From now on, our account is authorized to feed values to this new stream. We decide arbitrarily that
for our use case, a value consists of [vector](/cvm/data-types/vector) with a timestamp and a temperature.

```clojure
;; A first one...
;;
(call simple-oracle
      (feed :temperature-watcher
            [1631205877992
             26.6]))

;; Another one some time later...
;;
(call simple-oracle
      (feed :temperature-watcher
            [1631217820358
             22.7]))
```

Indeed, values are being added under the right stream:

```clojure
(get simple-oracle/streams
     :temperature-watcher)

;; -> [[1631205877992 26.6]
;;    [1631217820358 22.7]]
   
```

Any other account is not authorized to feed value to this stream:

```clojure
(deploy (quasiquote (call ~simple-oracle
                          (feed :temperature-watcher
                                [1631222452798
                                 20.1]))))

;; Error! TRUST: Unauthorized to feed stream
```


## Additional notes

Many features could be added to this simple oracle platform. How would you:

- Authorize several accounts per stream?
- Remove old unnecessary values?
- Enforce some form of data validation?
