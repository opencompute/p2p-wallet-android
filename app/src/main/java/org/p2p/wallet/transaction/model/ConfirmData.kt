package org.p2p.wallet.transaction.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.home.model.Token

@Parcelize
class ConfirmData(
    val token: Token.Active,
    val amount: String,
    val amountUsd: String,
    val destination: String
) : Parcelable {

    fun getFormattedAmount(): String = "$amount ${token.tokenSymbol}"

    fun getFormattedAmountUsd(): String = "~$$amountUsd"
}