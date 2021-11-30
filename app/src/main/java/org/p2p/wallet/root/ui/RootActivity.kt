package org.p2p.wallet.root.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.common.mvp.BaseMvpActivity
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.infrastructure.navigation.NavigationScreenTracker
import org.p2p.wallet.utils.edgetoedge.applyTranslucentFlag
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    companion object {
        fun createIntent(context: Context) = Intent(context, RootActivity::class.java)
    }

    override val presenter: RootContract.Presenter by inject()

    private val screenTracker: NavigationScreenTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        window.applyTranslucentFlag()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }

        presenter.loadPricesAndBids()
        if (BuildConfig.DEBUG) DebugDrawer.install(this)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.content)
            val screenName = fragment?.javaClass?.name.orEmpty()
            screenTracker.setCurrentScreen(screenName)
        }
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingFragment())
    }

    override fun navigateToSignIn() {
        replaceFragment(SignInPinFragment.create())
    }

    override fun onBackPressed() {
        popBackStack()
    }
}