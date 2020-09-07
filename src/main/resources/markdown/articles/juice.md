# Juice

## Rationale

On-chain transactions must be controlled via cryptoeconomic methods to ensure that scarce on-chain compute capacity is utilised productively.

Without such controls, the Convex system would face significant problems from:
- Lack of incentives to write efficient code
- Cheap denial of service attacks on the network by wasting compute time
- Tragedy of the commons effects, where compute is treated as free but the cost falls on peer operators

## Overview of Solution

Every transaction submitted to Convex must be backed by a reserve of juice. 

At the start of a transaction, Juice can be obtained by making some portion of the User Account's coin balance available to purcase Juice at the prevailing Juice Price. If insufficient Juice is available from the account that submits the transaction, it will automatically fail with the JUICE exception.

During the execution of the transaction, juice is consumed depending on the type of transaction.

- A Transfer transaction pays a fixed juice cost
- An Invoke or Call transaction pays juice for each CVM operation executed, according to a pre-defined juice cost schedule.

At the end of the transaction, any remaining juice is converted back to coins. 

If coins are required to pay for additional memory allowance, they are subtracted at this point. If the available coins are insufficient, the transaction will fail with the MEMORY exception (and any transaction effects will be rolled back).

Any remaining coins after the transaction is fully executed are refunded to the User's Account.

## Juice pricing

If the network is uncongested, juice costs can be kept very low (practically free). However, congestion causes problems because it imposes costs (sometimes called negative externalities) on other users of the network.

The ideal solution suggested by economics is to impose a system of *congestion pricing*, such that the costs of using a scare resource are increased to the point that some users decide not to submit transactions during congested periods. This ensures that high value transactions can still get through, but low value transactions are deferred - perhaps until "off peak" times, or bundled into larger more valuable transactions.

Convex therefore implements a system of dynamic pricing for juice: the coin cost of each unit of juice varies depending on the rate of juice consumption in the recent past.

## CVM operation costs

Costs for various CVM instructions are still undergoing tuning. The intention is for juice costs to be roughly proportional to the compute resources required to execute the operation on a typical Peer computer.

Because of this:

- Operations which simply perform a local read are very cheap (~10 juice)
- Operations which simply perform a  more complex read are moderately cheap (~50 juice)
