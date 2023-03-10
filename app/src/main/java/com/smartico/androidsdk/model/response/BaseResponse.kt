package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

open class BaseResponse(

    @SerializedName("cid")
    val cid: Int,

    @SerializedName("errCode")
    val errCode: Int? = null,

    @SerializedName("errMsg")
    val errMsg: String? = null

) : SmarticoWebSocketMessage