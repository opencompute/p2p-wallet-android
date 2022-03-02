package org.p2p.wallet.renbtc.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.wallet.renbtc.db.RenTransactionStatusEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class RenTransactionStatusEntity(
    @PrimaryKey
    private val transactionId: String
) {

    companion object {
        const val TABLE_NAME = "ren_transaction_status_table"
        const val COLUMN_TRANSACTION_ID = "column_status_transaction_id"

    }
}