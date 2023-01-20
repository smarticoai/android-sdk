package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.messageengine.ClassId
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class Pong(
    @SerializedName("cid")
    val cid: Int = ClassId.Pong.id
): SmarticoWebSocketMessage