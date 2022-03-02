package org.p2p.wallet.renbtc.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.p2p.wallet.renbtc.db.RenTransactionEntity.Companion.TABLE_NAME
import org.p2p.wallet.renbtc.model.RenBTCPayment

@Entity(
    tableName = TABLE_NAME
)
data class RenTransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_TRANSACTION_ID)
    val transactionId: String,
    @Embedded
    val payment: RenBTCPayment,
) {

    companion object {
        const val TABLE_NAME = "ren_transaction_table"
        const val COLUMN_TRANSACTION_ID = "column_transaction_id"
    }
}