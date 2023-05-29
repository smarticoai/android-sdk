package com.smartico.androidsdk.ui

import android.content.Context
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.log
import com.smartico.androidsdk.messageengine.SdkSession
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import org.json.JSONObject


internal class SmarticoWebView(context: Context) : WebView(context) {
    private var bridgeUp = false
    private val uiHandler = android.os.Handler(Looper.getMainLooper())
    private var pendingOperations: ArrayList<(() -> Unit)> = ArrayList()

    fun executeDpk(url: String) {
        log("open webView with url=$url")
        this.visibility = View.INVISIBLE
        this.webViewClient = WebViewClient()
        this.webChromeClient = WebChromeClient()
        this.settings.javaScriptEnabled = true
        this.settings.setSupportMultipleWindows(true)


        addJavascriptInterface(this, "SmarticoBridge")
        this.loadUrl(url)
    }

    @JavascriptInterface
    fun postMessage(message: String): Boolean {
        uiHandler.post {
            try {
                log("Webview message: $message")
                handleMessage(message)
            } catch (e: Exception) {
                log(e)
            }
        }
        return true
    }

    fun onClientEngagementEvent(event: ClientEngagementEvent) {
        val gson = GsonBuilder().create()
        val msg = gson.toJson(event)
        if (bridgeUp) {
            send(msg)
        } else {
            pendingOperations.add {
                send(msg)
            }
        }
    }

    private fun send(message: String) {
        post {
            log("postMessage $message")
            loadUrl("javascript:window.postMessage($message, '*');")
//            evaluateJavascript("window.postMessage($message)", null)
        }
    }


    private fun handleMessage(message: String) {
        val msg = JSONObject(message)
        val classId: Int = msg.getInt("bcid")
        if(classId == BridgeMessagePageReady) {
            bridgeUp = true
            pendingOperations.forEach {
                it()
            }
            pendingOperations.clear()
        } else if(classId == BridgeMessageCloseMe) {
            SmarticoSdk.instance.hidePopup()
        } else if(classId == BridgeMessageExecuteDeeplink) {
            val dpk = msg.optString("dp", "")
            if(dpk.isNotEmpty()) {
                SmarticoSdk.instance.context.get()?.let {
                    val sessionInstance = SdkSession.instance
                    val labelKey = sessionInstance.labelKey ?: ""
                    val brandKey = sessionInstance.brandKey ?: ""
                    val userExtId = sessionInstance.userExtId ?: ""
                    val query = "label_name=$labelKey&brand_key=$brandKey&user_ext_id=$userExtId&dp=$dpk"
                    SmarticoSdk.instance.executeDeeplink(it, dpk, query)
                }
            }
        } else if(classId == BridgeMessageReadyToBeShown) {
            this.visibility = View.VISIBLE
        } else if(classId == BridgeMessageSendToSocket) {
            SmarticoSdk.instance.forwardToServer(message)
        }
    }

    companion object {
        private const val BridgeMessagePageReady = 1
        private const val BridgeMessageCloseMe = 2
        const val BridgeMessageInitializeWithEngagementEvent = 3
        private const val BridgeMessageExecuteDeeplink = 4
        private const val BridgeMessageReadyToBeShown = 5
        private const val BridgeMessageSendToSocket = 6
    }
}