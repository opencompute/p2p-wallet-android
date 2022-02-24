package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveSolanaBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.renbtc.ui.main.RenBTCFragment
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.createBitmap
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast

class ReceiveSolanaFragment :
    BaseMvpFragment<ReceiveSolanaContract.View, ReceiveSolanaContract.Presenter>(R.layout.fragment_receive_solana),
    ReceiveSolanaContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create(token: Token?) = ReceiveSolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)
    override val presenter: ReceiveSolanaContract.Presenter by inject {
        parametersOf(token)
    }
    private val binding: FragmentReceiveSolanaBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenName.Receive.SOLANA)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            saveButton.setOnClickListener {
                val bitmap = qrView.createBitmap()
                presenter.saveQr(usernameTextView.text.toString(), bitmap)
            }
            networkView.setOnClickListener {
                presenter.onNetworkClicked()
            }
            faqTextView.setOnClickListener {
                analyticsInteractor.logScreenOpenEvent(ScreenName.Receive.LIST)
                replaceFragment(TokenListFragment.create())
            }
            setFragmentResultListener(REQUEST_KEY) { _, bundle ->
                val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
                if (type == NetworkType.BITCOIN) {
                    popAndReplaceFragment(RenBTCFragment.create())
                }
            }
        }
        presenter.loadData()
    }

    override fun showUserData(userPublicKey: String, username: Username?) {
        with(binding) {
            fullAddressTextView.text = userPublicKey.highlightPublicKey(requireContext())
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(userPublicKey)
                fullAddressTextView.setTextColor(getColor(R.color.backgroundButtonPrimary))
                Toast.makeText(requireContext(), R.string.main_receive_address_copied, Toast.LENGTH_SHORT).show()
            }
            shareButton.setOnClickListener {
                requireContext().shareText(userPublicKey)
                receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
            }
            copyButton.setOnClickListener {
                requireContext().copyToClipBoard(userPublicKey)
                toast(R.string.common_copied)
                receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
            }

            progressButton.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, userPublicKey)
                showUrlInCustomTabs(url)
            }

            if (username == null) return
            usernameTextView.isVisible = true

            val fullUsername = username.getFullUsername(requireContext())

            usernameTextView.text = fullUsername
            usernameTextView.isVisible = fullUsername.isNotEmpty()

            usernameTextView.setOnClickListener {
                requireContext().copyToClipBoard(fullUsername)
                usernameTextView.setTextColor(getColor(R.color.backgroundButtonPrimary))
                Toast.makeText(requireContext(), R.string.receive_username_copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun showReceiveToken(token: Token.Active) {
        binding.progressButton.setOnClickListener {
            presenter.onBrowserClicked(token.publicKey)
            val url = getString(R.string.solanaWalletExplorer, token.publicKey)
        }
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        with(binding) {
            qrImageView.setImageBitmap(qrBitmap)
        }
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.qrImageView.isInvisible = isLoading
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showNetwork() {
        replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.SOLANA, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE))
    }

    override fun showBrowser(url: String) {
        showUrlInCustomTabs(url)
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}