package com.smartico.androidsdk.messageengine

import com.google.gson.Gson
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import com.smartico.androidsdk.model.response.IdentifyUserResponse
import com.smartico.androidsdk.model.response.InitSessionResponse
import com.smartico.androidsdk.model.response.Pong
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
                val pong = Pong()
                connectorRef.get()?.sendMessage(pong)
            }
            ClassId.IdentifyResponse.id -> {
                SdkSession.instance.identifyUserResponse =
                    gson.fromJson(string, IdentifyUserResponse::class.java)
                SmarticoSdk.instance.listener?.onOnline()
            }
            ClassId.ClientEngagementEvent.id -> {
                val clientEngagementEvent = gson.fromJson(string, ClientEngagementEvent::class.java)
                SmarticoSdk.instance.handleEngagementEvent(clientEngagementEvent)
            }

        }
    }

}