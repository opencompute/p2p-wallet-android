package com.p2p.wowlet.appbase.viewcommand

import android.os.Bundle
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.EnterWallet
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.YourWallets

sealed class Command {

    class NetworkErrorViewCommand : ViewCommand

    /*Navigate fragment commands*/
    class FinishAppViewCommand : ViewCommand
    class NavigateUpViewCommand(val destinationId: Int) : ViewCommand
    class NavigateUpBackStackCommand() : ViewCommand
    class NavigateRegLoginViewCommand(val destinationId: Int) : ViewCommand
    class NavigateTermAndConditionViewCommand(val destinationId: Int) : ViewCommand
    class NavigatePinCodeViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand

    // class NavigatePinCodeVerificationViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateCreateWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegFinishViewCommand(val destinationId: Int) : ViewCommand
    class NavigateNotificationViewCommand(val destinationId: Int) : ViewCommand
    class NavigateFingerPrintViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRecoveryWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateSecretKeyViewCommand(val destinationId: Int) : ViewCommand
    class NavigateSwapViewCommand(val destinationId: Int) : ViewCommand

    class NavigateScannerViewCommand(val destinationId: Int) : ViewCommand
    class OpenAddCoinDialogViewCommand() : ViewCommand
    class NavigateWalletViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand
    class NavigateSendCoinViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand
    class NavigateBlockChainViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand
    class NavigateManualSecretKeyViewCommand(val destinationId: Int, val bundle: Bundle?) :
        ViewCommand

    class NavigateDetailSavingViewCommand(val destinationId: Int) : ViewCommand
    class OpenProfileDialogViewCommand() : ViewCommand
    class OpenMainActivityViewCommand() : ViewCommand


    class EnterWalletDialogViewCommand(val list: List<EnterWallet>) : ViewCommand
    class YourWalletDialogViewCommand(val enterWallet: EnterWallet) : ViewCommand
    class OpenMyWalletDialogViewCommand() : ViewCommand
    class OpenProfileDetailDialogViewCommand() : ViewCommand
    class OpenBackupDialogViewCommand() : ViewCommand
    class OpenTransactionDialogViewCommand(val itemActivity: ActivityItem) : ViewCommand
    class SendCoinDoneViewCommand(val transactionInfo: ActivityItem) : ViewCommand
    class SendCoinViewCommand() : ViewCommand
    class SwapCoinProcessingViewCommand() : ViewCommand
    class OpenAllMyTokensDialogViewCommand(val yourWallets: YourWallets) : ViewCommand
}