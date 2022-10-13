package org.p2p.wallet.debug

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesContract
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesPresenter
import org.p2p.wallet.debug.pushnotifications.PushNotificationsContract
import org.p2p.wallet.debug.pushnotifications.PushNotificationsPresenter
import org.p2p.wallet.debug.settings.DebugSettingsContract
import org.p2p.wallet.debug.settings.DebugSettingsPresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.settings.interactor.ThemeInteractor

object DebugSettingsModule : InjectionModule {

    override fun create() = module {
        factory { SettingsInteractor(get(), get()) }
        factory { ThemeInteractor(get()) }
        factory {
            DebugSettingsPresenter(
                environmentManager = get(),
                homeLocalRepository = get(),
                context = get(),
                resourcesProvider = get(),
                networkServicesUrlProvider = get()
            )
        } bind DebugSettingsContract.Presenter::class
        factoryOf(::FeatureTogglesPresenter) bind FeatureTogglesContract.Presenter::class
        factoryOf(::PushNotificationsPresenter) bind PushNotificationsContract.Presenter::class
    }
}
