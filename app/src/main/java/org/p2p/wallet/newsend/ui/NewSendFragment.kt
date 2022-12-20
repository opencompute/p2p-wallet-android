package org.p2p.wallet.newsend.ui

import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet.Companion.ARG_RESULT_KEY
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.newsend.ui.dialogs.FreeTransactionsDetailsBottomSheet
import org.p2p.wallet.newsend.ui.dialogs.SendTransactionsDetailsBottomSheet
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.newsend.ui.stub.SendNoAccountFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.CUT_SEVEN_SYMBOLS
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_RECIPIENT = "ARG_RECIPIENT"
private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"

private const val KEY_RESULT_NEW_FEE_PAYER = "KEY_RESULT_APPROXIMATE_FEE_USD"
private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
        fun create(recipient: SearchResult, initialToken: Token.Active? = null) =
            NewSendFragment()
                .withArgs(
                    ARG_RECIPIENT to recipient,
                    ARG_INITIAL_TOKEN to initialToken
                )
    }

    private val recipient: SearchResult by args(ARG_RECIPIENT)
    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject {
        parametersOf(recipient)
    }
    override val navBarColor: Int = R.color.smoke
    override val statusBarColor: Int = R.color.smoke

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialToken(initialToken)
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
            focusAndShowKeyboard()
            setTokenContainerEnabled(isEnabled = initialToken == null)
        }
        binding.sliderSend.onSlideCompleteListener = {
            presenter.send()
        }

        binding.textViewDebug.isVisible = BuildConfig.DEBUG

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(ARG_RESULT_KEY) -> {
                    val fee = result.getParcelable<SendSolanaFee>(ARG_RESULT_KEY)
                    fee?.let { presenter.onAccountCreationFeeClicked(fee) }
                }
            }
        }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
            result.containsKey(KEY_RESULT_NEW_FEE_PAYER) -> {
                val newFeePayer = result.getParcelable<Token.Active>(KEY_RESULT_NEW_FEE_PAYER)!!
                presenter.updateFeePayerToken(newFeePayer)
            }
        }
    }

    override fun showTransactionDetails(sendFeeTotal: SendFeeTotal) {
        SendTransactionsDetailsBottomSheet.show(childFragmentManager, sendFeeTotal, KEY_REQUEST_SEND, ARG_RESULT_KEY)
    }

    override fun showAccountCreationFeeInfo(tokenSymbol: String, amountInUsd: String, hasAlternativeToken: Boolean) {
        val target = SendNoAccountFragment.create(
            tokenSymbol = tokenSymbol,
            approximateFeeUsd = amountInUsd,
            hasAlternativeFeePayerToken = hasAlternativeToken,
            requestKey = KEY_REQUEST_SEND,
            resultKey = KEY_RESULT_NEW_FEE_PAYER
        )
        replaceFragment(target)
    }

    override fun showFreeTransactionsInfo() {
        FreeTransactionsDetailsBottomSheet.show(childFragmentManager)
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

    override fun showAroundValue(value: String) {
        binding.widgetSendDetails.setAroundValue(value)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showFeeLoading(isLoading)
    }

    override fun setFeeLabel(text: String?) {
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

    override fun showProgressDialog(internalTransactionId: String, data: NewShowProgress) {
        listener?.showTransactionProgress(internalTransactionId, data)
        popBackStackTo(target = NewSearchFragment::class, inclusive = true)
    }

    private fun UiKitToolbar.setupToolbar() {
        title = (recipient as? SearchResult.UsernameFound)?.username
            ?: recipient.addressState.address.cutMiddle(CUT_SEVEN_SYMBOLS)
        setNavigationOnClickListener { popBackStack() }
    }
}