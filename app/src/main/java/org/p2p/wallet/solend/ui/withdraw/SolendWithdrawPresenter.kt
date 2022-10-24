package org.p2p.wallet.solend.ui.withdraw

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.interactor.SolendWithdrawInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetails
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toLamports
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FEE_DELAY_IN_MS = 250L

class SolendWithdrawPresenter(
    token: SolendDepositToken.Active,
    private val resourcesProvider: ResourcesProvider,
    private val depositsInteractor: SolendDepositInteractor,
    private val withdrawInteractor: SolendWithdrawInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor
) : BasePresenter<SolendWithdrawContract.View>(), SolendWithdrawContract.Presenter {

    private var selectedDepositToken = token
    private var validDeposits: List<SolendDepositToken.Active> = emptyList()

    private var currentInput: BigDecimal = BigDecimal.ZERO
    private var currentOutput: BigDecimal = BigDecimal.ZERO

    private var calculateFeeJob: Job? = null

    override fun attach(view: SolendWithdrawContract.View) {
        super.attach(view)
        initializeTokens(view)
        loadOrcaInfo()
    }

    private fun initializeTokens(view: SolendWithdrawContract.View) {
        view.showTokenToWithdraw(
            depositToken = selectedDepositToken,
            withChevron = validDeposits.size > 1
        )

        if (validDeposits.isEmpty()) {
            launch {
                try {
                    validDeposits = depositsInteractor.getUserDeposits()
                        .filterIsInstance<SolendDepositToken.Active>()
                        .filter { it.depositAmount.isMoreThan(BigDecimal.ZERO) }

                    val validDeposit = validDeposits.find { it.tokenSymbol == selectedDepositToken.tokenSymbol }
                    validDeposit?.let { selectTokenToWithdraw(it) }
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available withdraw tokens")
                    view.showUiKitSnackBar(e.getErrorMessage { res -> resourcesProvider.getString(res) })
                }
            }
        }
    }

    override fun selectTokenToWithdraw(tokenToWithdraw: SolendDepositToken.Active) {
        if (selectedDepositToken != tokenToWithdraw) {
            selectedDepositToken = tokenToWithdraw
        }
        view?.showTokenToWithdraw(
            depositToken = tokenToWithdraw,
            withChevron = validDeposits.size > 1
        )
    }

    override fun onTokenWithdrawClicked() {
        view?.showTokensToWithdraw(validDeposits)
    }

    override fun updateInputs(input: BigDecimal, output: BigDecimal) {
        this.currentInput = input
        this.currentOutput = output

        calculateFee(input, output)
    }

    override fun withdraw() {
        launch {
            try {
                val amountInLamports = currentInput.toLamports(selectedDepositToken.decimals)
                withdrawInteractor.withdraw(selectedDepositToken, amountInLamports)
                view?.showIndefiniteInfoMessage(
                    messageRes = R.string.solend_withdraw_in_progress,
                    actionButtonRes = R.string.common_hide
                )
                view?.navigateToEarnScreen()
            } catch (e: Throwable) {
                Timber.e(e, "Error withdrawing from ${selectedDepositToken.tokenSymbol}")
                view?.showErrorMessage(e)
            }
        }
    }

    private fun calculateFee(input: BigDecimal, output: BigDecimal) {
        calculateFeeJob?.cancel()
        calculateFeeJob = launch {
            delay(FEE_DELAY_IN_MS)

            view?.showFeeLoading(isLoading = true)
            val amountInLamports = currentInput.toLamports(selectedDepositToken.decimals)
            val amountInUsd = (currentInput * selectedDepositToken.usdRate).scaleShort()

            val fee = try {
                withdrawInteractor.calculateWithdrawFee(
                    amountInLamports = amountInLamports,
                    token = selectedDepositToken
                )
            } catch (e: CancellationException) {
                Timber.w(e, "Calculate deposit fees was cancelled")
                return@launch
            } catch (e: Throwable) {
                Timber.e(e, "Error calculating deposit fees")
                view?.showUiKitSnackBar(messageResId = R.string.error_calculating_fees)
                return@launch
            }

            val transferFee = fee.getTransferFeeInDecimals()
            val transferFeeUsd = (transferFee * fee.usdRate).scaleShort()

            val rentFee = fee.getRentFeeInDecimals()
            val rentFeeUsd = (rentFee * fee.usdRate).scaleShort()

            var totalInLamports = amountInLamports
            var totalInUsd = (currentInput * selectedDepositToken.usdRate).scaleShort()

            val tokenSymbol = fee.symbol
            if (selectedDepositToken.tokenSymbol == fee.symbol) {
                totalInLamports += fee.fee.total
                totalInUsd = totalInLamports.fromLamports(fee.decimals).scaleShort()
            } else {
                totalInUsd = totalInUsd + transferFeeUsd + rentFeeUsd
            }

            val total = totalInLamports.fromLamports(fee.decimals).scaleLong()
            val detailsData = SolendTransactionDetails(
                amount = "$input $tokenSymbol (~$ $amountInUsd)",
                transferFee = if (transferFee.isNotZero()) "$transferFee $tokenSymbol (~$ $transferFeeUsd)" else null,
                fee = "$rentFee $tokenSymbol (~$ $rentFeeUsd)",
                total = "$total $tokenSymbol (~$ $totalInUsd)"
            )
            updateWithdrawState(input, output, detailsData)

            view?.showFeeLoading(isLoading = false)
        }
    }

    private fun updateWithdrawState(input: BigDecimal, output: BigDecimal, data: SolendTransactionDetails) {
        val maxDepositAmount = selectedDepositToken.depositAmount.orZero()
        val tokenAmount = buildString {
            append(maxDepositAmount.formatToken())
            append(" ")
            append(selectedDepositToken.tokenSymbol)
        }
        val isBiggerThenMax = input > maxDepositAmount
        when {
            input.isZero() && output.isZero() -> view?.setEmptyAmountState()
            isBiggerThenMax -> view?.setBiggerThenMaxAmountState(tokenAmount)
            else -> view?.setValidDepositState(output, tokenAmount, SolendTransactionDetailsState.Withdraw(data))
        }
    }

    private fun loadOrcaInfo() {
        view?.showFullScreenLoading(isLoading = true)
        launch {
            try {
                orcaInfoInteractor.load()
            } catch (e: Throwable) {
                Timber.e(e, "Error initializing orca")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                view?.showFullScreenLoading(isLoading = false)
            }
        }
    }
}
