package org.p2p.wallet.settings.ui.recovery

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.common.AppRestarter
import android.content.res.Resources
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.security.SecureStorageContract

class RecoveryKitPresenter(
    private val secureStorage: SecureStorageContract,
    private val resources: Resources,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val adminAnalytics: AdminAnalytics,
    private val appRestarter: AppRestarter,
    private val authLogoutInteractor: AuthLogoutInteractor
) : BasePresenter<RecoveryKitContract.View>(),
    RecoveryKitContract.Presenter {

    private var seedPhraseProviderType: SeedPhraseSource? = null
    private val seedPhrase = mutableListOf<SeedPhraseWord>()

    override fun onSeedPhraseClicked() {
        if (seedPhraseProvider.isAvailable) {
            view?.showSeedPhraseLockFragment()
        } else {
            view?.showLogoutInfoDialog()
        }
    }

    override fun attach(view: RecoveryKitContract.View) {
        super.attach(view)
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase()
        seedPhraseProviderType = userSeedPhrase.provider
        seedPhrase.clear()
        seedPhrase.addAll(
            userSeedPhrase.seedPhrase.map {
                SeedPhraseWord(
                    text = it,
                    isValid = true,
                    isBlurred = false
                )
            }
        )
        if (seedPhraseProviderType != SeedPhraseSource.MANUAL) {
            fetchMetadata()
        }
    }

    private fun setUnavailableState() {
        val notAvailableString = resources.getString(R.string.recovery_not_available)
        view?.apply {
            showDeviceName(notAvailableString)
            showPhoneNumber(notAvailableString)
            showSocialId(notAvailableString)
        }
    }

    private fun fetchMetadata() {
        secureStorage.getObject(
            SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
            GatewayOnboardingMetadata::class
        )?.let { metadata ->
            view?.showDeviceName(metadata.deviceShareDeviceName)
            view?.showPhoneNumber(metadata.customSharePhoneNumberE164)
            view?.showSocialId(metadata.socialShareOwnerEmail)
        } ?: setUnavailableState()

        view?.setWebAuthInfoVisibility(isVisible = true)
    }

    override fun logout() {
        authLogoutInteractor.onUserLogout()
        adminAnalytics.logSignedOut()
        appRestarter.restartApp()
    }
}
