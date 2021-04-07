package com.p2p.wowlet.fragment.qrscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.interactors.DashboardInteractor
import com.p2p.wowlet.domain.interactors.QrScannerInteractor
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.QrWalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrScannerViewModel(
    val qrScannerInteractor: QrScannerInteractor,
    val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {
    private val _isCurrentAccount by lazy { MutableLiveData<QrWalletType>() }
    val isCurrentAccount: LiveData<QrWalletType> get() = _isCurrentAccount
    private val _isCurrentAccountError by lazy { MutableLiveData<String>() }
    val isCurrentAccountError: LiveData<String> get() = _isCurrentAccountError

    fun navigateUp() {
        _command.value =
            Command.NavigateUpBackStackCommand()
    }

    fun getAccountInfo(publicKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = qrScannerInteractor.getAccountInfo(publicKey)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    data.data?.let { mintAddress->
                        when (val result = dashboardInteractor.checkWalletFromList(mintAddress)) {
                            is Result.Success -> {
                                _isCurrentAccount.value =
                                    result.data?.let { constWalletItem -> QrWalletType(constWalletItem, publicKey) }
                            }
                            is Result.Error -> {
                                _isCurrentAccountError.value = result.errors.errorMessage
                            }
                        }
                    }
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _isCurrentAccountError.value = data.errors.errorMessage
                }
            }
        }
    }
}