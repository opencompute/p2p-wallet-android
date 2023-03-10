package org.p2p.ethereumkit.internal.core.eip1559

data class FeeHistory(
        val baseFeePerGas: List<Long>,
        val gasUsedRatio: List<Double>,
        val oldestBlock: Long,
        val reward: List<List<Long>>
)