package com.smartico.androidsdk.model.response

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.messageengine.ClassId

//{"saw_template_id":1,"cid":707,"ts":1685342791874,"uuid":"04cdfd97-112b-4de7-b8f2-28a7c8e67f4a","payload":null,"duration":null}
data class TriggerMiniGameResponse(

    @SerializedName("saw_template_id")
    val sawTemplateId: Int?,

    @SerializedName("ts")
    val ts: Long?,

    @SerializedName("uuid")
    val uuid: String?


) : BaseResponse(cid = ClassId.TriggerMiniGame.id)