package com.p2p.wowlet.fragment.dashboard.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.Command.EnterWalletDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.OpenAllMyTokensDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.OpenBackupFailedDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.OpenProfileDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.OpenRecoveryPhraseDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.OpenSwapBottomSheetViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.YourWalletDialogViewCommand
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentDashboardBinding
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet.Companion.TAG_SEND_COIN
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.p2p.wowlet.fragment.dashboard.adapter.WalletsAdapter
import com.p2p.wowlet.fragment.dashboard.dialog.BackupDialog
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDetailsFragment
import com.p2p.wowlet.fragment.dashboard.dialog.SecurityDialog
import com.p2p.wowlet.fragment.dashboard.dialog.TransactionBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet.Companion.TAG_ADD_COIN
import com.p2p.wowlet.fragment.dashboard.dialog.allmytokens.AllMyTokensBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.allmytokens.AllMyTokensBottomSheet.Companion.TAG_ALL_MY_TOKENS_DIALOG
import com.p2p.wowlet.fragment.dashboard.dialog.currency.CurrencyDialog
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.DetailWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.DetailWalletBottomSheet.Companion.DETAIL_WALLET
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet.Companion.ENTER_WALLET
import com.p2p.wowlet.fragment.dashboard.dialog.networks.NetworksDialog
import com.p2p.wowlet.fragment.dashboard.dialog.profile.ProfileDialog
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.RecoveryPhraseDialog
import com.p2p.wowlet.fragment.dashboard.dialog.savedcards.SavedCardsDialog
import com.p2p.wowlet.fragment.dashboard.dialog.sendyourwallet.YourWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.swap.SwapBottomSheet
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.fragment.qrscanner.view.QrScannerFragment
import com.p2p.wowlet.utils.OnSwipeTouchListener
import com.p2p.wowlet.utils.drawChart
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.p2p.wowlet.utils.withArgs
import com.wowlet.entities.enums.PinCodeFragmentType
import com.wowlet.entities.local.WalletItem
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.p2p.solanaj.rpc.RpcClient

