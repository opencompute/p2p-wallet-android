package org.p2p.wallet.jupiter.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.jupiter.api.request.SwapRouteRequest

data class SwapJupiterQuoteResponse(
    @SerializedName("data")
    val routes: List<SwapRouteRequest>
)
