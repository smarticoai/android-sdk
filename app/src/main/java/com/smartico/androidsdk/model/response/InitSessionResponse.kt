package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class InitSessionResponse(
    @SerializedName("cid")
    val cid: Int,

    @SerializedName("errCode")
    val errCode: Int,

    @SerializedName("errMsg")
    val errMsg: String?,

    @SerializedName("ts")
    val ts: Long?,

    @SerializedName("uuid")
    val uuid: String?,

    @SerializedName("products")
    val products: List<Int>?,

    @SerializedName("payload")
    val payload: InitSessionResponsePayload?,

    @SerializedName("settings")
    val settings: InitSessionResponseSettings?

) : SmarticoWebSocketMessage

internal data class InitSessionResponsePayload(
    @SerializedName("country")
    val country: String?,

    @SerializedName("city")
    val city: String,

    @SerializedName("ip")
    val ip: String,

    @SerializedName("s")
    val s: String
)

internal data class InitSessionResponseSettings(
    @SerializedName("FCM_SENDER_ID")
    val fcmSenderId: String?,

    @SerializedName("GAMIFICATION_WRAPPER_PAGE")
    val gamificationWrapperPage: String?,

    @SerializedName("ENGAGEMENT_WRAPPER_PAGE")
    val engagementWrapperPage: String?,

    @SerializedName("DYNAMIC_IMAGE_DOMAIN")
    val dynamicImageDomain: String?
)