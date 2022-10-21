package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse.ErrorType
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse

class UserSignUpInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository
) {

    sealed interface SignUpResult {
        object UserAlreadyExists : SignUpResult
        object SignUpSuccessful : SignUpResult
        class SignUpFailed(override val cause: Throwable) : Error(), SignUpResult
    }

    suspend fun trySignUpNewUser(socialShareUserId: String): SignUpResult {
        return try {
            val signUpResponse: Web3AuthSignUpResponse = generateDeviceAndThirdShare()
            signUpFlowDataRepository.generateUserAccount(userMnemonicPhrase = signUpResponse.mnemonicPhraseWords)
            userSignUpDetailsStorage.save(signUpResponse, socialShareUserId)

            SignUpResult.SignUpSuccessful
        } catch (web3AuthError: Web3AuthErrorResponse) {
            if (web3AuthError.errorType == ErrorType.CANNOT_RECONSTRUCT) {
                SignUpResult.UserAlreadyExists
            } else {
                SignUpResult.SignUpFailed(web3AuthError)
            }
        } catch (error: Throwable) {
            SignUpResult.SignUpFailed(error)
        }
    }

    fun continueSignUpUser(): SignUpResult {
        return try {
            val lastUserDetails = userSignUpDetailsStorage.getLastSignUpUserDetails()
                ?: throw NullPointerException("Last sign up user details (aka device share) not found")

            signUpFlowDataRepository.signUpUserId = lastUserDetails.userId
            signUpFlowDataRepository.generateUserAccount(
                userMnemonicPhrase = lastUserDetails.signUpDetails.mnemonicPhraseWords
            )
            SignUpResult.SignUpSuccessful
        } catch (error: Throwable) {
            SignUpResult.SignUpFailed(error)
        }
    }

    private suspend fun generateDeviceAndThirdShare(): Web3AuthSignUpResponse {
        val torusKey = signUpFlowDataRepository.torusKey
        if (torusKey.isNullOrEmpty()) {
            error("Torus key is null")
        } else {
            return web3AuthApi.triggerSilentSignUp(torusKey)
        }
    }
}