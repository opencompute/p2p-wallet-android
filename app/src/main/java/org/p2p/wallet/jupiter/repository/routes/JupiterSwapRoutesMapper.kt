package org.p2p.wallet.jupiter.repository.routes

import org.p2p.wallet.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapFees
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMarketInformation
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.core.crypto.toBase58Instance

class JupiterSwapRoutesMapper {

    fun fromNetwork(response: SwapJupiterQuoteResponse): List<JupiterSwapRoute> = response.routes
        .map { route ->
            JupiterSwapRoute(
                inAmountInLamports = route.inAmount.toBigInteger(),
                outAmountInLamports = route.outAmount.toBigInteger(),
                priceImpactPct = route.priceImpactPct.toBigDecimal(),
                marketInfos = route.marketInfos.toSwapMarketInformation(),
                amountInLamports = route.amount.toBigInteger(),
                slippageBps = route.slippageBps,
                otherAmountThreshold = route.otherAmountThreshold,
                swapMode = route.swapMode,
                fees = route.fees.toSwapFee(),
                keyAppFeeInLamports = route.keyAppFees.fee.toBigInteger(),
                keyAppRefundableFee = route.keyAppFees.refundableFee,
                keyAppHash = route.keyAppFees.hash
            )
        }

    private fun List<SwapRouteRequest.MarketInfoRequest>.toSwapMarketInformation(): List<JupiterSwapMarketInformation> =
        map { response ->
            JupiterSwapMarketInformation(
                id = response.id,
                label = response.label,
                inputMint = response.inputMint.toBase58Instance(),
                outputMint = response.outputMint.toBase58Instance(),
                notEnoughLiquidity = response.notEnoughLiquidity,
                inAmountInLamports = response.inAmount.toBigInteger(),
                outAmountInLamports = response.outAmount.toBigInteger(),
                minInAmountInLamports = response.minInAmount?.toBigInteger(),
                minOutAmountInLamports = response.minOutAmount?.toBigInteger(),
                priceImpactPct = response.priceImpactPct.toBigDecimal(),
                liquidityFee = response.lpFee.let { responseFee ->
                    JupiterSwapMarketInformation.LpFee(
                        amountInLamports = responseFee.amount.toBigInteger(),
                        mint = responseFee.mint.toBase58Instance(),
                        percent = responseFee.pct.toBigDecimal()
                    )
                },
                platformFee = response.platformFee.let { responseFee ->
                    JupiterSwapMarketInformation.PlatformFee(
                        amountInLamports = responseFee.amountInLamports.toBigInteger(),
                        mint = responseFee.mint.toBase58Instance(),
                        percent = responseFee.pct.toBigDecimal()
                    )
                }
            )
        }

    private fun JupiterSwapFeesRequest.toSwapFee(): JupiterSwapFees = JupiterSwapFees(
        signatureFee = signatureFeeInLamports.toBigInteger(),
        openOrdersDeposits = openOrdersDepositsLamports.map { it.toBigInteger() },
        ataDeposits = ataDeposits.map { it.toBigInteger() },
        totalFeeAndDepositsInSol = totalFeeAndDepositsLamports.toBigInteger(),
        minimumSolForTransaction = minimumSolForTransactionLamports.toBigInteger(),
    )
}
