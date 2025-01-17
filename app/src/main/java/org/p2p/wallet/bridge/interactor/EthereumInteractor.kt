package org.p2p.wallet.bridge.interactor

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.core.EthereumKit
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.bridge.claim.interactor.EthBridgeClaimInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class EthereumInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val claimInteractor: EthBridgeClaimInteractor,
    private val ethereumRepository: EthereumRepository,
    private val bridgeSendInteractor: BridgeSendInteractor
) {

    fun setup(userSeedPhrase: List<String>) {
        EthereumKit.init()
        ethereumRepository.init(userSeedPhrase)
    }

    fun isInitialized(): Boolean {
        return ethereumRepository.isInitialized()
    }

    suspend fun loadWalletTokens(claimTokens: List<EthereumClaimToken>): List<Token.Eth> {
        return ethereumRepository.loadWalletTokens(claimTokens)
    }

    suspend fun cacheWalletTokens(tokens: List<Token.Eth>) {
        ethereumRepository.cacheWalletTokens(tokens)
    }

    suspend fun updateTokensRates(tokensRates: List<TokenServicePrice>) {
        ethereumRepository.updateTokensRates(tokensRates)
    }

    suspend fun loadClaimTokens(): List<EthereumClaimToken> {
        return claimInteractor.getEthereumClaimTokens(getEthUserAddress())
    }

    suspend fun loadSendTransactionDetails() {
        bridgeSendInteractor.getSendTransactionDetails(SolAddress(tokenKeyProvider.publicKey))
    }

    fun observeTokensFlow(): Flow<List<Token.Eth>> {
        return ethereumRepository.getWalletTokensFlow()
    }

    fun getEthTokens(): List<Token.Eth> {
        return ethereumRepository.getWalletTokens()
    }

    fun getEthUserAddress(): EthAddress = ethereumRepository.getAddress()

    suspend fun getEthereumBundle(erc20Token: EthAddress?, amount: String): BridgeBundle {
        val ethereumAddress: EthAddress = ethereumRepository.getAddress()
        return claimInteractor.getEthereumClaimableToken(
            erc20Token = erc20Token,
            amount = amount,
            ethereumAddress = ethereumAddress
        )
    }

    fun signClaimTransaction(transaction: HexString): Signature {
        return ethereumRepository.signTransaction(transaction)
    }

    suspend fun sendClaimBundle(signatures: List<Signature>) {
        return claimInteractor.sendEthereumBundle(signatures)
    }

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal {
        return claimInteractor.getEthereumMinAmountForFreeFee()
    }

    fun getClaimBundleById(bundleId: String): BridgeBundle? {
        return claimInteractor.getClaimBundleById(bundleId)
    }

    fun getSendBundleById(bundleId: String): BridgeSendTransactionDetails? {
        return claimInteractor.getSendBundleById(bundleId)
    }
}
