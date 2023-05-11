package org.p2p.wallet.infrastructure.network.alarmlogger.api

import com.google.gson.annotations.SerializedName

data class AlarmErrorsRequest(
    @SerializedName("title")
    val logsTitle: String,
    @SerializedName("message")
    val payload: String // should pass only String
)
