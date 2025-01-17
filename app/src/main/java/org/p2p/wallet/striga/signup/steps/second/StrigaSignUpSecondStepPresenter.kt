package org.p2p.wallet.striga.signup.steps.second

import com.sumsub.sns.core.data.model.SNSSDKState
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.alarmlogger.model.StrigaAlarmError
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.signup.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val onboardingInteractor: StrigaOnboardingInteractor,
    private val strigaItemCellMapper: StrigaItemCellMapper,
    private val alarmErrorsLogger: AlarmErrorsLogger
) : BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    private val cachedSignupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var selectedCountry: CountryCode? = null
    private var selectedOccupation: StrigaOccupation? = null
    private var selectedFunds: StrigaSourceOfFunds? = null

    private var isSubmittedFirstTime = false

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
        }
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupDataSecondStep()
        data.forEach { (type, value) ->
            setCachedData(type, value.orEmpty())
            view?.updateSignupField(type, value.orEmpty())
        }

        cachedSignupData[StrigaSignupDataType.OCCUPATION]?.value?.let {
            onboardingInteractor.getOccupationByName(it)
                ?.also(::onOccupationChanged)
        }
        cachedSignupData[StrigaSignupDataType.SOURCE_OF_FUNDS]?.value?.let {
            onboardingInteractor.getSourcesOfFundsByName(it)
                ?.also(::onSourceOfFundsChanged)
        }
        cachedSignupData[StrigaSignupDataType.COUNTRY_ALPHA_2]?.value?.let {
            interactor.findCountryByIsoAlpha2(it)
                ?.also(::onCountryChanged)
        }
    }

    override fun onFieldChanged(type: StrigaSignupDataType, newValue: String) {
        Timber.i("field $type changed with new value of size: ${newValue.length}")

        val isPresetDataChanged = type in setOf(
            StrigaSignupDataType.COUNTRY_ALPHA_2,
            StrigaSignupDataType.OCCUPATION,
            StrigaSignupDataType.SOURCE_OF_FUNDS
        )
        if (!isPresetDataChanged) {
            setCachedData(type, newValue)
        }

        Timber.i("isSubmittedFirstTime = $isSubmittedFirstTime")
        if (isSubmittedFirstTime) {
            val validationResult = interactor.validateField(type, newValue)
            Timber.i("validation result for field $type = ${validationResult.isValid}")
            if (validationResult.isValid) {
                view?.clearError(type)
            } else {
                view?.setErrors(listOf(validationResult))
            }
        }
        view?.setButtonIsEnabled(true)
    }

    override fun onPresetDataChanged(selectedItem: StrigaPresetDataItem) {
        when (selectedItem) {
            is StrigaPresetDataItem.Country -> onCountryChanged(selectedItem.details ?: return)
            is StrigaPresetDataItem.Occupation -> onOccupationChanged(selectedItem.details ?: return)
            is StrigaPresetDataItem.SourceOfFunds -> onSourceOfFundsChanged(selectedItem.details ?: return)
        }
    }

    override fun onOccupationClicked() {
        view?.showOccupationPicker(selectedOccupation)
    }

    override fun onFundsClicked() {
        view?.showSourceOfFundsPicker(selectedFunds)
    }

    override fun onCountryClicked() {
        view?.showCurrentCountryPicker(selectedCountry)
    }

    private fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds) {
        selectedFunds = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.sourceName),
            type = StrigaSignupDataType.SOURCE_OF_FUNDS
        )
        setCachedData(StrigaSignupDataType.SOURCE_OF_FUNDS, newValue.sourceName)
    }

    private fun onOccupationChanged(newValue: StrigaOccupation) {
        selectedOccupation = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitleWithEmoji(newValue.emoji, newValue.occupationName),
            type = StrigaSignupDataType.OCCUPATION
        )
        setCachedData(StrigaSignupDataType.OCCUPATION, newValue.occupationName)
    }

    private fun onCountryChanged(newValue: CountryCode) {
        selectedCountry = newValue
        view?.updateSignupField(
            newValue = "${newValue.flagEmoji} ${newValue.countryName}",
            type = StrigaSignupDataType.COUNTRY_ALPHA_2
        )
        setCachedData(StrigaSignupDataType.COUNTRY_ALPHA_2, newValue.nameCodeAlpha2)
    }

    override fun onSubmit() {
        if (!isSubmittedFirstTime) {
            isSubmittedFirstTime = true
        }
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(cachedSignupData)

        if (isValid) {
            view?.setProgressIsVisible(isVisible = true)
            launch {
                try {
                    mapDataForStorage()
                    interactor.saveChangesNow(cachedSignupData.values)
                    interactor.createUser()
                    view?.navigateNext()
                } catch (e: StrigaDataLayerError.ApiServiceError.PhoneNumberAlreadyUsed) {
                    view?.navigateToPhoneError()
                } catch (e: Throwable) {
                    Timber.e(e, "Unable to create striga user")
                    view?.showUiKitSnackBar(e.message, R.string.error_general_message)
                    logAlarmError(e)
                } finally {
                    view?.setProgressIsVisible(isVisible = false)
                }
            }
        } else {
            view?.setErrors(states)
            // disable button is there are errors
            view?.setButtonIsEnabled(isEnabled = false)
            states.firstOrNull { !it.isValid }?.let {
                view?.scrollToFirstError(it.type)
            }
        }
    }

    private fun logAlarmError(e: Throwable) {
        val error = StrigaAlarmError(
            source = "other",
            kycSdkState = SNSSDKState.Initial.toString(),
            error = e
        )
        alarmErrorsLogger.triggerStrigaAlarm(error)
    }

    private fun mapDataForStorage() {
        cachedSignupData[StrigaSignupDataType.OCCUPATION]?.value?.let {
            setCachedData(
                type = StrigaSignupDataType.OCCUPATION,
                value = strigaItemCellMapper.fromUiTitle(it.uppercase())
            )
        }
        cachedSignupData[StrigaSignupDataType.SOURCE_OF_FUNDS]?.value?.let {
            // convert UI string into STRIGA_FORMAT
            setCachedData(
                type = StrigaSignupDataType.SOURCE_OF_FUNDS,
                value = strigaItemCellMapper.fromUiTitle(it)
            )
        }
    }

    override fun saveChanges() {
        mapDataForStorage()
        interactor.saveChanges(cachedSignupData.values)
    }

    private fun setCachedData(type: StrigaSignupDataType, value: String) {
        cachedSignupData[type] = StrigaSignupData(type, value)
    }
}
