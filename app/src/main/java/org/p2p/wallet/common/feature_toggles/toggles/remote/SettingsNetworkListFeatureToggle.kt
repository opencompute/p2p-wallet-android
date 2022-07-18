package org.p2p.wallet.common.feature_toggles.toggles.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import org.p2p.wallet.utils.fromJsonReified

class SettingsNetworkValue(
    @SerializedName("urlString")
    val url: String,
    @SerializedName("network")
    val networkName: String,
    @SerializedName("additionalQuery")
    val additionalQuery: String? = null
)

class SettingsNetworkListFeatureToggle(
    private val gson: Gson,
    valuesProvider: RemoteConfigValuesProvider
) : JsonFeatureToggle<List<SettingsNetworkValue>>(valuesProvider) {
    override val featureKey: String = "settings_network_values"
    override val featureDescription: String = "List of available networks to choose from"

    override val defaultValue: List<SettingsNetworkValue> = listOf(
        SettingsNetworkValue(url = "https://p2p.rpcpool.com", networkName = "mainnet-beta"),
        SettingsNetworkValue(url = "https://solana-api.projectserum.com", networkName = "mainnet-beta"),
        SettingsNetworkValue(url = "https://api.mainnet-beta.solana.com", networkName = "mainnet-beta")

    )
    override val value: List<SettingsNetworkValue>
        get() = valuesProvider.getString(featureKey)
            ?.let { gson.fromJsonReified<List<SettingsNetworkValue>>(it) }
            ?.takeIf(List<SettingsNetworkValue>::isNotEmpty)
            ?: defaultValue
}