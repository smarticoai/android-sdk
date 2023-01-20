package com.smartico.androidsdk.network

import com.google.gson.Gson
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.log
import com.smartico.androidsdk.messageengine.ResponseHandler
import com.smartico.androidsdk.model.SmarticoWebSocketMessage
import okhttp3.*
import okio.ByteString


internal class WebSocketConnector: WebSocketListener() {
    private val normalCloseStatus = 1000
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isOpen = false
    private val gson = Gson()
    private val responseHandler = ResponseHandler(this)

    fun startConnector() {
        synchronized(this) {
            val request: Request = Request.Builder().url(serviceUrl()).build()
            webSocket = client.newWebSocket(request, this)
        }
    }

    private fun serviceUrl(): String {
        // TODO: What to put as domain?
        val domain = "androidsmarticosdk.com"
        val codeVersion = SmarticoSdk.libraryVersion
        return "wss://api.smartico.ai/websocket/services?master&domain=$domain&version=$codeVersion"
    }

    fun stopConnector() {
        synchronized(this) {
            client.dispatcher.executorService.shutdown()
        }
    }

    fun sendMessage(msg: SmarticoWebSocketMessage) {
        synchronized(this) {
            val json = gson.toJson(msg)
            log("Sending: $json")
            webSocket?.send(json)
        }
    }

    fun forwardMessage(msg: String) {
        synchronized(this) {
            log("Forwarding: $msg")
            webSocket?.send(msg)
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        synchronized(this) {
            isOpen = true
            SmarticoSdk.instance.listener?.onConnected()
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        log("Receiving: $text")
        responseHandler.handleMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        log("Receiving: " + bytes.hex());
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        synchronized(this) {
            webSocket.close(normalCloseStatus, null)
            log("Closing: $code $reason")
            SmarticoSdk.instance.listener?.onDisconnected()
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        synchronized(this) {
            super.onFailure(webSocket, t, response)
            log(t)
            SmarticoSdk.instance.listener?.onDisconnected()
        }
    }
}