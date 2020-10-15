(ns convex-web.glossary)

(def block-number
  "Block number in which the transaction was included.")

(def transaction-index
  "Index position of the transaction within the block. Lower indexed transactions were executed first.")

(def sequence-number
  "Sequence Number for each Transaction, each valid Transaction for an Account must increase the number by one.")