class DashboardFragment : BaseFragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModel()
    private val binding: FragmentDashboardBinding by viewBinding()

    private val rpcClient: RpcClient by inject()

    private var isBackupSuccess: Boolean? = null

    companion object {
        const val BACK_TO_DASHBOARD_SCREEN = "backToDashboardScreen"

        fun create(backToDashboard: Boolean): DashboardFragment =
            DashboardFragment().withArgs(
                BACK_TO_DASHBOARD_SCREEN to backToDashboard
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeData()

        binding.run {
            imgProfilePic.setOnClickListener { viewModel.openProfileDialog() }
            swapView.setOnClickListener {
                viewModel.openSwapBottomSheet(null)
            }

            addTokensContainer.setOnClickListener {
                openAddCoinDialog()
            }

            lScannerContainer.setOnClickListener {
                replaceFragment(QrScannerFragment())
            }

            enterWalletView.setOnClickListener { viewModel.enterWalletDialog() }

            myBalanceContainer.setOnClickListener { viewModel.openAllMyTokensDialog() }

            sendButton.setOnClickListener {
                val item = (viewModel as? DashboardViewModel)?.getAllWalletData?.value?.firstOrNull()
                SendCoinsBottomSheet.newInstance(
                    item, null
                ).show(
                    childFragmentManager,
                    TAG_SEND_COIN
                )
            }

            with(vRvWallets) {
                layoutManager = LinearLayoutManager(this.context)
            }
        }
        viewModel.getWalletItems()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        if (isBackupSuccess != null) {
            if (isBackupSuccess as Boolean) {
                viewModel.openRecoveryPhraseDialog()
            } else {
                viewModel.openBackupFailedDialog()
            }
        }

        binding.rootContainer.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeRight() {
                replaceFragment(QrScannerFragment())
            }
        })
        binding.vRvWallets.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeRight() {
                replaceFragment(QrScannerFragment())
            }
        })
    }

    private fun initData() {
        arguments?.let {
            isBackupSuccess = it.getBoolean(BACK_TO_DASHBOARD_SCREEN)
            it.clear()
        }
    }

    private fun processViewCommand(command: ViewCommand) {
        when (command) {
            is OpenSwapBottomSheetViewCommand -> {
                SwapBottomSheet.newInstance(
                    allMyWallets = command.allMyWallets,
                    selectedWalletItems = command.walletData
                ).show(
                    childFragmentManager,
                    SwapBottomSheet.TAG_SWAP_BOTTOM_SHEET
                )
            }

            is Command.OpenTransactionDialogViewCommand -> {
                TransactionBottomSheet.newInstance(command.itemActivity) {
                    replaceFragment(BlockChainExplorerFragment.createScreen(it))
                }.show(childFragmentManager, TransactionBottomSheet.TRANSACTION_DIALOG)
            }

            is OpenProfileDialogViewCommand -> {
                ProfileDialog.newInstance({
                    replaceFragment(ProfileDetailsFragment.newInstance())
                }, {
                    BackupDialog.newInstance {
                        replaceFragment(
                            PinCodeFragment.create(
                                openSplashScreen = false,
                                isBackupDialog = true,
                                type = PinCodeFragmentType.VERIFY
                            )
                        )
                    }.show(childFragmentManager, BackupDialog.TAG_BACKUP_DIALOG)
                }, {
                    NetworksDialog.newInstance {
                        rpcClient.updateEndpoint(it.endpoint)
                        viewModel.getWalletItems()
                        it()
                    }.show(childFragmentManager, NetworksDialog.TAG_NETWORKS_DIALOG)
                }, {
                    CurrencyDialog.newInstance(it)
                        .show(childFragmentManager, CurrencyDialog.TAG_CURRENCY_DIALOG)
                }, {
                    SavedCardsDialog.newInstance()
                        .show(childFragmentManager, SavedCardsDialog.TAG_SAVED_CARDS_DIALOG)
                },
                    {
                        SecurityDialog.newInstance(it)
                            .show(childFragmentManager, SecurityDialog.TAG_SECURITY_DIALOG)
                    }).show(
                    childFragmentManager,
                    ProfileDialog.TAG_PROFILE_DIALOG
                )
            }
            is OpenAllMyTokensDialogViewCommand -> {
                AllMyTokensBottomSheet.newInstance(command.yourWallets, {
                    openAddCoinDialog()
                }) { itemWallet ->
                    openWalletDetails(itemWallet)
                }.show(
                    childFragmentManager, TAG_ALL_MY_TOKENS_DIALOG
                )
            }
            is OpenRecoveryPhraseDialogViewCommand -> {
                RecoveryPhraseDialog.newInstance()
                    .show(childFragmentManager, RecoveryPhraseDialog.TAG_RECOVERY_DIALOG)
            }
            is OpenBackupFailedDialogViewCommand -> {

            }
            is YourWalletDialogViewCommand -> {
                YourWalletBottomSheet.newInstance(command.enterWallet).show(
                    childFragmentManager,
                    YourWalletBottomSheet.ENTER_YOUR_WALLET
                )
            }
            is EnterWalletDialogViewCommand -> {
                EnterWalletBottomSheet.newInstance(command.list).show(
                    childFragmentManager,
                    ENTER_WALLET
                )
            }
        }
    }

    private fun openWalletDetails(it: WalletItem) {
        DetailWalletBottomSheet.newInstance(it, {
            viewModel.goToQrScanner(it)
        }, {
            openAddCoinDialog()
        }, {
            viewModel.goToSendCoin(it)
        }, {
            viewModel.enterWalletDialog()
        }, {
            viewModel.openSwapBottomSheet(it)
        }, {
            replaceFragment(BlockChainExplorerFragment.createScreen(it))
        }).show(
            childFragmentManager,
            DETAIL_WALLET
        )
    }

    private fun observeData() {
        viewModel.getWalletDataError.observe(viewLifecycleOwner) {
            context?.run {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.yourBalance.observe(viewLifecycleOwner) {
            binding.vPrice.text = getString(R.string.usd_symbol_2, it)
        }
        viewModel.getWalletChart.observe(viewLifecycleOwner) {
            binding.vPieChartData.drawChart(it)
        }
        viewModel.getWalletData.observe(viewLifecycleOwner) {
            val adapter = WalletsAdapter(viewModel, it) {
                // what to do?
            }
            binding.vRvWallets.adapter = adapter
        }
        viewModel.getAllWalletData.observe(viewLifecycleOwner) { itemList ->
            when {
                itemList.isEmpty() -> {
                    binding.allMyTokensContainer.visibility = View.GONE
                    binding.addTokensContainer.visibility = View.GONE
                }
                itemList.size <= 4 -> {
                    binding.allMyTokensContainer.visibility = View.GONE
                    binding.addTokensContainer.visibility = View.VISIBLE
                }
                else -> {
                    binding.allMyTokensContainer.visibility = View.VISIBLE
                    binding.addTokensContainer.visibility = View.GONE
                }
            }
            binding.rootContainer.post {
                val myBalanceContainer: Int =
                    binding.myBalanceContainer.run { height + marginTop + marginBottom }
                val lCoinContainer: Int =
                    binding.lCoinContainer.run { height + marginBottom }
                val rvMargins: Int = binding.vRvWallets.run { marginTop + marginBottom }
                val button: Int = binding.run {
                    addTokensContainer.run { height + marginBottom + marginTop }
                    +allMyTokensContainer.run { height + marginBottom + marginTop }
                }
                val allowedHeight =
                    binding.rootContainer.height - (myBalanceContainer + lCoinContainer + rvMargins + button)
                val layoutParams: ViewGroup.LayoutParams =
                    if (binding.vRvWallets.height > allowedHeight) {
                        binding.vRvWallets.layoutParams.apply {
                            height = allowedHeight
                        }
                    } else {
                        binding.vRvWallets.layoutParams.apply {
                            height = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }
                binding.vRvWallets.layoutParams = layoutParams
            }
        }

        viewModel.command.observe(viewLifecycleOwner) {
            processViewCommand(it)
        }
    }

    private fun openAddCoinDialog() {
        AddCoinBottomSheet.newInstance(
            goToDetailWalletFragment = {
                openWalletDetails(it)
                viewModel.getWalletItems()
            },
            goToSolanaExplorerFragment = {
                replaceFragment(BlockChainExplorerFragment.createScreen(it))
            }
        ).show(childFragmentManager, TAG_ADD_COIN)
    }
}