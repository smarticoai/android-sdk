package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.messageengine.ClassId
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class IdentifyUserRequest(
    @SerializedName("cid")
    val cid: Int = ClassId.IdentifyRequest.id,

    @SerializedName("ext_user_id")
    val extUserId: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("platform")
    val platform: Int,

    @SerializedName("pushNotificationUserStatus")
    val pushNotificationUserStatus: Int,

    @SerializedName("page")
    val page: String?,

    @SerializedName("ua")
    val ua: UA?

): SmarticoWebSocketMessage