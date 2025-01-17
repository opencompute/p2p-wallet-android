package org.p2p.wallet.home.ui.topup

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class TopUpWalletPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val metadataInteractor: MetadataInteractor,
    private val inAppFeatureFlags: InAppFeatureFlags,
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.isWeb3AuthUser || appFeatureFlags.strigaSimulateWeb3Flag.featureValue

    private val strigaBankTransferProgress = MutableStateFlow(false)

    override fun attach(view: TopUpWalletContract.View) {
        super.attach(view)

        if (strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3) {
            strigaBankTransferProgress.onEach(view::showStrigaBankTransferView)
                .launchIn(this)
        } else {
            view.showStrigaBankTransferView(showProgress = false)
        }

        launch {
            val tokenToBuy = userInteractor.getSingleTokenForBuy()
            tokenToBuy?.let(view::showBankCardView) ?: view.hideBankCardView()
        }

        view.showCryptoReceiveView()
    }

    override fun onBankTransferClicked() {
        if (!strigaSignupFeatureToggle.isFeatureEnabled) {
            launch {
                val tokenToBuy = userInteractor.getSingleTokenForBuy()
                tokenToBuy?.let {
                    view?.navigateToBuyWithTransfer(it)
                }
            }
            return
        }
        // in case of simulation web3 user, we don't need to check metadata
        if (inAppFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
            view?.navigateToBankTransferTarget(StrigaUserStatusDestination.ONBOARDING)
            return
        }

        launch {
            try {
                strigaBankTransferProgress.emit(true)

                ensureNeededStrigaDataLoaded()

                val strigaDestination = strigaUserInteractor.getUserDestination()

                when {
                    strigaDestination == StrigaUserStatusDestination.IBAN_ACCOUNT &&
                        strigaUserInteractor.isKycApproved -> {
                        // prefetch account details for IBAN
                        strigaWalletInteractor.getFiatAccountDetails()
                        // prefetch crypto account details for future use
                        strigaWalletInteractor.getCryptoAccountDetails()
                    }
                    strigaDestination == StrigaUserStatusDestination.KYC_PENDING -> {
                        view?.navigateToKycPending()
                        return@launch
                    }
                }

                view?.navigateToBankTransferTarget(strigaDestination)
            } catch (strigaDataLoadFailed: Throwable) {
                Timber.e(strigaDataLoadFailed, "failed to load needed data for bank transfer")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                strigaBankTransferProgress.emit(false)
            }
        }
    }

    private suspend fun ensureNeededStrigaDataLoaded() {
        if (metadataInteractor.currentMetadata == null) {
            Timber.i("Metadata is not fetched. Trying again...")
            loadUserMetadata()
        }

        if (strigaUserInteractor.isUserCreated()) {
            if (strigaUserInteractor.isUserVerificationStatusLoaded() && !strigaUserInteractor.isKycApproved) {
                Timber.i("Striga user status is not fetched. Trying again...")
                loadStrigaUserStatus()
            }
            if (!strigaUserInteractor.isUserDetailsLoaded()) {
                Timber.i("Striga user signup data is not fetched. Trying again...")
                loadStrigaUserDetails()
            }
        }
    }

    private suspend fun loadUserMetadata() {
        metadataInteractor.tryLoadAndSaveMetadata().throwIfFailure()
    }

    private suspend fun loadStrigaUserStatus() {
        strigaUserInteractor.loadAndSaveUserStatusData().unwrap()
    }

    private suspend fun loadStrigaUserDetails() {
        strigaSignupInteractor.loadAndSaveSignupData().unwrap()
    }
}
