package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.FeeRelayerState.Failure
import org.p2p.wallet.newsend.model.FeeRelayerState.ReduceAmount
import org.p2p.wallet.newsend.model.FeeRelayerState.UpdateFee
import org.p2p.wallet.newsend.model.FeeRelayerStateError
import org.p2p.wallet.newsend.model.FeeRelayerStateError.FeesCalculationError
import org.p2p.wallet.newsend.model.FeeRelayerStateError.InsufficientFundsToCoverFees
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates
import kotlinx.coroutines.CancellationException

class SendFeeRelayerManager(
    private val sendInteractor: SendInteractor
) {

    var onStateUpdated: ((FeeRelayerState) -> Unit)? = null
    var onFeeLoading: ((isLoading: Boolean) -> Unit)? = null

    private var currentState: FeeRelayerState by Delegates.observable(FeeRelayerState.Idle) { _, oldState, newState ->
        onStateUpdated?.invoke(newState)
    }

    private lateinit var feeLimitInfo: FreeTransactionFeeLimit
    private lateinit var recipientAddress: SearchResult
    private lateinit var solToken: Token.Active

    private var minRentExemption: BigInteger? = null

    suspend fun initialize(
        initialToken: Token.Active,
        solToken: Token.Active,
        recipientAddress: SearchResult
    ) {
        this.recipientAddress = recipientAddress
        this.solToken = solToken

        minRentExemption = sendInteractor.getMinRelayRentExemption()
        feeLimitInfo = sendInteractor.getFreeTransactionsInfo()
        sendInteractor.initialize(initialToken)
    }

    fun getMinRentExemption(): BigInteger = minRentExemption.orZero()

    fun getState(): FeeRelayerState = currentState

    fun buildTotalFee(
        sourceToken: Token.Active,
        calculationMode: CalculationMode,
    ): SendFeeTotal {
        val currentAmount = calculationMode.getCurrentAmount()
        return SendFeeTotal(
            currentAmount = currentAmount,
            currentAmountUsd = calculationMode.getCurrentAmountUsd(),
            receive = "${currentAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = currentAmount.toUsd(sourceToken),
            sourceSymbol = sourceToken.tokenSymbol,
            sendFee = (currentState as? UpdateFee)?.solanaFee,
            recipientAddress = recipientAddress.addressState.address,
            feeLimit = feeLimitInfo
        )
    }

    /**
     * Launches the auto-selection mechanism
     * Selects automatically the fee payer token if there is enough balance
     * */
    suspend fun executeSmartSelection(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy,
        tokenAmount: BigDecimal
    ) {
        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        try {
            onFeeLoading?.invoke(true)
            val feeRelayerFee = calculateFeeRelayerFee(
                sourceToken = sourceToken,
                feePayerToken = feePayer,
                result = recipientAddress
            )

            if (feeRelayerFee == null) {
                currentState = UpdateFee(solanaFee = null, feeLimitInfo = feeLimitInfo)
                return
            }

            val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
            showFeeDetails(
                sourceToken = sourceToken,
                feeRelayerFee = feeRelayerFee,
                feePayerToken = feePayer,
                inputAmount = inputAmount,
                strategy = strategy
            )
        } catch (e: Throwable) {
            Timber.e(e, "Error during FeeRelayer fee calculation")
        } finally {
            onFeeLoading?.invoke(false)
        }
    }

    suspend fun buildDebugInfo(solanaFee: SendSolanaFee?): String {
        val relayAccount = sendInteractor.getUserRelayAccount()
        val relayInfo = sendInteractor.getRelayInfo()
        return buildString {
            append("Relay account is created: ${relayAccount.isCreated}, balance: ${relayAccount.balance} (A)")
            appendLine()
            append("Min relay account balance required: ${relayInfo.minimumRelayAccountRent} (B)")
            appendLine()
            if (relayAccount.balance != null) {
                val diff = relayAccount.balance - relayInfo.minimumRelayAccountRent
                append("Remainder (A - B): $diff (R)")
                appendLine()
            }

            if (solanaFee == null) {
                append("Expected total fee in SOL: 0 (E)")
                appendLine()
                append("Needed top up amount (E - R): 0 (S)")
                appendLine()
                append("Expected total fee in Token: 0 (T)")
            } else {
                val expectedFee = solanaFee.feeRelayerFee.expectedFee.accountBalances
                append("Expected total fee in SOL: $expectedFee (E)")
                appendLine()

                val neededTopUpAmount = solanaFee.feeRelayerFee.totalInSol
                append("Needed top up amount (E - R): $neededTopUpAmount (S)")

                appendLine()

                val feePayerToken = solanaFee.feePayerToken
                val expectedFeeInSpl = solanaFee.feeRelayerFee.totalInSpl.orZero()
                    .fromLamports(feePayerToken.decimals)
                    .scaleLong()
                append("Expected total fee in Token: $expectedFeeInSpl ${feePayerToken.tokenSymbol} (T)")
            }
        }
    }

    /*
     * Assume this to be called only if associated account address creation needed
     * */
    private suspend fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        result: SearchResult
    ): FeeRelayerFee? {
        val recipient = result.addressState.address

        return try {
            sendInteractor.calculateFeesForFeeRelayer(
                feePayerToken = feePayerToken,
                token = sourceToken,
                recipient = recipient
            )
        } catch (e: CancellationException) {
            Timber.i("Fee calculation is cancelled")
            return null
        } catch (e: Throwable) {
            Timber.e(e, "Error calculating fees")
            handleError(FeesCalculationError)
            return null
        }
    }

    private suspend fun showFeeDetails(
        sourceToken: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        feePayerToken: Token.Active,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeRelayerFee)

        if (strategy == FeePayerSelectionStrategy.NO_ACTION) {
            validateFunds(sourceToken, fee, inputAmount)
            currentState = UpdateFee(fee, feeLimitInfo)
        } else {
            validateAndSelectFeePayer(sourceToken, fee, inputAmount, strategy)
        }
    }

    private fun validateFunds(source: Token.Active, fee: SendSolanaFee, inputAmount: BigInteger) {
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount
        )

        if (!isEnoughToCoverExpenses) {
            handleError(InsufficientFundsToCoverFees)
        }
    }

    private fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeRelayerFee: FeeRelayerFee
    ): SendSolanaFee =
        SendSolanaFee(
            feePayerToken = newFeePayer,
            sourceTokenSymbol = source.tokenSymbol,
            solToken = solToken,
            feeRelayerFee = feeRelayerFee
        )

    private suspend fun validateAndSelectFeePayer(
        sourceToken: Token.Active,
        fee: SendSolanaFee,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy
    ) {

        // Assuming token is not SOL
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.UpdateFeePayer -> {
                sendInteractor.setFeePayerToken(sourceToken)
            }
            is FeePayerState.SwitchToSol -> {
                sendInteractor.switchFeePayerToSol(this.solToken)
            }
            is FeePayerState.ReduceInputAmount -> {
                sendInteractor.setFeePayerToken(sourceToken)
                currentState = ReduceAmount(state.maxAllowedAmount)
            }
        }

        recalculate(sourceToken, inputAmount)
    }

    private suspend fun recalculate(sourceToken: Token.Active, inputAmount: BigInteger) {
        /*
         * Optimized recalculation and UI update
         * */
        val newFeePayer = sendInteractor.getFeePayerToken()
        val feeRelayerFee = calculateFeeRelayerFee(
            sourceToken = sourceToken,
            feePayerToken = newFeePayer,
            result = recipientAddress
        ) ?: return
        val fee = buildSolanaFee(newFeePayer, sourceToken, feeRelayerFee)
        validateFunds(sourceToken, fee, inputAmount)
        currentState = UpdateFee(fee, feeLimitInfo)
    }

    private fun handleError(error: FeeRelayerStateError) {
        currentState = Failure(error)
    }
}
