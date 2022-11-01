package org.p2p.wallet.solend.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.relay.RelayRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendFee
import org.p2p.wallet.solend.model.SolendTokenFee
import org.p2p.wallet.solend.repository.SolendRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import java.math.BigInteger

class SolendWithdrawInteractor(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val solendRepository: SolendRepository,
    private val relayRepository: RelayRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun withdraw(token: SolendDepositToken, amountInLamports: BigInteger): String {
        val account = Account(tokenKeyProvider.keyPair)
        val ownerAddress = account.publicKey.toBase58Instance()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        val remainingFreeTransactionsCount = freeTransactionFeeLimit.remaining
        val relayProgramId = FeeRelayerProgram.getProgramId(isMainnet = true).toBase58()

        // todo: use `hasFreeTransactions` when fee relayer is fixed
        val hasFreeTransactions = false /* freeTransactionFeeLimit.hasFreeTransactions() */
        val realFeePayerAddress = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        val serializedTransaction = solendRepository.createWithdrawTransaction(
            relayProgramId = relayProgramId,
            ownerAddress = ownerAddress,
            token = token,
            withdrawAmount = amountInLamports,
            remainingFreeTransactionsCount = 0,
            lendingMarketAddress = null,
            blockhash = recentBlockhash,
            payFeeWithRelay = hasFreeTransactions,
            feePayerToken = null,
            realFeePayerAddress = realFeePayerAddress,
        ) ?: error("Error occurred while creating a withdraw transaction")

        val keypair = account.getEncodedKeyPair()
        val signedTransaction = relayRepository.signTransaction(serializedTransaction, keypair, recentBlockhash)

        return rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedTransaction,
            encoding = Encoding.BASE58
        )
    }

    suspend fun calculateWithdrawFee(
        amountInLamports: BigInteger,
        token: SolendDepositToken
    ): SolendFee {
        val account = Account(tokenKeyProvider.keyPair)
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()

        val hasFreeTransactions = freeTransactionFeeLimit.hasFreeTransactions()
        val feePayer = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey

        val withdrawFeeInSol = solendRepository.getWithdrawFee(
            owner = account.publicKey.toBase58Instance(),
            feePayer = feePayer.toBase58Instance(),
            tokenAmount = amountInLamports,
            tokenSymbol = token.tokenSymbol
        )

        // calculating fee in SPL token
        val feeInSplToken = try {
            feeRelayerInteractor.calculateFeeInPayingToken(
                feeInSOL = FeeAmount(
                    transaction = if (hasFreeTransactions) BigInteger.ZERO else withdrawFeeInSol.transaction,
                    accountBalances = withdrawFeeInSol.rent
                ),
                payingFeeTokenMint = token.mintAddress
            )
        } catch (e: IllegalStateException) {
            null
        }

        val solToken = userInteractor.getUserSolToken() ?: error("No SOL token account found")

        val feeInSol = SolendFee(
            tokenSymbol = solToken.tokenSymbol,
            decimals = solToken.decimals,
            usdRate = solToken.usdRateOrZero,
            fee = withdrawFeeInSol,
            feePayer = TokenAccount(
                address = solToken.publicKey,
                mint = solToken.mintAddress
            )
        )

        val splToken = userInteractor.findUserToken(token.mintAddress)
        if (splToken == null || feeInSplToken == null) return feeInSol

        if (feeInSplToken.total > (splToken.totalInLamports)) return feeInSol

        // calculated fee in SPL token
        return SolendFee(
            tokenSymbol = token.tokenSymbol,
            decimals = splToken.decimals,
            usdRate = splToken.usdRateOrZero,
            fee = SolendTokenFee(feeInSplToken),
            feePayer = TokenAccount(
                address = splToken.publicKey,
                mint = splToken.mintAddress
            )
        )
    }
}