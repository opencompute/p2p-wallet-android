package org.p2p.wallet.smsinput.onboarding

import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenFragment
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class OnboardingSmsInputFragment : BaseSmsInputFragment() {

    override fun onBackPressed() {
        super.onBackPressed()
        popBackStack()
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.checkNumberTitleText.text =
            getString(R.string.onboarding_sms_input_phone_number_title, userPhoneNumber.formattedValue)
    }

    override fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError, timerLeftTime: Long) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(error, timerLeftTime)
        )
    }

    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) {
        popAndReplaceFragment(OnboardingGeneralErrorFragment.create(handledState))
    }

    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(RestoreErrorScreenFragment.create(handledState))
    }
}