package org.p2p.wallet.svl.ui.linkgeneration

import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendLinkGenerationBinding
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.analytics.SendViaLinkAnalytics
import org.p2p.wallet.svl.ui.linkresult.SendLinkGenerationResultFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TEMPORARY_ACCOUNT = "EXTRA_TEMPORARY_ACCOUNT"
private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_LAMPORTS = "EXTRA_LAMPORTS"
private const val EXTRA_SIMULATION = "EXTRA_SIMULATION"
private const val EXTRA_CURRENCY_MODE = "EXTRA_CURRENCY_MODE"

class SendLinkGenerationFragment :
    BaseMvpFragment<SendLinkGenerationContract.View, SendLinkGenerationContract.Presenter>(
        R.layout.fragment_send_link_generation
    ),
    SendLinkGenerationContract.View {

    companion object {
        fun create(
            recipient: TemporaryAccount,
            token: Token.Active,
            lamports: BigInteger,
            isSimulation: Boolean,
            currencyModeSymbol: String
        ): Fragment = SendLinkGenerationFragment()
            .withArgs(
                EXTRA_TEMPORARY_ACCOUNT to recipient,
                EXTRA_TOKEN to token,
                EXTRA_LAMPORTS to lamports,
                EXTRA_SIMULATION to isSimulation,
                EXTRA_CURRENCY_MODE to currencyModeSymbol
            )
    }

    override val presenter: SendLinkGenerationContract.Presenter by inject()
    private val svlAnalytics: SendViaLinkAnalytics by inject()

    private val binding: FragmentSendLinkGenerationBinding by viewBinding()

    private val recipient: TemporaryAccount by args(EXTRA_TEMPORARY_ACCOUNT)
    private val token: Token.Active by args(EXTRA_TOKEN)
    private val lamports: BigInteger by args(EXTRA_LAMPORTS)
    private val isSimulation: Boolean by args(EXTRA_SIMULATION)
    private val currencyModeSymbol: String by args(EXTRA_CURRENCY_MODE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        svlAnalytics.logLinkGenerationOpened()
        presenter.generateLink(recipient, token, lamports, isSimulation, currencyModeSymbol)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // do nothing
        }
    }

    override fun showResult(state: LinkGenerationState) {
        popAndReplaceFragment(target = SendLinkGenerationResultFragment.create(state))
    }
}
