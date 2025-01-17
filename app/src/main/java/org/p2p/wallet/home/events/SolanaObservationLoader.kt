package org.p2p.wallet.home.events

import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.solana.SolanaNetworkObserver

class SolanaObservationLoader(
    private val networkObserver: SolanaNetworkObserver,
    private val appScope: AppScope,
) : AppLoader {

    override suspend fun onLoad() {
        appScope.launch { networkObserver.start() }
    }
}
