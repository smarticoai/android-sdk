package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName

internal data class IdentifyUserResponse(
    @SerializedName("cid")
    val cid: Int,

    @SerializedName("public_username")
    val publicUsername: String,

    @SerializedName("avatar_id")
    val avatarId: String,

    @SerializedName("props")
    val props: Map<String, Any>?
)