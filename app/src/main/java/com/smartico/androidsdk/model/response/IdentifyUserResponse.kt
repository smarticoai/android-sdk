package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.messageengine.ClassId

internal data class IdentifyUserResponse(
    @SerializedName("public_username")
    val publicUsername: String,

    @SerializedName("avatar_id")
    val avatarId: String,

    @SerializedName("props")
    val props: Map<String, Any>?
) : BaseResponse(cid = ClassId.IdentifyResponse.id)