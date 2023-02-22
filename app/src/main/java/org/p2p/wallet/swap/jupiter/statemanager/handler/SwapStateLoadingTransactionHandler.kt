package org.p2p.wallet.swap.jupiter.statemanager.handler

import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateRoutesRefresher
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow

class SwapStateLoadingTransactionHandler(
    private val routesRefresher: SwapStateRoutesRefresher,
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.LoadingTransaction

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    ) {
        val oldState = state as SwapState.LoadingTransaction

        var tokenA: SwapTokenModel = oldState.tokenA
        var tokenB: SwapTokenModel = oldState.tokenB
        var amountTokenA: BigDecimal = oldState.amountTokenA
        var slippage: Double = oldState.slippage
        var activeRouteOrdinal = oldState.activeRoute

        when (action) {
            is SwapStateAction.SlippageChanged -> slippage = action.newSlippageValue
            SwapStateAction.SwitchTokens -> {
                tokenA = oldState.tokenB
                tokenB = oldState.tokenA
            }
            is SwapStateAction.TokenAAmountChanged -> amountTokenA = action.newAmount
            is SwapStateAction.TokenAChanged -> tokenA = action.newTokenA
            is SwapStateAction.TokenBChanged -> tokenB = action.newTokenB
            is SwapStateAction.ActiveRouteChanged -> activeRouteOrdinal = action.ordinalRouteNumber
            SwapStateAction.RefreshRoutes -> activeRouteOrdinal = SwapStateManager.DEFAULT_ACTIVE_ROUTE_ORDINAL

            SwapStateAction.EmptyAmountTokenA -> {
                stateFlow.value = SwapState.TokenAZero(tokenA, tokenB, slippage)
                return
            }

            SwapStateAction.InitialLoading -> {
                stateFlow.value = SwapState.InitialLoading
                return
            }
            SwapStateAction.SwapSuccess -> return
        }

        routesRefresher.refreshRoutes(
            state = stateFlow,
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage,
            activeRouteOrdinal = activeRouteOrdinal
        )
    }
}