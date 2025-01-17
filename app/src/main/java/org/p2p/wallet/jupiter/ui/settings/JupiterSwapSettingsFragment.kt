package org.p2p.wallet.jupiter.ui.settings

import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrInvisible
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapSettingsBinding
import org.p2p.wallet.jupiter.analytics.JupiterSwapSettingsAnalytics
import org.p2p.wallet.jupiter.ui.info.SwapInfoBottomSheet
import org.p2p.wallet.jupiter.ui.info.SwapInfoType
import org.p2p.wallet.jupiter.ui.routes.SwapSelectRoutesBottomSheet
import org.p2p.wallet.jupiter.ui.settings.adapter.SwapSettingsDecorator
import org.p2p.wallet.jupiter.ui.settings.view.swapCustomSlippageDelegate
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"

private typealias SwapSettingsBaseMvpFragment =
    BaseMvpFragment<JupiterSwapSettingsContract.View, JupiterSwapSettingsContract.Presenter>

class JupiterSwapSettingsFragment :
    SwapSettingsBaseMvpFragment(R.layout.fragment_jupiter_swap_settings),
    JupiterSwapSettingsContract.View {

    companion object {
        fun create(stateManagerKey: String): JupiterSwapSettingsFragment =
            JupiterSwapSettingsFragment()
                .withArgs(ARG_STATE_MANAGE_KEY to stateManagerKey)
    }

    private val binding: FragmentJupiterSwapSettingsBinding by viewBinding()

    private val stateManagerKey: String by args(ARG_STATE_MANAGE_KEY)

    override val presenter: JupiterSwapSettingsContract.Presenter by inject { parametersOf(stateManagerKey) }

    private val analytics: JupiterSwapSettingsAnalytics by inject()

    private val adapter by unsafeLazy {
        CommonAnyCellAdapter(
            swapCustomSlippageDelegate(presenter::onCustomSlippageChange),
            sectionHeaderCellDelegate(),
            baseCellDelegate(inflateListener = { financeBlock ->
                financeBlock.setOnClickAction { _, item -> presenter.onSettingItemClick(item) }
            }),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            recyclerViewSettings.itemAnimator = null
            recyclerViewSettings.addItemDecoration(SwapSettingsDecorator())
            recyclerViewSettings.attachAdapter(adapter)
        }
    }

    override fun bindSettingsList(list: List<AnyCellItem>) {
        adapter.setItems(list) {
            lifecycleScope.launchWhenStarted {
                binding.recyclerViewSettings.invalidateItemDecorations()
            }
        }
    }

    override fun setRatioState(state: TextViewCellModel?) {
        binding.textViewRate.bindOrInvisible(state)
    }

    override fun showDetailsDialog(type: SwapInfoType) {
        analytics.logFeeDetailsClicked(type)
        SwapInfoBottomSheet.show(childFragmentManager, stateManagerKey, type)
    }

    override fun showRouteDialog() {
        analytics.logChangeRouteClicked()
        SwapSelectRoutesBottomSheet.show(childFragmentManager, stateManagerKey)
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, _ ->
            insets.systemAndIme().consume {
                view.appleTopInsets(this)
                binding.recyclerViewSettings.appleBottomInsets(this)
            }
        }
    }
}
