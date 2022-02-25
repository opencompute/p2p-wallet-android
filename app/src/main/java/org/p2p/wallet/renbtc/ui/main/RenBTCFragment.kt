package org.p2p.wallet.renbtc.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRenBtcBinding
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding

class RenBTCFragment :
    BaseMvpFragment<RenBTCContract.View, RenBTCContract.Presenter>(R.layout.fragment_ren_btc),
    RenBTCContract.View {

    companion object {
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create() = RenBTCFragment()
    }

    override val presenter: RenBTCContract.Presenter by inject()
    private val binding: FragmentRenBtcBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenName.Receive.BITCOIN)
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            statusView.setOnClickListener {
                presenter.onStatusReceivedClicked()
            }
            networkView.setOnClickListener {
                presenter.onNetworkClicked()
            }
            qrView.setWatermarkIcon(R.drawable.ic_btc)
            qrView.onSaveClickListener = { name, bitmap ->
                presenter.saveQr(name, bitmap)
            }

            setFragmentResultListener(ReceiveNetworkTypeFragment.REQUEST_KEY) { _, bundle ->
            setFragmentResultListener(REQUEST_KEY) { _, bundle ->
                val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
                if (type == NetworkType.SOLANA) {
                    popAndReplaceFragment(ReceiveSolanaFragment.create(null))
                }
            }
        }

        presenter.subscribe()
        presenter.checkActiveSession(requireContext())
        presenter.startNewSession(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.cancelTimer()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.qrView.setImage(qrBitmap)
        }
    }

    override fun showActiveState(address: String, remaining: String, fee: String) {
        with(binding) {
            qrView.setValue(address.highlightPublicKey(requireContext()))

            progressButton.setOnClickListener {
                presenter.onBrowserClicked(address)
            }

            val infoText = getString(R.string.receive_session_info)
            val onlyBitcoin = getString(R.string.receive_only_bitcoin)
            sessionInfoTextView.text = SpanUtils.setTextBold(infoText, onlyBitcoin)

            val btcText = getString(R.string.common_btc)
            val amountText = getString(R.string.receive_session_min_transaction, fee)
            amountInfoTextView.text = SpanUtils.setTextBold(amountText, fee, btcText)
        }
    }

    override fun updateTimer(remaining: String) {
        val text = getString(R.string.receive_session_timer_info, remaining)
        binding.timerTextView.text = SpanUtils.setTextBold(text, remaining)
        binding.timerTextView.isVisible = true
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
        binding.qrView.showLoading(isLoading)
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showTransactionsCount(count: Int) {
        binding.statusCountTextView.text = count.toString()
        binding.statusView.isEnabled = count != 0
    }

    override fun navigateToSolana() {
        popAndReplaceFragment(ReceiveSolanaFragment.create(null))
    }

    override fun showNetwork() {
        replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.BITCOIN, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE))
    }

    override fun showBrowser(url: String) {
        showUrlInCustomTabs(url)
    }

    override fun showStatuses() {
        replaceFragment(RenTransactionsFragment.create())
    }
}