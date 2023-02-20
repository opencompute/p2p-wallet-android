package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toLamports
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.utils.toBase58Instance

class SwapStateRoutesRefresher(
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository
) {
    suspend fun refreshRoutes(
        state: MutableStateFlow<SwapState>,
        tokenA: Token.Active,
        tokenB: Token,
        amountTokenA: BigDecimal,
        slippage: Double,
        activeRouteOrdinal: Int
    ) {
        state.value = SwapState.LoadingRoutes(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage
        )

        val updatedRoutes = fetchRoutes(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
        )
        val amountTokenB = updatedRoutes[activeRouteOrdinal]
            .outAmountInLamports
            .fromLamports(tokenA.decimals)

        state.value = SwapState.LoadingTransaction(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            routes = updatedRoutes,
            activeRoute = activeRouteOrdinal,
            amountTokenB = amountTokenB,
            slippage = slippage,
        )

        val freshSwapTransaction = swapTransactionRepository.createSwapTransactionForRoute(
            route = updatedRoutes[activeRouteOrdinal],
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )

        state.value = SwapState.SwapLoaded(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            amountTokenB = amountTokenB,
            routes = updatedRoutes,
            activeRoute = activeRouteOrdinal,
            swapBlockchainTransaction = freshSwapTransaction,
            slippage = slippage
        )
    }

    private suspend fun fetchRoutes(
        tokenA: Token.Active,
        tokenB: Token,
        amountTokenA: BigDecimal,
    ): List<JupiterSwapRoute> {
        val routesRequest = JupiterSwap(
            inputMint = tokenA.mintAddress.toBase58Instance(),
            outputMint = tokenB.mintAddress.toBase58Instance(),
            amountInLamports = amountTokenA.toLamports(tokenA.decimals)
        )
        return swapRoutesRepository.getSwapRoutes(
            jupiterSwap = routesRequest,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
    }
}