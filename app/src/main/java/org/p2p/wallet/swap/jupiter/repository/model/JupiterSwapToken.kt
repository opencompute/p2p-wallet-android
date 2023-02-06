package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String

data class JupiterSwapToken(
    val address: Base58String,
    val chainId: Int,
    val decimals: Int,
    val coingeckoId: String,
    val logoUri: String,
    val name: String,
    val symbol: String,
    val tags: List<String>
)