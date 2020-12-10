package com.p2p.wowlet.fragment.dashboard.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.activity.RegistrationActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentDashboardBinding
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDetailsDialog
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDialog
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet.Companion.ENTER_WALLET
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet.Companion.TAG_ADD_COIN
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : FragmentBaseMVVM<DashboardViewModel, FragmentDashboardBinding>() {

    override val viewModel: DashboardViewModel by viewModel()
    override val binding: FragmentDashboardBinding by dataBinding(R.layout.fragment_dashboard)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@DashboardFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is NavigateScannerViewCommand -> navigateFragment(command.destinationId)
            is NavigateSwapViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is NavigateWalletViewCommand -> {
                navigateFragment(command.destinationId, command.bundle)
                (activity as MainActivity).showHideNav(false)
            }
            is NavigateSendCoinViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is NavigateDetailSavingViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is OpenAddCoinDialogViewCommand -> {
                AddCoinBottomSheet.newInstance().show(
                    childFragmentManager,
                    TAG_ADD_COIN
                )
            }
            is EnterWalletDialogViewCommand -> {
                EnterWalletBottomSheet.newInstance(command.list).show(
                    childFragmentManager,
                    ENTER_WALLET
                )
            }
            is OpenProfileDetailDialogViewCommand -> {
                ProfileDetailsDialog.newInstance {
                    viewModel.clearSecretKey()
                    activity?.let {
                        val intent = Intent(it, RegistrationActivity::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                }.show(
                    childFragmentManager,
                    ProfileDetailsDialog.TAG_PROFILE_DETAILS_DIALOG
                )
            }
            is OpenProfileDialogViewCommand -> {
                ProfileDialog.newInstance {
                    viewModel.goToProfileDetailDialog()
                }.show(
                    childFragmentManager,
                    ProfileDialog.TAG_PROFILE_DIALOG
                )
            }
        }
    }

    override fun navigateUp() {
        viewModel.finishApp()
    }
}