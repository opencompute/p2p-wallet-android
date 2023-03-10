package org.p2p.wallet.newsend.ui.vialink

import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.newsend.ui.dialogs.FreeTransactionsDetailsBottomSheet
import org.p2p.wallet.newsend.ui.linkgeneration.SendLinkGenerationFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"
private const val ARG_INPUT_AMOUNT = "ARG_INPUT_AMOUNT"

private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class SendViaLinkFragment :
    BaseMvpFragment<SendViaLinkContract.View, SendViaLinkContract.Presenter>(R.layout.fragment_send_new),
    SendViaLinkContract.View {

    companion object {
        fun create(
            initialToken: Token.Active? = null,
            inputAmount: BigDecimal? = null
        ): SendViaLinkFragment = SendViaLinkFragment()
            .withArgs(
                ARG_INITIAL_TOKEN to initialToken,
                ARG_INPUT_AMOUNT to inputAmount
            )
    }

    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)
    private val inputAmount: BigDecimal? by args(ARG_INPUT_AMOUNT)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: SendViaLinkContract.Presenter by inject()

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialData(initialToken, inputAmount)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupToolbar()
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::updateInputAmount
            maxButtonClickListener = presenter::onMaxButtonClicked
            switchListener = presenter::switchCurrencyMode
            feeButtonClickListener = presenter::onFeeInfoClicked
            if (inputAmount == null) {
                focusAndShowKeyboard()
            }
        }
        binding.sliderSend.onSlideCompleteListener = { presenter.checkInternetConnection() }
        binding.sliderSend.onSlideCollapseCompleted = { presenter.generateLink() }

        binding.textViewDebug.isVisible = BuildConfig.DEBUG
        binding.textViewMessage.isVisible = true

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
        }
    }

    override fun showFreeTransactionsInfo() {
        FreeTransactionsDetailsBottomSheet.show(childFragmentManager)
    }

    override fun navigateToLinkGeneration(account: TemporaryAccount, token: Token.Active, lamports: BigInteger) {
        replaceFragment(SendLinkGenerationFragment.create(account, token, lamports))
    }

    override fun updateInputValue(textValue: String, forced: Boolean) {
        binding.widgetSendDetails.setInput(textValue, forced)
    }

    override fun updateInputFraction(newInputFractionLength: Int) {
        binding.widgetSendDetails.updateFractionLength(newInputFractionLength)
    }

    override fun showToken(token: Token.Active) {
        binding.widgetSendDetails.setToken(token)
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisible(isVisible)
    }

    override fun setInputEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setInputEnabled(isEnabled)
    }

    override fun setBottomButtonText(text: TextContainer?) {
        binding.buttonBottom withTextOrGone text?.getString(requireContext())
    }

    override fun setSliderText(text: String?) {
        if (text.isNullOrEmpty()) {
            binding.sliderSend.isVisible = !text.isNullOrEmpty()
        } else {
            binding.sliderSend.isVisible = true
            binding.sliderSend.setActionText(text)
        }
    }

    override fun disableInputs() {
        binding.widgetSendDetails.disableInputs()
    }

    override fun showAroundValue(value: String) {
        binding.widgetSendDetails.setAroundValue(value)
    }

    override fun setTokenContainerEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setTokenContainerEnabled(isEnabled = isEnabled)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showFeeLoading(isLoading)
    }

    override fun showDelayedFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showDelayedFeeViewLoading(isLoading)
    }

    override fun showFeeViewVisible(isVisible: Boolean) {
        binding.widgetSendDetails.showFeeVisible(isVisible = isVisible)
    }

    override fun setFeeLabel(text: String) {
        binding.widgetSendDetails.setFeeLabel(text)
    }

    override fun setSwitchLabel(symbol: String) {
        binding.widgetSendDetails.setSwitchLabel(getString(R.string.send_switch_to_token, symbol))
    }

    override fun setMainAmountLabel(symbol: String) {
        binding.widgetSendDetails.setMainAmountLabel(symbol)
    }

    override fun setInputColor(@ColorRes colorRes: Int) {
        binding.widgetSendDetails.setInputTextColor(colorRes)
    }

    override fun showDebugInfo(text: CharSequence) {
        binding.textViewDebug.text = text
    }

    override fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?) {
        addFragment(
            target = NewSelectTokenFragment.create(
                tokens = tokens,
                selectedToken = selectedToken,
                requestKey = KEY_REQUEST_SEND,
                resultKey = KEY_RESULT_TOKEN_TO_SEND
            ),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showSliderCompleteAnimation() {
        binding.sliderSend.showCompleteAnimation()
    }

    override fun restoreSlider() {
        binding.sliderSend.restoreSlider()
    }

    private fun UiKitToolbar.setupToolbar() {
        setTitle(R.string.send_via_link_title)
        setNavigationOnClickListener { popBackStack() }
    }
}