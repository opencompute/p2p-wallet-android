package org.p2p.ethereumkit.internal.core

import android.app.Application
import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.api.core.*
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcBlock
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransaction
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransactionReceipt
import org.p2p.ethereumkit.internal.api.models.AccountState
import org.p2p.ethereumkit.internal.api.models.EthereumKitState
import org.p2p.ethereumkit.internal.api.storage.ApiStorage
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.core.storage.Eip20Storage
import org.p2p.ethereumkit.internal.core.storage.TransactionStorage
import org.p2p.ethereumkit.internal.core.storage.TransactionSyncerStateStorage
import org.p2p.ethereumkit.internal.crypto.CryptoUtils
import org.p2p.ethereumkit.internal.decorations.DecorationManager
import org.p2p.ethereumkit.internal.decorations.EthereumDecorator
import org.p2p.ethereumkit.internal.decorations.TransactionDecoration
import org.p2p.ethereumkit.internal.models.*
import org.p2p.ethereumkit.internal.network.*
import org.p2p.ethereumkit.internal.transactionsyncers.EthereumTransactionSyncer
import org.p2p.ethereumkit.internal.transactionsyncers.InternalTransactionSyncer
import org.p2p.ethereumkit.internal.transactionsyncers.TransactionSyncManager
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.p2p.ethereumkit.internal.crypto.InternalBouncyCastleProvider
import java.math.BigInteger
import java.security.Security
import java.util.*
import java.util.logging.Logger

