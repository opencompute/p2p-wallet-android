package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.utils.HexString

class BridgeBundleResponse(
    @SerializedName("bundle_id")
    val bundleId: String,
    @SerializedName("user_wallet")
    val userWallet: EthAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("token")
    val erc20TokenAddress: EthAddress,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("transactions")
    val transactions: List<HexString>,
    @SerializedName("signatures")
    val signatures: List<HexString>? = null,
    @SerializedName("fees")
    val fees: BridgeBundleFeesResponse
)