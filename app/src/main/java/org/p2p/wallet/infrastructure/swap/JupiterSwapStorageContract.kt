package org.p2p.wallet.infrastructure.swap

import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.routes.JupiterAvailableSwapRoutesMap
import org.p2p.core.crypto.Base58String

interface JupiterSwapStorageContract {
    var savedTokenAMint: Base58String?
    var savedTokenBMint: Base58String?

    var routesFetchDateMillis: Long?
    var routesMap: JupiterAvailableSwapRoutesMap?

    var swapTokensFetchDateMillis: Long?
    var swapTokens: List<JupiterSwapToken>?

    fun clear()
}
