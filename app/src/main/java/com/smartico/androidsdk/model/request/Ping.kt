package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class Ping(
    @SerializedName("cid")
    val cid: Int
) : SmarticoWebSocketMessage