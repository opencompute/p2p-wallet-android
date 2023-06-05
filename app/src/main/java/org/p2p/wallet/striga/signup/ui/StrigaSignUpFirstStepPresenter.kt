package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.PhoneMask
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor
) :
    BasePresenter<StrigaSignUpFirstStepContract.View>(dispatchers.ui),
    StrigaSignUpFirstStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var phoneMask: PhoneMask? = null
    private var countryOfBirth: Country? = null

    override fun firstAttach() {
        super.firstAttach()
        launch {
            setupPhoneMask()
            initialLoadSignupData()
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        setData(type, newValue)

        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onCountryChanged(newCountry: Country) {
        countryOfBirth = newCountry
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.name}",
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH
        )
        signupData[StrigaSignupDataType.COUNTRY_OF_BIRTH] = StrigaSignupData(
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH,
            value = newCountry.codeAlpha3
        )
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(selectedCountry = countryOfBirth)
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateFirstStep(signupData)

        if (isValid) {
            view?.navigateNext()
        } else {
            Timber.d("Validation failed: $states")
            view?.setErrors(states)
            // disable button is there are errors
            view?.setButtonIsEnabled(false)
            states.firstOrNull { !it.isValid }?.let {
                view?.scrollToFirstError(it.type)
            }
        }
    }

    override fun saveChanges() {
        mapDataForStorage()
        interactor.saveChanges(signupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupDataFirstStep().associateBy { it.type }

        // fill pre-saved values as-is
        data.values.forEach {
            setData(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }

        // db stores COUNTRY_OF_BIRTH as country name code ISO 3166-1 alpha-3,
        // so we need to find country by code and convert to country name
        countryOfBirth = interactor.findCountryByIsoAlpha3(
            data[StrigaSignupDataType.COUNTRY_OF_BIRTH]?.value.orEmpty()
        )
        countryOfBirth?.let { onCountryChanged(it) }
    }

    private fun mapDataForStorage() {
        val fullPhoneNumber = getFullPhoneNumber()
        if (fullPhoneNumber != null && phoneMask != null) {
            val phoneCodeWithSign = phoneMask?.phoneCodeWithSign ?: throw IllegalStateException("Phone mask is null")
            setData(
                StrigaSignupDataType.PHONE_CODE,
                phoneCodeWithSign
            )
            setData(
                StrigaSignupDataType.PHONE_NUMBER,
                removeCodeFromPhoneNumber(phoneCodeWithSign, fullPhoneNumber)
            )
        }
        setData(StrigaSignupDataType.COUNTRY_OF_BIRTH, countryOfBirth?.codeAlpha3.orEmpty())
    }

    private suspend fun setupPhoneMask() {
        val country = Country(
            name = "Turkey",
            flagEmoji = "",
            codeAlpha2 = "TR",
            codeAlpha3 = "TUR"
        ) // interactor.getSelectedCountry()

        phoneMask = interactor.findPhoneMaskByCountry(country)
        // todo: check that all countries have phone mask
        phoneMask?.let {
            val maskForFormatter = "+" + it.mask
            view?.setPhoneMask(maskForFormatter)
        }
    }

    private fun removeCodeFromPhoneNumber(phoneCode: String, phoneNumber: String): String {
        return phoneNumber.replace(phoneCode, "").replace(" ", "")
    }

    private fun getFullPhoneNumber(): String? {
        if (signupData[StrigaSignupDataType.PHONE_NUMBER]?.value.isNullOrBlank()) {
            return null
        }

        // phone is split by 2 parts: code and number
        return signupData[StrigaSignupDataType.PHONE_CODE]?.value.orEmpty() +
            signupData[StrigaSignupDataType.PHONE_NUMBER]?.value.orEmpty()
    }

    private fun setData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }
}