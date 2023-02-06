package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.swap.jupiter.repository.model.SwapFees
import org.p2p.wallet.swap.jupiter.repository.model.SwapMarketInformation
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.toBase58Instance

class JupiterSwapRoutesMapper {

    fun toSwapRoute(response: SwapJupiterQuoteResponse): List<SwapRoute> = response.routes
        .map { route ->
            SwapRoute(
                inAmount = route.inAmount.toBigDecimal(),
                outAmount = route.outAmount.toBigDecimal(),
                priceImpactPct = route.priceImpactPct.toBigDecimal(),
                marketInfos = route.marketInfos.toSwapMarketInformation(),
                amount = route.amount.toBigDecimal(),
                slippageBps = route.slippageBps,
                otherAmountThreshold = route.otherAmountThreshold,
                swapMode = route.swapMode,
                fees = route.fees.toSwapFee()
            )
        }

    private fun List<SwapRouteRequest.MarketInfoRequest>.toSwapMarketInformation(): List<SwapMarketInformation> =
        map { response ->
            SwapMarketInformation(
                id = response.id,
                label = response.label,
                inputMint = response.inputMint.toBase58Instance(),
                outputMint = response.outputMint.toBase58Instance(),
                notEnoughLiquidity = response.notEnoughLiquidity,
                inAmount = response.inAmount.toBigDecimal(),
                outAmount = response.outAmount.toBigDecimal(),
                minInAmount = response.minInAmount?.toBigDecimal(),
                minOutAmount = response.minOutAmount?.toBigDecimal(),
                priceImpactPct = response.priceImpactPct,
                lpFee = response.lpFee.let { responseFee ->
                    SwapMarketInformation.LpFee(
                        amount = responseFee.amount,
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct
                    )
                },
                platformFee = response.platformFee.let { responseFee ->
                    SwapMarketInformation.PlatformFeeRequest(
                        amount = responseFee.amount,
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct
                    )
                }
            )
        }

    private fun JupiterSwapFeesRequest.toSwapFee(): SwapFees = SwapFees(
        signatureFee = signatureFeeInLamports.toBigInteger(),
        openOrdersDeposits = openOrdersDepositsLamports.map { it.toBigInteger() },
        ataDeposits = ataDeposits.map { it.toBigInteger() },
        totalFeeAndDeposits = totalFeeAndDepositsLamports.toBigInteger(),
        minimumSolForTransaction = minimumSolForTransactionLamports.toBigInteger(),
    )
}