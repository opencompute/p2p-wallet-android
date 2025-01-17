package org.p2p.wallet.auth.ui.generalerror.timer

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.io.File

interface OnboardingGeneralErrorTimerContract {
    interface View : MvpView {
        fun updateText(
            @StringRes titleRes: Int,
            @StringRes subTitleRes: Int,
            formattedTimeLeft: String
        )

        fun navigateToStartingScreen()
        fun showFile(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun onTermsClick()
        fun onPolicyClick()
    }
}
