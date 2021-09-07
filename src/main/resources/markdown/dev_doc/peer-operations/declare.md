Prior to running, a peer must be declared. The following Convex Lisp functions are the only features not described in the [Convex Virtual Machine](/cvm)
section given their specificity.

A peer requires a public key:

```clojure
(def peer-key
     0x1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812)
```

A peer is created on-chain by providing its public key and its initial stake:

```clojure
(create-peer peer-key
             50000000)
```

The account calling this function becomes the owner of that newly declared peer. The power of a peer during consensus is directly and linearly related
to its stake in Convex Coins, although Convex Coins are freely distributed on the current test network.

In order for other peers to broadcasts beliefs and state updates to that new peer, a URL must be declared on-chain as well. Naturally, this URL must be
publicly accessible which will typically involve some port mapping when running the peer. The URL is provided in the metadata [map](/cvm/data-types/map)
attached to the peer by its controller. This map can also contain other arbitrary information if desired.

```clojure
(set-peer-data peer-key
               {:url "my-peer.com:18888"})
```

On-chain state contains a `:peers` key pointing to a [blob-map](/cvm/data-types/blob-map) of `peer public key` to `peer data`. Hence, information
about this example peer can be retrieved as such:

```clojure
(get-in *state*
        [:peers
         peer-key])
```
