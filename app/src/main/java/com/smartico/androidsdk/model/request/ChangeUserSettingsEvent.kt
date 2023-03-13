package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.messageengine.ClassId
import java.util.UUID

// AA: the right name for this case is just "GeneralEvent"
// it should not have default type like "core_language_changed"
// "payload" is also abstract json, it shouldn't be typed to any specific structure
data class ChangeUserSettingsEvent(

    @SerializedName("eventType")
    val eventType: String = "core_language_changed",

    @SerializedName("payload")
    val payload: ChangeUserSettingsEventPayload,

    @SerializedName("ts")
    val ts: Long = System.currentTimeMillis(),

    @SerializedName("uuid")
    val uuid: String = UUID.randomUUID().toString()

) : BaseRequest(cid = ClassId.GenericEvent.id)

data class ChangeUserSettingsEventPayload(
    @SerializedName("language")
    val language: String? = null,

    @SerializedName("action")
    val action: String? = null
)