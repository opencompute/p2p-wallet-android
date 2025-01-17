package org.p2p.core.network.environment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class NetworkEnvironment(val endpoint: String) : Parcelable {
    MAINNET("https://api.mainnet-beta.solana.com"),
    SOLANA("https://solana-api.projectserum.com"),
    RPC_POOL("https://p2p.rpcpool.com"),
    DEVNET("https://api.devnet.solana.com");
}
