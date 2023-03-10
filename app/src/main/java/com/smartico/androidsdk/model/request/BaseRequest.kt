package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

open class BaseRequest(
    @SerializedName("cid")
    val cid: Int
) : SmarticoWebSocketMessage