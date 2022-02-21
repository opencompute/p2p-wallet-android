package org.p2p.wallet.moonpay.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.PrefixSuffixTextWatcher
import org.p2p.wallet.databinding.FragmentBuySolanaBinding
import org.p2p.wallet.moonpay.model.BuyData
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withTextOrGone

class BuySolanaFragment :
    BaseMvpFragment<BuySolanaContract.View, BuySolanaContract.Presenter>(R.layout.fragment_buy_solana),
    BuySolanaContract.View {

    companion object {
        fun create() = BuySolanaFragment()
    }

    override val presenter: BuySolanaContract.Presenter by inject()

    private val binding: FragmentBuySolanaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            installPrefixWatcher()

            continueButton.setOnClickListener {
                presenter.onContinueClicked()
            }
            getValueTextView.setOnClickListener {
                presenter.onSwapClicked()
            }
        }

        presenter.loadData()
    }

    override fun showTokenPrice(price: String) {
        binding.priceView.setValueText(price)
    }

    override fun showData(data: BuyData) {
        with(binding) {
            priceView.setValueText(data.priceText)
            getValueTextView.text = data.receiveAmountText
            processingFeeView.setValueText(data.processingFeeText)
            networkFeeView.setValueText(data.networkFeeText)
            extraFeeView.setValueText(data.extraFeeText)
            accountCreationView.setValueText(data.accountCreationCostText)
            data.purchaseCostText?.let {
                purchaseCostView.setValueText(it)
            }

            totalView.setValueText(data.totalText)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showMessage(message: String?) {
        binding.apply {
            errorTextView withTextOrGone message
            continueButton.isEnabled = !hasInputError()
        }
    }

    override fun navigateToMoonpay(amount: String) {
        replaceFragment(MoonpayViewFragment.create(amount))
    }

    override fun swapData(isSwapped: Boolean, prefixSuffixSymbol: String) = with(binding) {
        if (isSwapped) {
            payTextView.setText(R.string.buy_you_get)
            getTextView.setText(R.string.buy_you_pay)
        } else {
            payTextView.setText(R.string.buy_you_pay)
            getTextView.setText(R.string.buy_you_get)
        }
        val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
        installPrefixWatcher(isSwapped, prefixSuffixSymbol)
        payEditText.hint = if (isSuffix) {
            "0 $prefixSuffixSymbol"
        } else {
            "${prefixSuffixSymbol}0"
        }
        payEditText.text = payEditText.text
    }

    private fun installPrefixWatcher(
        isSwapped: Boolean = false,
        prefixSuffixSymbol: String = Constants.USD_SYMBOL
    ) = with(binding) {
        val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
        val finalPrefixSuffixSymbol = if (isSuffix) " $prefixSuffixSymbol" else prefixSuffixSymbol
        PrefixSuffixTextWatcher.uninstallFrom(payEditText)
        PrefixSuffixTextWatcher.installOn(payEditText, finalPrefixSuffixSymbol, isSuffix = isSuffix) { data ->
            if (!isSwapped) purchaseCostView.setValueText(data.prefixText)
            continueButton.isEnabled = data.prefixText.isNotEmpty() && !hasInputError()
            presenter.setBuyAmount(data.valueWithoutPrefix)
        }
    }

    private fun hasInputError(): Boolean = binding.errorTextView.isVisible
}