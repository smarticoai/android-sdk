package com.smartico.androidsdk.model.request

import com.google.gson.annotations.SerializedName
import com.smartico.androidsdk.model.SmarticoWebSocketMessage

internal data class ClientEngagementEvent(

    @SerializedName("engagement_uid")
    val engagementUid: String?,

    @SerializedName("activityType")
    val activityType: Int?,

    @SerializedName("engagement_id")
    val engagementId: String?,

    @SerializedName("root_audience_id")
    val rootAudienceId: Int?,

    @SerializedName("audience_id")
    val audienceId: Int?,

    @SerializedName("resource_id")
    val resourceId: Int?,

    @SerializedName("resource_variation_id")
    val resourceVariationId: Int?,

    @SerializedName("template_html")
    val templateHtml: String?,

    @SerializedName("template_engine")
    val templateEngine: Int?,

    @SerializedName("params")
    val params: Map<String, Any>?,

    @SerializedName("source_product_id")
    val sourceProductId: Int?,

    @SerializedName("cid")
    val cid: Int?,

    @SerializedName("ts")
    val ts: Long?,

    @SerializedName("uuid")
    val uuid: String?

) : SmarticoWebSocketMessage