class EthereumKit(
    private val blockchain: IBlockchain,
    private val transactionManager: TransactionManager,
    private val transactionSyncManager: TransactionSyncManager,
    private val connectionManager: ConnectionManager,
    private val address: EthAddress,
    val chain: Chain,
    val walletId: String,
    val transactionProvider: ITransactionProvider,
    val eip20Storage: IEip20Storage,
    private val decorationManager: DecorationManager,
    private val state: EthereumKitState = EthereumKitState()
) : IBlockchainListener {

    private val logger = Logger.getLogger("EthereumKit")
    private val disposables = CompositeDisposable()

    private val lastBlockHeightSubject = PublishSubject.create<Long>()
    private val syncStateSubject = PublishSubject.create<SyncState>()
    private val accountStateSubject = PublishSubject.create<AccountState>()

    val defaultGasLimit: Long = 21_000
    private val maxGasLimit: Long = 2_000_000
    private val defaultMinAmount: BigInteger = BigInteger.ONE

    private var started = false

    init {
        state.lastBlockHeight = blockchain.lastBlockHeight
        state.accountState = blockchain.accountState

        transactionManager.fullTransactionsAsync
            .subscribeOn(Schedulers.io())
            .subscribe {
                blockchain.syncAccountState()
            }.let {
                disposables.add(it)
            }
    }

    val lastBlockHeight: Long?
        get() = state.lastBlockHeight

    val accountState: AccountState?
        get() = state.accountState

    val syncState: SyncState
        get() = blockchain.syncState

    val transactionsSyncState: SyncState
        get() = transactionSyncManager.syncState

    val receiveAddress: EthAddress
        get() = address

    val lastBlockHeightFlowable: Flowable<Long>
        get() = lastBlockHeightSubject.toFlowable(BackpressureStrategy.BUFFER)

    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsSyncStateFlowable: Flowable<SyncState>
        get() = transactionSyncManager.syncStateAsync

    val accountStateFlowable: Flowable<AccountState>
        get() = accountStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val allTransactionsFlowable: Flowable<Pair<List<FullTransaction>, Boolean>>
        get() = transactionManager.fullTransactionsAsync

    fun start() {
        if (started)
            return
        started = true

        blockchain.start()
        transactionSyncManager.sync()
    }

    fun stop() {
        started = false
        blockchain.stop()
        state.clear()
        connectionManager.stop()
    }

    fun refresh() {
        blockchain.refresh()
        transactionSyncManager.sync()
    }

    fun getNonce(defaultBlockParameter: DefaultBlockParameter): Single<Long> {
        return blockchain.getNonce(defaultBlockParameter)
    }
    fun getFullTransactionsFlowable(tags: List<List<String>>): Flowable<List<FullTransaction>> {
        return transactionManager.getFullTransactionsFlowable(tags)
    }

    fun getFullTransactionsAsync(tags: List<List<String>>, fromHash: ByteArray? = null, limit: Int? = null): Single<List<FullTransaction>> {
        return transactionManager.getFullTransactionsAsync(tags, fromHash, limit)
    }

    fun getPendingFullTransactions(tags: List<List<String>>): List<FullTransaction> {
        return transactionManager.getPendingFullTransactions(tags)
    }

    fun getFullTransactions(hashes: List<ByteArray>): List<FullTransaction> {
        return transactionManager.getFullTransactions(hashes)
    }

    fun getFullTransactionSingle(hash: ByteArray): Single<FullTransaction> {
        return transactionManager.getFullTransactionSingle(hash)
    }

    fun estimateGas(to: EthAddress?, value: BigInteger, gasPrice: GasPrice): Single<Long> {
        // without address - provide default gas limit
        if (to == null) {
            return Single.just(defaultGasLimit)
        }

        // if amount is 0 - set default minimum amount
        val resolvedAmount = if (value == BigInteger.ZERO) defaultMinAmount else value

        return blockchain.estimateGas(to, resolvedAmount, maxGasLimit, gasPrice, null)
    }

    fun estimateGas(to: EthAddress?, value: BigInteger?, gasPrice: GasPrice, data: ByteArray?): Single<Long> {
        return blockchain.estimateGas(to, value, maxGasLimit, gasPrice, data)
    }

    fun estimateGas(transactionData: TransactionData, gasPrice: GasPrice): Single<Long> {
        return estimateGas(transactionData.to, transactionData.value, gasPrice, transactionData.input)
    }

    fun rawTransaction(
        transactionData: TransactionData,
        gasPrice: GasPrice,
        gasLimit: Long,
        nonce: Long? = null
    ): Single<RawTransaction> {
        return rawTransaction(
            address = transactionData.to,
            value = transactionData.value,
            transactionInput = transactionData.input,
            gasPrice = gasPrice,
            gasLimit = gasLimit,
            nonce = nonce
        )
    }

    fun rawTransaction(
        address: EthAddress,
        value: BigInteger,
        transactionInput: ByteArray = byteArrayOf(),
        gasPrice: GasPrice,
        gasLimit: Long,
        nonce: Long? = null
    ): Single<RawTransaction> {
        val nonceSingle = nonce?.let { Single.just(it) } ?: blockchain.getNonce(DefaultBlockParameter.Pending)

        return nonceSingle.flatMap { nonce ->
            Single.just(RawTransaction(gasPrice, gasLimit, address, value, nonce, transactionInput))
        }
    }

    fun send(rawTransaction: RawTransaction, signature: Signature): Single<FullTransaction> {
        logger.info("send rawTransaction: $rawTransaction")

        return blockchain.send(rawTransaction, signature)
            .map { transactionManager.handle(listOf(it)).first() }
    }

    fun decorate(transactionData: TransactionData): TransactionDecoration? {
        return decorationManager.decorateTransaction(address, transactionData)
    }

    fun transferTransactionData(address: EthAddress, value: BigInteger): TransactionData {
        return transactionManager.etherTransferTransactionData(address = address, value = value)
    }

    fun getLogs(address: EthAddress?, topics: List<ByteArray?>, fromBlock: Long, toBlock: Long, pullTimestamps: Boolean): Single<List<TransactionLog>> {
        return blockchain.getLogs(address, topics, fromBlock, toBlock, pullTimestamps)
    }

    fun getStorageAt(contractAddress: EthAddress, position: ByteArray, defaultBlockParameter: DefaultBlockParameter): Single<ByteArray> {
        return blockchain.getStorageAt(contractAddress, position, defaultBlockParameter)
    }

    fun call(contractAddress: EthAddress, data: ByteArray, defaultBlockParameter: DefaultBlockParameter = DefaultBlockParameter.Latest): Single<ByteArray> {
        return blockchain.call(contractAddress, data, defaultBlockParameter)
    }

    fun debugInfo(): String {
        val lines = mutableListOf<String>()
        lines.add("ADDRESS: $address")
        return lines.joinToString { "\n" }
    }

    fun statusInfo(): Map<String, Any> {
        val statusInfo = LinkedHashMap<String, Any>()

        statusInfo["Last Block Height"] = state.lastBlockHeight ?: "N/A"
        statusInfo["Sync State"] = blockchain.syncState.toString()
        statusInfo["Blockchain source"] = blockchain.source
        statusInfo["Transactions source"] = "Infura, Etherscan" //TODO

        return statusInfo
    }

    //
    //IBlockchainListener
    //

    override fun onUpdateLastBlockHeight(lastBlockHeight: Long) {
        if (state.lastBlockHeight == lastBlockHeight)
            return

        state.lastBlockHeight = lastBlockHeight
        lastBlockHeightSubject.onNext(lastBlockHeight)
        transactionSyncManager.sync()
    }

    override fun onUpdateSyncState(syncState: SyncState) {
        syncStateSubject.onNext(syncState)
    }

    override fun onUpdateAccountState(accountState: AccountState) {
        if (state.accountState == accountState) return

        state.accountState = accountState
        accountStateSubject.onNext(accountState)
    }

    fun addTransactionSyncer(transactionSyncer: ITransactionSyncer) {
        transactionSyncManager.add(transactionSyncer)
    }

    fun addMethodDecorator(decorator: IMethodDecorator) {
        decorationManager.addMethodDecorator(decorator)
    }

    fun addEventDecorator(decorator: IEventDecorator) {
        decorationManager.addEventDecorator(decorator)
    }

    fun addTransactionDecorator(decorator: ITransactionDecorator) {
        decorationManager.addTransactionDecorator(decorator)
    }

    internal fun <T> rpcSingle(rpc: JsonRpc<T>): Single<T> {
        return blockchain.rpcSingle(rpc)
    }

    sealed class SyncState {
        class Synced : SyncState()
        class NotSynced(val error: Throwable) : SyncState()
        class Syncing(val progress: Double? = null) : SyncState()

        override fun toString(): String = when (this) {
            is Syncing -> "Syncing ${progress?.let { "${it * 100}" } ?: ""}"
            is NotSynced -> "NotSynced ${error.javaClass.simpleName} - message: ${error.message}"
            else -> this.javaClass.simpleName
        }

        override fun equals(other: Any?): Boolean {
            if (other !is SyncState)
                return false

            if (other.javaClass != this.javaClass)
                return false

            if (other is Syncing && this is Syncing) {
                return other.progress == this.progress
            }

            return true
        }

        override fun hashCode(): Int {
            if (this is Syncing) {
                return Objects.hashCode(this.progress)
            }
            return Objects.hashCode(this.javaClass.name)
        }
    }

    open class SyncError : Exception() {
        class NotStarted : SyncError()
        class NoNetworkConnection : SyncError()
    }

    companion object {

        val gson = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
            .registerTypeAdapter(Long::class.java, LongTypeAdapter())
            .registerTypeAdapter(object : TypeToken<Long?>() {}.type, LongTypeAdapter())
            .registerTypeAdapter(Int::class.java, IntTypeAdapter())
            .registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            .registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
            .registerTypeHierarchyAdapter(DefaultBlockParameter::class.java, DefaultBlockParameterTypeAdapter())
            .registerTypeAdapter(object : TypeToken<Optional<RpcTransaction>>() {}.type, OptionalTypeAdapter<RpcTransaction>(RpcTransaction::class.java))
            .registerTypeAdapter(object : TypeToken<Optional<RpcTransactionReceipt>>() {}.type, OptionalTypeAdapter<RpcTransactionReceipt>(RpcTransactionReceipt::class.java))
            .registerTypeAdapter(object : TypeToken<Optional<RpcBlock>>() {}.type, OptionalTypeAdapter<RpcBlock>(RpcBlock::class.java))
            .create()

        fun init() {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(InternalBouncyCastleProvider.getInstance())
        }

        fun getInstance(
            application: Application,
            words: List<String>,
            passphrase: String = "",
            chain: Chain,
            rpcSource: RpcSource,
            transactionSource: TransactionSource,
            walletId: String
        ): EthereumKit {
            val seed = Mnemonic().toSeed(words, passphrase)
            val privateKey = Signer.privateKey(seed, chain)
            val address = ethereumAddress(privateKey)
            return getInstance(application, address, chain, rpcSource, transactionSource, walletId)
        }

        fun getInstance(
            application: Application,
            address: EthAddress,
            chain: Chain,
            rpcSource: RpcSource,
            transactionSource: TransactionSource,
            walletId: String
        ): EthereumKit {

            val connectionManager = ConnectionManager(application)

            val syncer: IRpcSyncer = when (rpcSource) {
                is RpcSource.WebSocket -> {
                    val rpcWebSocket = NodeWebSocket(rpcSource.url, gson, rpcSource.auth)
                    val webSocketRpcSyncer = WebSocketRpcSyncer(rpcWebSocket, gson)

                    rpcWebSocket.listener = webSocketRpcSyncer

                    webSocketRpcSyncer
                }
                is RpcSource.Http -> {
                    val apiProvider = NodeApiProvider(rpcSource.urls, gson, rpcSource.auth)
                    org.p2p.ethereumkit.internal.api.core.ApiRpcSyncer(
                        apiProvider,
                        connectionManager,
                        chain.syncInterval
                    )
                }
            }

            val transactionBuilder = TransactionBuilder(address, chain.id)
            val transactionProvider = transactionProvider(transactionSource, address)

            val apiDatabase = EthereumDatabaseManager.getEthereumApiDatabase(application, walletId, chain)
            val storage = ApiStorage(apiDatabase)

            val blockchain = RpcBlockchain.instance(address, storage, syncer, transactionBuilder)

            val transactionDatabase = EthereumDatabaseManager.getTransactionDatabase(application, walletId, chain)
            val transactionStorage = TransactionStorage(transactionDatabase)
            val transactionSyncerStateStorage = TransactionSyncerStateStorage(transactionDatabase)

            val erc20Database = EthereumDatabaseManager.getErc20Database(application, walletId, chain)
            val erc20Storage = Eip20Storage(erc20Database)

            val ethereumTransactionSyncer = EthereumTransactionSyncer(transactionProvider, transactionSyncerStateStorage)
            val internalTransactionsSyncer = InternalTransactionSyncer(transactionProvider, transactionStorage)

            val decorationManager = DecorationManager(address, transactionStorage)
            val transactionManager = TransactionManager(address, transactionStorage, decorationManager, blockchain, transactionProvider)
            val transactionSyncManager = TransactionSyncManager(transactionManager)

            transactionSyncManager.add(internalTransactionsSyncer)
            transactionSyncManager.add(ethereumTransactionSyncer)

            val ethereumKit = EthereumKit(
                blockchain,
                transactionManager,
                transactionSyncManager,
                connectionManager,
                address,
                chain,
                walletId,
                transactionProvider,
                erc20Storage,
                decorationManager
            )

            blockchain.listener = ethereumKit

            decorationManager.addTransactionDecorator(EthereumDecorator(address))

            return ethereumKit
        }

        fun clear(context: Context, chain: Chain, walletId: String) {
            EthereumDatabaseManager.clear(context, chain, walletId)
        }

        fun getNodeApiProvider(rpcSource: RpcSource.Http): NodeApiProvider {
            return NodeApiProvider(rpcSource.urls, gson, rpcSource.auth)
        }

        private fun transactionProvider(transactionSource: TransactionSource, address: EthAddress): ITransactionProvider {
            when (transactionSource.type) {
                is TransactionSource.SourceType.Etherscan -> {
                    val service = EtherscanService(transactionSource.type.apiBaseUrl, transactionSource.type.apiKey)
                    return EtherscanTransactionProvider(service, address)
                }
            }
        }

        private fun ethereumAddress(privateKey: BigInteger): EthAddress {
            val publicKey = CryptoUtils.ecKeyFromPrivate(privateKey).publicKeyPoint.getEncoded(false).drop(1).toByteArray()
            return EthAddress(CryptoUtils.sha3(publicKey).takeLast(20).toByteArray())
        }

    }

}