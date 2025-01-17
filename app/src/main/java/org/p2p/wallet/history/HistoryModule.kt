package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.repository.local.PendingTransactionsInMemoryRepository
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsDatabaseRepository
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.local.mapper.TransactionDetailsEntityMapper
import org.p2p.wallet.history.repository.remote.BridgeHistoryRepository
import org.p2p.wallet.history.repository.remote.HistoryPendingTransactionsCleaner
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.HistoryRepository
import org.p2p.wallet.history.repository.remote.MoonpayHistoryRemoteRepository
import org.p2p.wallet.history.repository.remote.RpcHistoryRepository
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsContract
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsPresenter
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.history.ui.historylist.HistoryListViewPresenter
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinkDetailsContract
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinkDetailsPresenter
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinksContract
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinksPresenter
import org.p2p.wallet.history.ui.token.TokenHistoryContract
import org.p2p.wallet.history.ui.token.TokenHistoryPresenter
import org.p2p.wallet.push_notifications.PushNotificationsModule
import org.p2p.wallet.rpc.api.RpcTransactionApi
import org.p2p.wallet.sell.interactor.HistoryItemMapper

object HistoryModule : InjectionModule {

    override fun create(): Module = module {
        dataLayer()

        factoryOf(::RpcHistoryTransactionConverter)
        factoryOf(::HistoryItemMapper)
        factoryOf(::HistorySellTransactionMapper)

        factoryOf(::HistoryPresenter) bind
            HistoryContract.Presenter::class
        factoryOf(::TokenHistoryPresenter) bind
            TokenHistoryContract.Presenter::class
        factoryOf(::HistoryListViewPresenter) bind
            HistoryListViewContract.Presenter::class
        factoryOf(::HistoryTransactionDetailsPresenter) bind
            HistoryTransactionDetailsContract.Presenter::class

        factoryOf(::HistorySendLinksPresenter) bind HistorySendLinksContract.Presenter::class
        factoryOf(::HistorySendLinkDetailsPresenter) bind HistorySendLinkDetailsContract.Presenter::class
    }

    private fun Module.dataLayer() {
        single {
            get<Retrofit>(named(PushNotificationsModule.NOTIFICATION_SERVICE_RETROFIT_QUALIFIER)).create(
                RpcHistoryServiceApi::class.java
            )
        }

        factoryOf(::TransactionDetailsEntityMapper)
        singleOf(::HistoryServiceSignatureFieldGenerator)
        factoryOf(::HistoryPendingTransactionsCleaner)
        single<HistoryRemoteRepository> {
            val remotes = listOf(
                new(::RpcHistoryRepository),
                new(::MoonpayHistoryRemoteRepository),
                new(::BridgeHistoryRepository)
            )
            HistoryRepository(
                repositories = remotes,
                dispatchers = get(),
                pendingTransactionsLocalRepository = get(),
                pendingTransactionsCleaner = get()
            )
        }

        factoryOf(::TransactionDetailsDatabaseRepository) bind TransactionDetailsLocalRepository::class
        singleOf(::PendingTransactionsInMemoryRepository) bind PendingTransactionsLocalRepository::class
        single { get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcTransactionApi::class.java) }

        factoryOf(::HistoryInteractor)
    }
}
