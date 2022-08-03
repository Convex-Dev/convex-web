Here is an example of a minimalistic voting system where only selected accounts can participate.


## Human description

This system will be an [actor](/cvm/accounts/actors) managing the entire voting process. It requires 4 elements.

**Allowed accounts.** A [set](/cvm/data-types/set) of [addresses](/cvm/data-types/address) designating which accounts
are allowed to vote. When an allowed account votes, its address is removed from this set because each account can vote
only once.

**End.** A [Unix timestamp](https://en.wikipedia.org/wiki/Unix_time) designating when voting ends. After this
deadline, votes are not accepted anymore.

**Question.** Most likely some [text](/cvm/data-types/text) describing the proposal in human language, assmuing voters
are human.

**Votes.** A [map](/cvm/data-type/map) where keys are possible answers and values are the numbers of votes for each answer.

**Vote.** A voting [callable function](/cvm/accounts/callable-functions). First, it ensures that the calling account is allowed
to vote, that deadline is not met yet, and that its choice is valid. Then, if those conditions are satisfied, vote
is accepted and the calling account is removed from allowed accounts so that it cannot vote a second time. A
[keyword](/cvm/data-types/keyword) is returned to provide feedback.


## First draft

For the time being, let us hardcode those values in [quoted code](/cvm/building-blocks/code-is-data).

```clojure
(def proposal-code
     ;; Quoted so that none of it is executed.
     ;;
     (quote
       (do
     
         ;; Just a few accounts for this example.
         ;;
         (def allowed-accounts
              #{*caller* #42 #100})

         ;; Voting closes in 1 day from now (in milliseconds).
         ;;
         ;; 1000 milliseconds per second, 60 seconds per minute,
         ;; 60s minute per hour, and 24 hours per day.
         ;;
         (def end
              (+ *timestamp*
                 (* 1000 60 60 24)))

         ;; Essential question that must be answered.
         ;;
         (def question
              "Do you like Belgian waffles?")
     
         ;; Initial votes for possible answers.
         ;;
         ;; All 0's, no one voted yet.
         ;;
         (def votes
              {:no  0
               :meh 0
               :yes 0})
     
         ;; Voting callable function.
         ;;
         ;; *caller* is the address calling the function.
         ;;
         ;; Ensures all requirements for voting are satisfied.
         ;; If so, removes authorized *caller* from allowed accounts
         ;; and accepts selected answer.
         ;;
         (defn vote
           ^{:callable? true}
           [answer]
           (cond
             (not (contains-key? allowed-accounts
                                 *caller*))
             :not-allowed
     
             (>= *timestamp*
                 end)
             :too-late
     
             (not (contains-key? votes
                                 answer))
             :unknown-answer
     
             :else
             (do
               (def allowed-accounts
                    (disj allowed-accounts
                          *caller*))
               (def votes
                    (assoc votes
                           answer
                           (+ (get votes
                                   answer)
                              1)))
               :voted))))))
```

Now this code can be deployed as an actor:

```clojure
(def my-proposal
     (deploy proposal-code))
```

And since our address is an allowed account (`*caller*` during actor deployment), we can vote.

Providing an unknown answer does not work:

```clojure
(call my-proposal
      (vote :hello))

;; -> :unknown-answer
```

But voting for one of the three possible answers does work:

```clojure
(call my-proposal
      (vote :yes))

;; -> :voted
```

Indeed:

```clojure
my-proposal/votes

;; -> {:no  0
;;    :meh 0
;;    :yes 1}
```

However, we cannot vote a second time:

```clojure
(call my-proposal
      (vote :no))

;; -> :not-allowed
```


## Making it reusable

Our first draft works but it is not reusable. It is certainly not optimal rewriting everything for
any new proposal. In reality, the logic is sound and direct. What is needed is the ability to replace
those important values mentioned above.

The following version is built around a [function](/cvm/building-blocks/functions) which does code templating via
[quasiquote](/cvm/building-blocks/code-is-data?section=Quasiquote+and+unquote). It takes as parameters the four
values mentioned above which determine what is the question, what are the possible answers, who is allowed to vote
and until when.

`quasiquote` prevents evaluation, just like `quote`, but allows using `unquote` (here shortened to **~**) in order
to insert some precomputed values. That is how values are inserted. The voting function remains as before.

```clojure
(defn proposal-code
  [question answers end allowed-accounts]
  (quasiquote
    (do

      (def allowed-accounts
           ~(into #{}
                  allowed-accounts))

      (def end
           ~end)

      (def question
           ~question)

      (def votes
           ~(reduce (fn [votes answer]
                      (assoc votes
                             answer
                             0))
                    {}
                    answers))

      (defn vote
        ^{:callable? true}
        [answer]
        (cond
          (not (contains-key? allowed-accounts
                              *caller*))
          :not-allowed

          (>= *timestamp*
              end)
          :too-late

          (not (contains-key? votes
                              answer))
          :unknown-answer

          :else
          (do
            (def allowed-accounts
                 (disj allowed-accounts
                       *caller*))
            (def votes
                 (assoc votes
                        answer
                        (+ (get votes
                                answer)
                           1)))
            :voted))))))
```

Deploying a new proposal is now easy:

```clojure
(def my-proposal
     (deploy (proposal-code "Do you like Belgian waffles?"
                            [:no :meh :yes]
                            (+ *timestamp*
                               (* 1000 60 60))
                            #{*address* #42 #1000})))
```


## Additional notes

A lot of different ways with a lot of different capabilities can be envisioned.

For instance, how would you:

- Keep track of who is voting for what answer?
- Allow an admin to add voters later, on the fly?
- Add vote delegation?
