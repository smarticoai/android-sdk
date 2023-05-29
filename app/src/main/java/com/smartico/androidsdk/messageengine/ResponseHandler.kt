package com.smartico.androidsdk.messageengine

import com.google.gson.Gson
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.log
import com.smartico.androidsdk.model.request.BaseRequest
import com.smartico.androidsdk.model.request.ChangeUserSettingsEvent
import com.smartico.androidsdk.model.request.ChangeUserSettingsEventPayload
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import com.smartico.androidsdk.model.response.IdentifyUserResponse
import com.smartico.androidsdk.model.response.InitSessionResponse
import com.smartico.androidsdk.model.response.TriggerMiniGameResponse
import com.smartico.androidsdk.network.WebSocketConnector
import org.json.JSONObject
import java.lang.ref.WeakReference


internal class ResponseHandler(connector: WebSocketConnector) {
    private val gson = Gson()
    private val connectorRef: WeakReference<WebSocketConnector>

    init {
        connectorRef = WeakReference(connector)
    }


    fun handleMessage(string: String) {
        try {
            val obj = JSONObject(string)
            val cid = obj.optInt("cid", -1)
            if (cid >= 0) {
                handleSdkMessage(cid, string)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleSdkMessage(cid: Int, string: String) {
        when (cid) {
            ClassId.InitResponse.id -> {
                SdkSession.instance.sessionResponse =
                    gson.fromJson(string, InitSessionResponse::class.java)
            }
            ClassId.Ping.id -> {
                val pong = BaseRequest(cid = ClassId.Pong.id)
                connectorRef.get()?.sendMessage(pong)
            }
            ClassId.IdentifyResponse.id -> {
                val resp = gson.fromJson(string, IdentifyUserResponse::class.java)
                SdkSession.instance.identifyUserResponse = resp
                SmarticoSdk.instance.listener?.onOnline()

                resp.props?.get("core_user_language")?.let { serverUserLang ->
                    val language = SdkSession.instance.language ?: ""
                    if (language.isNotEmpty() && language != serverUserLang) {
                        log("serverUserLang=$serverUserLang language=$language -> update")
                        SmarticoSdk.instance.changeUserLanguage(
                            ChangeUserSettingsEvent(
                                payload = ChangeUserSettingsEventPayload(
                                    language = language
                                )
                            )
                        )
                    }
                }
            }
            ClassId.ClientEngagementEvent.id -> {
                log("received client engagement event")
                val clientEngagementEvent = gson.fromJson(string, ClientEngagementEvent::class.java)
                SmarticoSdk.instance.handleEngagementEvent(clientEngagementEvent)
            }
            ClassId.TriggerMiniGame.id -> {
                log("trigger mini game")
                val triggerMiniGameResponse = gson.fromJson(string, TriggerMiniGameResponse::class.java)
                SmarticoSdk.instance.triggerMiniGame(triggerMiniGameResponse)
            }

        }
    }

}