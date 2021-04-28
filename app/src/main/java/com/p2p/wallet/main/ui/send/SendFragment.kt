package com.p2p.wallet.main.ui.send

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import com.bumptech.glide.Glide
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.databinding.FragmentSendBinding
import com.p2p.wallet.main.ui.select.SelectTokenFragment
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import java.math.BigDecimal

class SendFragment :
    BaseMvpFragment<SendContract.View, SendContract.Presenter>(R.layout.fragment_send),
    SendContract.View {

    companion object {
        fun create() = SendFragment()
    }

    override val presenter: SendContract.Presenter by inject()

    private val binding: FragmentSendBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sendButton.setOnClickListener {
                val address = addressEditText.text.toString()
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: return@setOnClickListener
                presenter.sendToken(address, amount)
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            SelectTokenFragment.REQUEST_KEY,
            viewLifecycleOwner,
            FragmentResultListener { _, result ->
                if (!result.containsKey(SelectTokenFragment.EXTRA_TOKEN)) return@FragmentResultListener
                val token = result.getParcelable<Token>(SelectTokenFragment.EXTRA_TOKEN)
                if (token != null) presenter.setSourceToken(token)
            }
        )

        presenter.loadInitialData()
    }

    override fun navigateToTokenSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens),
            enter = R.anim.slide_up,
            exit = R.anim.slide_down,
            popExit = 0,
            popEnter = 0
        )
    }

    override fun showSuccess() {

    }

    @SuppressLint("SetTextI18n")
    override fun showSourceToken(token: Token) {
        with(binding) {
            Glide.with(sourceImageView).load(token.iconUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            tokenTextView.text = token.tokenSymbol
            singleValueTextView.text = getString(R.string.main_single_value_format, token.tokenSymbol)
            usdValueTextView.text = getString(R.string.main_usd_end_format, token.getFormattedExchangeRate())
            availableTextView.text = getString(R.string.main_send_available, token.getFormattedTotal())
            feeValueTextView.text = "0,000005 SOL" // todo: get valid fee
            amountEditText.doAfterTextChanged {
                val amount = it.toString().toDoubleOrNull()?.toBigDecimal() ?: BigDecimal.ZERO
                val around = token.exchangeRate.times(amount)

                val isMoreThanBalance = amount > token.total
                val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue
                availableTextView.setTextColor(ContextCompat.getColor(requireContext(), availableColor))

                aroundTextView.text = getString(R.string.main_send_around_in_usd, around)

                val isEnabled = amount == BigDecimal.ZERO || !isMoreThanBalance
                sendButton.isEnabled = isEnabled && addressEditText.text.toString().isNotEmpty()
            }

            addressEditText.doAfterTextChanged {
                val isEmpty = it.toString().isEmpty()

                scanImageView.isVisible = isEmpty
                clearImageView.isVisible = !isEmpty

                sendButton.isEnabled = !isEmpty && amountEditText.text.toString().isNotEmpty()
            }

            sourceImageView.setOnClickListener {
                presenter.loadTokensForSelection()
            }
        }
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}