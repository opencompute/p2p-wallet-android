package com.p2p.wallet.dashboard.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogTansactionBinding
import com.p2p.wallet.deprecated.viewcommand.Command
import com.p2p.wallet.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wallet.utils.getTransactionDate
import com.p2p.wallet.common.network.Constants.Companion.EXPLORER_SOLANA
import com.p2p.wallet.dashboard.model.local.ActivityItem
import com.p2p.wallet.utils.bindadapter.walletFormat
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.blockchain.BlockChainExplorerFragment
import com.p2p.wallet.utils.copyToClipBoard
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionBottomSheet(private val dataInfo: ActivityItem, val navigate: (url: String) -> Unit) :
    BottomSheetDialogFragment() {

    companion object {
        const val TRANSACTION_DIALOG = "transactionDialog"
        fun newInstance(dataInfo: ActivityItem, navigate: (url: String) -> Unit): TransactionBottomSheet =
            TransactionBottomSheet(dataInfo, navigate)
    }

    private val viewModel: DetailWalletViewModel by viewModel()

    private val binding: DialogTansactionBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_tansaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBlockTime(dataInfo.slot)
        binding.apply {
            setTransactionImage()

            fromTextView.walletFormat(dataInfo.from, 4)
            toTextView.walletFormat(dataInfo.to, 4)

            copyToUserKey.setOnClickListener {
                context?.copyToClipBoard(dataInfo.to)
            }
            copyFromUserKey.setOnClickListener {
                context?.copyToClipBoard(dataInfo.from)
            }
            copyTransaction.setOnClickListener {
                context?.copyToClipBoard(dataInfo.signature)
            }

            blockChainExplorer.setOnClickListener {
                replaceFragment(BlockChainExplorerFragment.createScreen(EXPLORER_SOLANA + dataInfo.signature))
            }

            txtType.setText(if (dataInfo.isReceive) R.string.receive else R.string.send)
        }
        observes()
    }

    private fun setTransactionImage() {
        val imageTransaction = if (dataInfo.isReceive) R.drawable.ic_receive else R.drawable.ic_send
        binding.imgTransactionType.setImageResource(imageTransaction)
    }

    private fun observes() {
        viewModel.blockTime.observe(
            viewLifecycleOwner,
            {
                binding.yourTransactionDate.text = it.getTransactionDate()
            }
        )
        viewModel.blockTimeError.observe(
            viewLifecycleOwner,
            {
                binding.yourTransactionDate.text = it.getTransactionDate()
            }
        )

        viewModel.command.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    is Command.NavigateBlockChainViewCommand -> {
                        navigate.invoke(it.url)
                    }
                }
            }
        )
    }
}