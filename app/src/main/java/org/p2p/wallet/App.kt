package org.p2p.wallet

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.history.HistoryModule
import org.p2p.wallet.infrastructure.InfrastructureModule
import org.p2p.wallet.infrastructure.network.NetworkModule
import org.p2p.wallet.main.MainModule
import org.p2p.wallet.qr.QrModule
import org.p2p.wallet.renbtc.di.RenBtcModule
import org.p2p.wallet.restore.BackupModule
import org.p2p.wallet.root.RootModule
import org.p2p.wallet.root.ui.RootActivity
import org.p2p.wallet.rpc.RpcModule
import org.p2p.wallet.settings.SettingsModule
import org.p2p.wallet.settings.interactor.ThemeInteractor
import org.p2p.wallet.swap.SwapModule
import org.p2p.wallet.user.UserModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.KoinContextHandler
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        setupKoin()
        AndroidThreeTen.init(this)
        DebugDrawer.init(this)
        KoinContextHandler.get().get<ThemeInteractor>().applyCurrentNightMode()
    }

    private fun setupKoin() {
        KoinContextHandler.stop()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    AuthModule.create(),
                    RootModule.create(),
                    BackupModule.create(),
                    UserModule.create(),
                    MainModule.create(),
                    RenBtcModule.create(),
                    NetworkModule.create(),
                    QrModule.create(),
                    HistoryModule.create(),
                    SettingsModule.create(),
                    SwapModule.create(),
                    RpcModule.create(),
                    InfrastructureModule.create(),
                    createAppModule()
                )
            )
        }
    }

    private fun createAppModule(): Module = module {
        single {
            AppRestarter {
                restart()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        } bind AppRestarter::class
    }

    private fun restart() {
        setupKoin()
        RootActivity
            .createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let { startActivity(it) }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}