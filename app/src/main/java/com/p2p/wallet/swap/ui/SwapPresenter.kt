package com.p2p.wallet.swap.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.model.FeeType
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.model.SwapFee
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates

// TODO: Refactor this class, too complicated logic, it can be optimized
class SwapPresenter(
    private val initialToken: Token?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: SwapInteractor,
    private val serumSwapInteractor: SerumSwapInteractor
) : BasePresenter<SwapContract.View>(), SwapContract.Presenter {

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Double = 0.5

    private var sourceRate: BigDecimal = BigDecimal.ZERO
    private var destinationRate: BigDecimal = BigDecimal.ZERO

    private var lamportsPerSignature: BigInteger = BigInteger.ZERO
    private var creatingAccountFee: BigInteger = BigInteger.ZERO
    private var liquidityProviderFee: BigDecimal = BigDecimal.ZERO

    private var currentFees = mutableMapOf<FeeType, SwapFee>()

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            sourceToken = initialToken ?: userInteractor.getUserTokens().find { it.isSOL }

            val availableAmount = swapInteractor.calculateAvailableAmount(sourceToken!!, currentFees[FeeType.DEFAULT])
            val available = "$availableAmount ${sourceToken!!.tokenSymbol}"
            view?.showSourceAvailable(available)

            lamportsPerSignature = swapInteractor.getLamportsPerSignature()
            liquidityProviderFee = swapInteractor.calculateLiquidityProviderFee()
            creatingAccountFee = swapInteractor.getCreatingTokenAccountFee()

            view?.showSlippage(slippage)
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            view?.openSourceSelection(tokens)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token ->
                token.mintAddress != sourceToken?.mintAddress
            }
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        if (sourceToken == newToken) return

        sourceToken = newToken

        val availableAmount = swapInteractor.calculateAvailableAmount(newToken, currentFees[FeeType.DEFAULT])
        val scaledAmount = availableAmount?.scaleLong() ?: BigDecimal.ZERO
        val available = "$scaledAmount ${newToken.tokenSymbol}"
        view?.showSourceAvailable(available)

        destinationToken = null
        view?.hidePrice()
        destinationAmount = "0"

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)

        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken

        launch {
            calculateRateAndFees(sourceToken!!, newToken)
            calculateAmount(sourceToken!!, newToken)
        }

        setButtonEnabled()
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
        view?.showSlippage(slippage)
    }

    override fun loadDataForSwapSettings() {
        view?.openSwapSettings(Slippage.parse(slippage))
    }

    override fun loadSlippage() {
        view?.openSlippageDialog(Slippage.parse(slippage))
    }

    override fun feedAvailableValue() {
        view?.updateInputValue(requireSourceToken().total.scaleLong())
    }

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount
        val token = sourceToken ?: return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = token.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(token.total)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        calculateAmount(requireSourceToken(), destinationToken)

        setButtonEnabled()
    }

    override fun reverseTokens() {

        /* reversing tokens */
        val newSource = destinationToken!!
        val newDestination = sourceToken!!
        sourceToken = null

        /* reversing amounts */
        sourceAmount = destinationAmount
        destinationAmount = ""

        /* rate is being used at [calculateAmount], so reversing fields as well */
        val oldSourceRate = sourceRate
        sourceRate = destinationRate
        destinationRate = oldSourceRate

        setNewSourceToken(newSource)
        setNewDestinationToken(newDestination)
        view?.showButtonEnabled(false)
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val finalAmount = sourceAmount.toBigDecimalOrZero()

                val transactionId = serumSwapInteractor.swap(
                    fromWallet = requireSourceToken(),
                    toWallet = requireDestinationToken(),
                    amount = finalAmount,
                    slippage = slippage
                )

                val info = TransactionInfo(
                    transactionId = transactionId,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = destinationAmount.toBigDecimalOrZero(),
                    usdAmount = aroundValue,
                    tokenSymbol = requireDestinationToken().tokenSymbol
                )
                view?.showSwapSuccess(info)
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun calculateRateAndFees(source: Token, destination: Token) {
        try {
            view?.showButtonText(R.string.swap_calculating_fees)

            sourceRate = swapInteractor.loadPrice(source.mintAddress, destination.mintAddress).scaleMedium()
            destinationRate = swapInteractor.loadPrice(destination.mintAddress, source.mintAddress).scaleMedium()

            val priceData = PriceData(
                sourceAmount = sourceRate.toString(),
                destinationAmount = destinationRate.toString(),
                sourceSymbol = source.tokenSymbol,
                destinationSymbol = destination.tokenSymbol
            )
            view?.showPrice(priceData)

            /* Load reverse price and caching it */

            val fees = swapInteractor.calculateFees(
                sourceToken = source,
                destinationToken = destination,
                lamportsPerSignature = lamportsPerSignature,
                creatingAccountFee = creatingAccountFee
            )

            currentFees.putAll(fees)

            val liquidityFee = currentFees[FeeType.LIQUIDITY_PROVIDER]?.stringValue.orEmpty()
            val defaultFee = currentFees[FeeType.DEFAULT]

            val networkFee = if (defaultFee != null) {
                val formattedFee = defaultFee.lamports
                    .fromLamports(source.decimals)
                    .toFee()
                    .scaleMedium()

                "$formattedFee ${defaultFee.tokenSymbol}"
            } else null

            val feeOption = "${source.tokenSymbol}+${destination.tokenSymbol}"
            view?.showFees(networkFee = networkFee.orEmpty(), liquidityFee = liquidityFee, feeOption)
        } catch (e: Throwable) {
            Timber.e(e, "Error calculating network fees")
        } finally {
            updateButtonText(source)
        }
    }

    private fun calculateAmount(source: Token, destination: Token?) {
        if (destination == null) return
        calculationJob?.cancel()
        calculationJob = launch {
            val estimatedAmount = swapInteractor.calculateEstimatedAmount(
                inputAmount = sourceAmount.toDoubleOrNull(),
                rate = sourceRate.toDouble(),
                slippage = slippage
            )?.scaleMedium()

            destinationAmount = (estimatedAmount ?: BigDecimal.ZERO).toString()

            val data = AmountData(
                destinationAmount = destinationAmount,
                estimatedReceiveAmount = estimatedAmount ?: BigDecimal.ZERO
            )

            updateButtonText(source)
            view?.showCalculations(data)
            view?.showSlippage(slippage)
        }

        setButtonEnabled()
    }

    private fun setButtonEnabled() {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero() > sourceToken?.total ?: BigDecimal.ZERO
        val isEnabled = sourceAmount.toBigDecimalOrZero().compareTo(BigDecimal.ZERO) != 0 &&
            !isMoreThanBalance && destinationToken != null
        view?.showButtonEnabled(isEnabled)
    }

    private fun updateButtonText(source: Token) {
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = source.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)

        when {
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            else -> view?.showButtonText(R.string.main_swap_now)
        }
    }

    private fun requireSourceToken(): Token =
        sourceToken ?: throw IllegalStateException("Source token is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}

data class AmountData(
    val destinationAmount: String,
    val estimatedReceiveAmount: BigDecimal
)

data class PriceData(
    val sourceAmount: String,
    val destinationAmount: String,
    val sourceSymbol: String,
    val destinationSymbol: String
)

private fun BigDecimal.toFee() = this / BigDecimal(1000)