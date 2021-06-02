package com.p2p.wallet.token.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.p2p.wallet.R
import com.p2p.wallet.amount.toPowerValue
import com.p2p.wallet.common.network.Constants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

@Parcelize
data class Token(
    val tokenSymbol: String,
    val publicKey: String,
    val decimals: Int,
    val mintAddress: String,
    val tokenName: String,
    val iconUrl: String,
    val price: BigDecimal,
    val total: BigDecimal,
    val walletBinds: BigDecimal,
    @ColorRes val color: Int,
    val exchangeRate: Double,
    val isHidden: Boolean
) : Parcelable {

    @IgnoredOnParcel
    val isZero: Boolean
        get() = total.compareTo(BigDecimal.ZERO) == 0

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = tokenName == SOL_NAME

    @IgnoredOnParcel
    val visibilityIcon: Int
        get() = if (isHidden) R.drawable.ic_show else R.drawable.ic_hide

    fun getFormattedMintAddress(): String = if (mintAddress == SOL_MINT) {
        Constants.WRAPPED_SOL_MINT
    } else {
        mintAddress
    }

    @Suppress("MagicNumber")
    fun getFormattedAddress(): String {
        if (publicKey.length < ADDRESS_SYMBOL_COUNT) {
            return publicKey
        }

        val firstSix = publicKey.take(4)
        val lastFour = publicKey.takeLast(4)
        return "$firstSix...$lastFour"
    }

    fun getFormattedPrice(): String = "${price.setScale(2, RoundingMode.HALF_EVEN)} $"

    fun getFormattedTotal(): String = "$total $tokenSymbol"

    fun getFormattedExchangeRate(): String = String.format("%.2f", exchangeRate)

    companion object {
        private const val ADDRESS_SYMBOL_COUNT = 10
        private const val SOL_DECIMALS = 9
        private const val SOL_MINT = "SOLMINT"
        private const val SOL_NAME = "SOL"
        private const val SOL_LOGO_URL =
            "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/solana/info/logo.png"

        /* fixme: workaround about adding hardcode wallet, looks strange */
        fun getSOL(publicKey: String, amount: Long) = Token(
            tokenSymbol = SOL_NAME,
            tokenName = SOL_NAME,
            mintAddress = SOL_MINT,
            iconUrl = SOL_LOGO_URL,
            publicKey = publicKey,
            decimals = SOL_DECIMALS,
            total = BigDecimal(amount).divide(SOL_DECIMALS.toPowerValue()),
            price = BigDecimal.ZERO,
            walletBinds = BigDecimal.ZERO,
            color = R.color.chartSOL,
            exchangeRate = 0.0,
            isHidden = false
        )
    }
}