package org.p2p.wallet.settings.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.repository.SettingsLocalRepository

private const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"

class SettingsInteractor(
    private val localRepository: SettingsLocalRepository,
    private val sharedPreferences: SharedPreferences,
    private val environmentManager: EnvironmentManager
) {
    fun getProfileSettings(username: String) = localRepository.getProfileSettings(username)

    fun getNetworkSettings(): List<SettingsRow> {
        val networkName = environmentManager.loadEnvironment().name
        val tokenName = Token.SOL_SYMBOL
        return localRepository.getNetworkSettings(networkName, tokenName)
    }

    fun getAppearanceSettings(): List<SettingsRow> {
        return localRepository.getAppearanceSettings(BuildConfig.VERSION_NAME)
    }

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
    }

    fun isZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)
}