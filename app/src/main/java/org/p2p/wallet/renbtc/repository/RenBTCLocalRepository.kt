package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenTransactionStatus

interface RenBTCLocalRepository {
    fun getAllTransactions(): List<RenTransactionStatus>
}