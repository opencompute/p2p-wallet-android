package org.p2p.wallet.auth.ui.onboarding.root

import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingFragment
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.replaceFragment

class OnboardingRootFragment :
    BaseMvpFragment<OnboardingRootContract.View, OnboardingRootContract.Presenter>(R.layout.fragment_root_onboarding),
    OnboardingRootContract.View {

    companion object {
        fun create(): OnboardingRootFragment = OnboardingRootFragment()
    }

    override val presenter: OnboardingRootContract.Presenter by inject { parametersOf(this) }

    override fun navigateToOnboarding() {
        replaceFragment(PhoneNumberEnterFragment.create())
        return

        replaceFragment(
            target = NewOnboardingFragment.create(),
            containerId = R.id.onboardingRootContainer,
            fragmentManager = childFragmentManager,
            addToBackStack = false
        )
    }

    override fun navigateToContinueOnboarding() {
        replaceFragment(
            target = ContinueOnboardingFragment.create(),
            containerId = R.id.onboardingRootContainer,
            fragmentManager = childFragmentManager,
            addToBackStack = false
        )
    }
}
