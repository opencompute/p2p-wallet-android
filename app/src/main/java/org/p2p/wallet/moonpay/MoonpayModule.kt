package org.p2p.wallet.moonpay

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.repository.buy.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.buy.MoonpayBuyRemoteRepository
import org.p2p.wallet.moonpay.repository.buy.MoonpayBuyRepository
import org.p2p.wallet.moonpay.repository.buy.NewMoonpayBuyRemoteRepository
import org.p2p.wallet.moonpay.repository.buy.NewMoonpayBuyRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepositoryMapper
import org.p2p.wallet.moonpay.serversideapi.MoonpayServerSideApi
import retrofit2.Retrofit

object MoonpayModule : InjectionModule {
    override fun create() = module {
        factoryOf(::MoonpayApiMapper)

        factory<MoonpayClientSideApi> {
            val retrofit = get<Retrofit>(named(NetworkModule.MoonpayRetrofitQualifier.CLIENT_SIDE_MOONPAY))
            retrofit.create(MoonpayClientSideApi::class.java)
        }
        factory<MoonpayServerSideApi> {
            val retrofit = get<Retrofit>(named(NetworkModule.MoonpayRetrofitQualifier.SERVER_SIDE_PROXY))
            retrofit.create(MoonpayServerSideApi::class.java)
        }

        factoryOf(::MoonpayBuyRemoteRepository) bind MoonpayBuyRepository::class
        factoryOf(::NewMoonpayBuyRemoteRepository) bind NewMoonpayBuyRepository::class

        factoryOf(::MoonpaySellRepositoryMapper)
        singleOf(::MoonpaySellRemoteRepository) bind MoonpaySellRepository::class

        factoryOf(::MoonpayWidgetUrlBuilder)
    }
}