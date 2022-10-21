package org.p2p.wallet.auth.ui.phone

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

private const val MAX_PHONE_NUMBER_TRIES = 5
private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val gatewayServiceErrorHandler: GatewayServiceErrorHandler
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    override fun attach(view: PhoneNumberEnterContract.View) {
        super.attach(view)

        when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> view.initCreateWalletViews()
            is OnboardingFlow.RestoreWallet -> view.initRestoreWalletViews()
        }

        selectedCountryCode?.let { countryCode ->
            view.showDefaultCountryCode(countryCode)
        } ?: launch { loadDefaultCountryCode() }
    }

    private suspend fun loadDefaultCountryCode() {
        try {
            val countryCode: CountryCode? =
                countryCodeInteractor.detectCountryCodeBySimCard()
                    ?: countryCodeInteractor.detectCountryCodeByNetwork()
                    ?: countryCodeInteractor.detectCountryCodeByLocale()

            selectedCountryCode = countryCode

            view?.showDefaultCountryCode(countryCode)
        } catch (e: Throwable) {
            Timber.e(e, "Loading default country code failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        launch {
            selectedCountryCode = countryCodeInteractor.findCountryForPhoneCode(newCountryCode)
            view?.update(selectedCountryCode)
        }
    }

    override fun onPhoneChanged(phoneNumber: String) {
        selectedCountryCode?.let {
            val isValidNumber = countryCodeInteractor.isValidNumberForRegion(it.phoneCode, phoneNumber)
            val newButtonState = if (isValidNumber) {
                PhoneNumberScreenContinueButtonState.ENABLED_TO_CONTINUE
            } else {
                PhoneNumberScreenContinueButtonState.DISABLED_INPUT_IS_EMPTY
            }
            view?.setContinueButtonState(newButtonState)
        }
    }

    override fun onCountryCodeChanged(newCountry: CountryCode) {
        selectedCountryCode = newCountry
        view?.update(selectedCountryCode)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
    }

    override fun submitUserPhoneNumber(phoneNumberString: String) {
        launch {
            view?.setLoadingState(isLoading = true)
            val userPhoneNumber = PhoneNumber(selectedCountryCode?.phoneCode + phoneNumberString)
            onboardingInteractor.temporaryPhoneNumber = userPhoneNumber
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> startCreatingWallet(userPhoneNumber)
                is OnboardingFlow.RestoreWallet -> startRestoringCustomShare(userPhoneNumber)
            }
            view?.setLoadingState(isLoading = false)
        }
    }

    private suspend fun startCreatingWallet(phoneNumber: PhoneNumber) {
        try {
            selectedCountryCode?.let {
                if (createWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    createWalletInteractor.resetUserEnterPhoneNumberTriesCount()
                    view?.navigateToAccountBlocked(DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds)
                } else {
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = phoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (error: Throwable) {
            if (error is GatewayServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            createWalletInteractor.setIsCreateWalletRequestSent(isSent = false)
        }
    }

    private fun handleGatewayServiceError(gatewayServiceError: GatewayServiceError) {
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(gatewayServiceError)) {
            is GatewayHandledState.CriticalError -> {
                view?.navigateToCriticalErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.navigateToAccountBlocked(gatewayHandledResult.cooldownTtl)
            }
            is GatewayHandledState.TitleSubtitleError -> {
                view?.navigateToCriticalErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.ToastError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.message)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: PhoneNumber) {
        try {
            selectedCountryCode?.let {
                if (restoreWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    restoreWalletInteractor.resetUserEnterPhoneNumberTriesCount()
                    view?.navigateToAccountBlocked(DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds)
                } else {
                    restoreWalletInteractor.startRestoreCustomShare(userPhoneNumber = phoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (error: Throwable) {
            if (error is GatewayServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            restoreWalletInteractor.setIsRestoreWalletRequestSent(isSent = false)
        }
    }
}