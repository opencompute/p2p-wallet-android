package com.p2p.wowlet.fragment.investments.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentInvestmentsBinding

import com.p2p.wowlet.fragment.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.utils.popBackStack
import org.koin.androidx.viewmodel.ext.android.viewModel

class InvestmentsFragment : FragmentBaseMVVM<InvestmentsViewModel, FragmentInvestmentsBinding>() {

    override val viewModel: InvestmentsViewModel by viewModel()
    override val binding: FragmentInvestmentsBinding by dataBinding(R.layout.fragment_investments)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@InvestmentsFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()

        }
    }
}