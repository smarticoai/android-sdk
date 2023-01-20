package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class InitSession(
    @SerializedName("cid")
    val cid: Int,

    @SerializedName("label_name")
    val labelName: String,

    @SerializedName("brand_key")
    val brandKey: String,

    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("page")
    val page: String?,

    @SerializedName("tracker_version")
    val trackerVersion: String,

    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("ua")
    val ua: UA
) : SmarticoWebSocketMessage

internal data class UA(

    @SerializedName("fp")
    val fp: String?,

    @SerializedName("agent")
    val agent: String?,

    @SerializedName("host")
    val host: String?,

    @SerializedName("device_type")
    val deviceType: String,

    @SerializedName("tzoffset")
    val tzoffset: Int,

    @SerializedName("os")
    val os: String = "Android"
)