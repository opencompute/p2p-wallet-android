package org.p2p.solanaj.rpc.model

class RecentPerformanceSample(
    val numberOfSlots: Int,
    val numberOfTransactions: Int,
    val samplePeriodInSeconds: Int,
    val slot: Int
)
