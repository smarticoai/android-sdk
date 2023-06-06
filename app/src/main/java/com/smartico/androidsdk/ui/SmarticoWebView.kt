package com.smartico.androidsdk.ui

import android.content.Context
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.GsonBuilder
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.log
import com.smartico.androidsdk.messageengine.SdkSession
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import org.json.JSONObject


internal class SmarticoWebView(context: Context) : WebView(context) {
    private var bridgeUp = false
    private var readyToBeShown = false
    private val uiHandler = android.os.Handler(Looper.getMainLooper())
    private var pendingOperations: ArrayList<(() -> Unit)> = ArrayList()

    fun executeDpk(url: String) {
        logWv("open webView with url=$url")
        if(!readyToBeShown) {
            this.visibility = View.INVISIBLE
            this.webViewClient = WebViewClient()
            this.webChromeClient = WebChromeClient()
            this.settings.javaScriptEnabled = true
            this.settings.setSupportMultipleWindows(true)
            addJavascriptInterface(this, "SmarticoBridge")
        }
        if(this.parent is ViewGroup) {
            (this.parent as ViewGroup).visibility = VISIBLE
        }
        this.loadUrl(url)
    }

    @JavascriptInterface
    fun postMessage(message: String): Boolean {
        uiHandler.post {
            try {
                logWv("Webview incoming message: $message")
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
            logWv("send $message")
            loadUrl("javascript:window.postMessage($message, '*');")
        }
    }

    private fun logWv(msg: String) {
        log("${this.hashCode()} - $msg")
    }


    private fun handleMessage(message: String) {
        val msg = JSONObject(message)
        val classId: Int = msg.getInt("bcid")
        if (classId == BridgeMessagePageReady) {
            bridgeUp = true
            pendingOperations.forEach {
                it()
            }
            pendingOperations.clear()
        } else if (classId == BridgeMessageCloseMe) {
            SmarticoSdk.instance.hidePopup()
        } else if (classId == BridgeMessageExecuteDeeplink) {
            val dpk = msg.optString("dp", "")
            var hasMatch = false
            for (value in DPK.values()) {
                if(value.id == dpk) {
                    hasMatch = true
                }
            }
            if (dpk.isNotEmpty() && hasMatch) {
                val sessionInstance = SdkSession.instance
                val labelKey = sessionInstance.labelKey ?: ""
                val brandKey = sessionInstance.brandKey ?: ""
                val userExtId = sessionInstance.userExtId ?: ""
                val query =
                    "label_name=$labelKey&brand_key=$brandKey&user_ext_id=$userExtId&dp=$dpk"
                SmarticoSdk.instance.openDeeplink(query)
            }
        } else if (classId == BridgeMessageReadyToBeShown) {
            logWv("show webview")
            this.visibility = View.VISIBLE
            this.readyToBeShown = true
        } else if (classId == BridgeMessageSendToSocket) {
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
enum class DPK(val id: String) {
    GfMain("gf"),
    Activity("gf_activity"),
    Missions("gf_missions"),
    Badges("gf_badges"),
    LeaderBoard("gf_board"),
    Tournaments("gf_tournaments"),
    LeaderBoardPrev("gf_board_previous"),
    LeaderBoardRules("gf_board_rules"),
    Bonuses("gf_bonuses"),
    Levels("gf_levels"),
    Saw("gf_saw"),
    Store("gf_store"),
    Settings("gf_settings"),
    Section("gf_section"),
    ChangeNickname("gf_change_nickname"),
    ChangeAvatar("gf_change_avatar"),
}