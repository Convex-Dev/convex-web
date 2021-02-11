(ns convex-web.config)

(def default-range 15)

(def max-range 25)

(def max-faucet-amount 100000000)

(def faucet-wait-millis
  "Milliseconds a user has to wait, since last request, to submit a Faucet."
  (* 1000 60 5))
