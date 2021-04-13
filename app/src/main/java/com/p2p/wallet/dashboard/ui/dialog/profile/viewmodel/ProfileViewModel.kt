package com.p2p.wallet.dashboard.ui.dialog.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.interactor.DashboardInteractor
import com.p2p.wallet.dashboard.model.SelectedCurrency
import com.p2p.wallet.dashboard.model.local.EnableFingerPrintModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val dashboardInteractor: DashboardInteractor
) : ViewModel() {

    fun setUsesFingerPrint(isEnabled: Boolean, isNotWantEnable: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
//            fingerPrintInteractor.setFingerPrint(
//                EnableFingerPrintModel(isEnabled, isNotWantEnable)
//            )
        }

    fun isUsesFingerprint(): EnableFingerPrintModel {
//        return fingerPrintInteractor.isEnableFingerPrint()
        return EnableFingerPrintModel(false, false)
    }

    fun setSelectedCurrency(currency: SelectedCurrency) {
        dashboardInteractor.setSelectedCurrency(currency)
    }

    fun getSelectedCurrency(): SelectedCurrency? = dashboardInteractor.getSelectedCurrency()

    fun getSelectedCurrencyCheckbox(): Int {
        return when (getSelectedCurrency()) {
            SelectedCurrency.USD -> R.id.rbUSD
            SelectedCurrency.EUR -> R.id.rbEUR
            SelectedCurrency.CNY -> R.id.rbCNY
            SelectedCurrency.KRW -> R.id.rbKRW
            SelectedCurrency.RUB -> R.id.rbRUB
            else -> R.id.rbUSD
        }
    }

    fun getSelectedCurrencyName(): String {
        return when (getSelectedCurrency()) {
            SelectedCurrency.USD -> "US\u0024"
            SelectedCurrency.EUR -> "EU\u20ac"
            SelectedCurrency.CNY -> "CY\u00a5"
            SelectedCurrency.KRW -> "KW\u20a9"
            SelectedCurrency.RUB -> "RU\u20bd"
            else -> "US\u0024"
        }
    }
}