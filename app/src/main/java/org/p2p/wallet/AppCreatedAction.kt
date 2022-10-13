package org.p2p.wallet

import org.koin.core.component.KoinComponent
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.remote_config.AppFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.solend.repository.SolendConfigurationRepository
import timber.log.Timber
import kotlinx.coroutines.launch

/**
 * Entity to perform some actions / loading needed when app is created
 */
class AppCreatedAction(
    private val pushTokenRepository: PushTokenRepository,
    private val remoteConfig: AppFirebaseRemoteConfig,
    private val solendConfigRepository: SolendConfigurationRepository,
    private val solendFeatureToggle: SolendEnabledFeatureToggle,
    private val appScope: AppScope
) : KoinComponent {

    operator fun invoke() {
        if (BuildConfig.DEBUG) {
            logFirebaseDevicePushToken()
        }

        remoteConfig.loadRemoteConfig(onConfigLoaded = {
            if (solendFeatureToggle.isFeatureEnabled) {
                initSolend()
            }
        })
    }

    private fun initSolend() {
        appScope.launch {
            try {
                solendConfigRepository.loadSolendConfiguration()
            } catch (error: Throwable) {
                Timber.e(error, "Failed to init Solend when app is created")
            }
        }
    }

    private fun logFirebaseDevicePushToken() {
        appScope.launch {
            kotlin.runCatching { pushTokenRepository.getPushToken().value }
                .onSuccess { Timber.tag("App:device_token").d(it) }
                .onFailure { Timber.e(it) }
        }
    }
}