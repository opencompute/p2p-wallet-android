package com.p2p.wowlet.auth.interactor
import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.common.network.CallException
import com.p2p.wowlet.common.network.Constants
import com.p2p.wowlet.common.network.Result

class PinCodeVerificationInteractor(private val preferenceService: PreferenceService) {

    suspend fun verifyPinCode(pinCode: String): Result<Boolean> {
        preferenceService.getPinCodeValue()?.let {
            return if (it.pinCode == pinCode.toInt()) {
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } ?: run {
            return Result.Error(
                CallException(
                    Constants.PREFERENCE_SAVED_ERROR,
                    "In the storage not saved pin code"
                )
            )
        }
    }
}