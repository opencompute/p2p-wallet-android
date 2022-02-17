package org.p2p.wallet.moonpay.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyData
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import timber.log.Timber
import java.math.BigDecimal

private const val DELAY_IN_MS = 500L

class BuySolanaPresenter(
    private val moonpayRepository: MoonpayRepository,
) : BasePresenter<BuySolanaContract.View>(), BuySolanaContract.Presenter {

    companion object {
        private const val TEMPORAL_ETH_SYMBOL = "ETH"
    }

    private var isSwapped: Boolean = false

    private var amount: String = "0"

    private var data: BuyData? = null

    private var calculationJob: Job? = null

    override fun loadData() {
        launch {
            try {
                view?.showLoading(true)
                val price = moonpayRepository.getCurrencyAskPrice(TEMPORAL_ETH_SYMBOL.lowercase()).scaleShort()
                view?.showTokenPrice("$USD_SYMBOL$price")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading currency ask price")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onContinueClicked() {
        data?.let { view?.navigateToMoonpay(it.total.toString()) }
    }

    override fun onSwapClicked() {
        isSwapped = !isSwapped
        val prefix = if (isSwapped) TEMPORAL_ETH_SYMBOL else USD_SYMBOL
        view?.swapData(isSwapped, prefix)
        setBuyAmount(amount)
    }

    override fun setBuyAmount(amount: String) {
        this.amount = amount
        calculateTokens(amount)
    }

    private fun calculateTokens(amount: String) {
        calculationJob?.cancel()

        val parsedAmount = amount.toBigDecimalOrZero()
        if (amount.isBlank() || parsedAmount.isZero()) {
            clear()
            return
        }

        val amountInTokens: String?
        val amountInCurrency: String?
        if (isSwapped) {
            amountInTokens = amount
            amountInCurrency = null
        } else {
            amountInTokens = null
            amountInCurrency = amount
        }

        calculationJob = launch {
            try {
                delay(DELAY_IN_MS)
                view?.showLoading(true)
                val baseCurrencyCode = USD_READABLE_SYMBOL.lowercase()
                when (
                    val result = moonpayRepository.getCurrency(
                        baseCurrencyAmount = amountInCurrency,
                        quoteCurrencyAmount = amountInTokens,
                        quoteCurrencyCode = "eth",
                        baseCurrencyCode = baseCurrencyCode
                    )
                ) {
                    is MoonpayBuyResult.Success -> handleSuccess(result.data)
                    is MoonpayBuyResult.Error -> view?.showMessage(result.message)
                }
            } catch (e: CancellationException) {
                Timber.w("Cancelled get currency request")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading buy currency data")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleSuccess(info: BuyCurrency) {
        val receiveSymbol = if (isSwapped) USD_SYMBOL else TEMPORAL_ETH_SYMBOL
        val amount = if (isSwapped) info.totalAmount.scaleShort() else info.receiveAmount
        val currencyForTokensAmount = info.price * info.receiveAmount.toBigDecimal()
        val data = BuyData(
            tokenSymbol = TEMPORAL_ETH_SYMBOL,
            currencySymbol = USD_SYMBOL,
            price = info.price.scaleShort(),
            receiveAmount = info.receiveAmount,
            processingFee = info.feeAmount.scaleShort(),
            networkFee = info.networkFeeAmount.scaleShort(),
            extraFee = info.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = info.totalAmount.scaleShort(),
            receiveAmountText = "$amount $receiveSymbol",
            purchaseCostText = if (isSwapped) currencyForTokensAmount.scaleShort().toString() else null
        )
        view?.showData(data).also { this.data = data }
        view?.showMessage(null)
    }

    private fun clear() {
        val data = data ?: return
        val clearedData = data.copy(
            receiveAmount = 0.0,
            processingFee = BigDecimal.ZERO,
            networkFee = BigDecimal.ZERO,
            extraFee = BigDecimal.ZERO,
            accountCreationCost = null,
            total = BigDecimal.ZERO
        )
        view?.showData(clearedData)
        view?.showMessage(null)
    }
}