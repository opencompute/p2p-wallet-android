package org.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.R

private const val KEY_NOTIFICATION_SERVICE_BASE_URL = "KEY_NOTIFICATION_SERVICE_BASE_URL"
private const val KEY_FEE_RELAYER_BASE_URL = "KEY_FEE_RELAYER_BASE_URL"
private const val KEY_TORUS_BASE_URL = "KEY_TORUS_BASE_URL"
private const val KEY_TORUS_BASE_VERIFIER = "KEY_TORUS_BASE_VERIFIER"
private const val KEY_TORUS_BASE_SUB_VERIFIER = "KEY_TORUS_BASE_SUB_VERIFIER"

class NetworkServicesUrlProvider(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    fun loadFeeRelayerEnvironment(): FeeRelayerEnvironment {
        val url = sharedPreferences.getString(
            KEY_FEE_RELAYER_BASE_URL,
            context.getString(R.string.feeRelayerBaseUrl)
        ).orEmpty()

        return FeeRelayerEnvironment(url)
    }

    fun saveFeeRelayerEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_FEE_RELAYER_BASE_URL, newUrl) }
    }

    fun loadNotificationServiceEnvironment(): NotificationServiceEnvironment {
        val url = sharedPreferences.getString(
            KEY_NOTIFICATION_SERVICE_BASE_URL,
            context.getString(R.string.notificationServiceBaseUrl)
        ).orEmpty()

        return NotificationServiceEnvironment(url)
    }

    fun saveNotificationServiceEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_NOTIFICATION_SERVICE_BASE_URL, newUrl) }
    }

    fun loadTorusEnvironment(): TorusEnvironment {
        val url = sharedPreferences.getString(
            KEY_TORUS_BASE_URL,
            context.getString(R.string.torusBaseUrl)
        ).orEmpty()
        val verifier = sharedPreferences.getString(
            KEY_TORUS_BASE_VERIFIER,
            context.getString(R.string.torusReleaseVerifier)
        ).orEmpty()
        val subVerifier = sharedPreferences.getString(
            KEY_TORUS_BASE_SUB_VERIFIER,
            context.getString(R.string.torusReleaseSubVerifier)
        ).orEmpty()
        return TorusEnvironment(url, verifier, subVerifier)
    }

    fun saveTorusEnvironment(newUrl: String?, newVerifier: String?, newSubVerifier: String?) {
        sharedPreferences.edit {
            newUrl?.let {
                putString(KEY_TORUS_BASE_URL, it)
            }
            newVerifier?.let {
                putString(KEY_TORUS_BASE_VERIFIER, it)
            }
            newSubVerifier?.let {
                putString(KEY_TORUS_BASE_SUB_VERIFIER, it)
            } ?: run {
                remove(KEY_TORUS_BASE_SUB_VERIFIER)
            }
        }
    }
}
