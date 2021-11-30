package org.p2p.wallet.auth.ui.username

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber

class ReserveUsernamePresenter(
    private val context: Context,
    private val interactor: UsernameInteractor,
    private val tokenProvider: TokenKeyProvider,
    private val fileRepository: FileRepository
) : BasePresenter<ReserveUsernameContract.View>(),
    ReserveUsernameContract.Presenter {

    private var checkUsernameJob: Job? = null

    override fun checkUsername(username: String) {
        checkUsernameJob?.cancel()

        if (username.isEmpty()) {
            view?.showIdleState()
            return
        }

        checkUsernameJob = launch {
            try {
                /*
                * We should check the availability of the latest entered value by the user
                * therefore we cancel old request if new value is entered and waiting for the latest response only
                * */
                delay(300)
                view?.showUsernameLoading(true)
                interactor.checkUsername(username)
                view?.showUnavailableName(username)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled request for checking username: $username")
            } catch (e: Throwable) {
                view?.showAvailableName(username)
                Timber.e(e, "Error occurred while checking username: $username")
            } finally {
                view?.showUsernameLoading(false)
            }
        }
    }

    override fun checkCaptcha() {
        launch {
            try {
                val params = interactor.checkCaptcha()
                view?.showCaptcha(params)
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while checking captcha")
                view?.showCaptchaFailed()
                view?.showErrorMessage(e)
            }
        }
    }

    override fun registerUsername(username: String, result: String) {
        view?.showLoading(true)
        launch {
            try {
                interactor.registerUsername(username, result)
                interactor.lookupUsername(tokenProvider.publicKey)
                view?.successRegisterName()
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while registering username")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun openTermsOfUse() {
        val inputStream = context.assets.open("p2p_terms_of_service.pdf")
        val file = fileRepository.savePdf("p2p_terms_of_service", inputStream.readBytes())
        view?.showFile(file)
    }

    override fun openPrivacyPolicy() {
        val inputStream = context.assets.open("p2p_privacy_policy.pdf")
        val file = fileRepository.savePdf("p2p_privacy_policy", inputStream.readBytes())
        view?.showFile(file)
    }
}