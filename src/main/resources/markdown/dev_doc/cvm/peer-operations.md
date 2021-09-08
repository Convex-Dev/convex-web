Any peer participating in a network, whether a local one or the current test network on `convex.world`, must be declared in the network state.

For syncing with the current test network, the easiest solution is to execute the following steps via the [sandbox](/sandbox).

Peers emit blocks of transactions which trigger consensus. A block must be digitally signed by the peer that emitted it. Hence, a peer needs a key
pair. Several options for generating key pairs are outlined in [this section](/run-a-peer).

Let us assume the following public key (a [blob](/cvm/data-types/blob) containing 32 bytes):

```clojure
(def peer-key
     0x1ee6d2eCAB45DFC7e46d52B73ec2b3Ef65B95967c69b0BC8A106e97C214bb812)
```

First, the peer must be created on the network by providing its public key and its initial stake:


```clojure
(create-peer peer-key
             50000000)
```

The account calling this function becomes the owner of that newly declared peer. The power of a peer during consensus is directly and linearly related
to its stake in Convex Coins. A peer can be re-staked by its owner as such:

```clojure
(set-peer-stake peer-key
                75000000)
``` 

In order for other peers to broadcast consensus information to this peer, a URL must be declared on-chain as well. Without this, it will not be able
to actively participate in the network, albeit it will be able to get its state up-to-date by polling other peers.

Naturally, this URL must be publicly accessible. In practice, this will typically involve some port mapping when running the peer. The URL is provided
in the metadata [map](/cvm/data-types/map) attached to the peer by its controller. This map can also contain other arbitrary information if desired:

```clojure
(set-peer-data peer-key
               {:url "my-peer.com:18888"})
```

On-chain `*state*` contains a `:peers` key pointing to a [blob-map](/cvm/data-types/blob-map) of `peer public key` -> `peer data`. Hence, information
about this example peer can be retrieved as such:

```clojure
(get-in *state*
        [:peers
         peer-key])
```
