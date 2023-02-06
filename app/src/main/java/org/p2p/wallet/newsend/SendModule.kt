package org.p2p.wallet.newsend

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.newsend.repository.RecipientsDatabaseRepository
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.newsend.ui.NewSendContract
import org.p2p.wallet.newsend.ui.NewSendPresenter
import org.p2p.wallet.newsend.ui.details.NewSendDetailsContract
import org.p2p.wallet.newsend.ui.details.NewSendDetailsPresenter
import org.p2p.wallet.newsend.ui.search.NewSearchContract
import org.p2p.wallet.newsend.ui.search.NewSearchPresenter

object SendModule : InjectionModule {
    override fun create() = module {
        initDataLayer()
        singleOf(::SendModeProvider)

        factory<NewSearchContract.Presenter> { (initialToken: Token.Active?) ->
            NewSearchPresenter(
                initialToken = initialToken,
                searchInteractor = get(),
                usernameDomainFeatureToggle = get(),
                userInteractor = get(),
                newSendAnalytics = get()
            )
        }
        factoryOf(::NewSelectTokenPresenter) bind NewSelectTokenContract.Presenter::class
        factoryOf(::NewSendPresenter) bind NewSendContract.Presenter::class
        factoryOf(::NewSendDetailsPresenter) bind NewSendDetailsContract.Presenter::class
    }

    private fun Module.initDataLayer() {
        factoryOf(::RecipientsDatabaseRepository) bind RecipientsLocalRepository::class
    }
}