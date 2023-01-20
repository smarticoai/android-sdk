package com.smartico.androidsdk

import android.content.Context
import android.os.Looper
import android.webkit.WebView
import com.smartico.androidsdk.messageengine.ClassId
import com.smartico.androidsdk.messageengine.PushClientPlatform
import com.smartico.androidsdk.messageengine.PushNotificationUserStatus
import com.smartico.androidsdk.messageengine.SdkSession
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import com.smartico.androidsdk.model.request.IdentifyUserRequest
import com.smartico.androidsdk.model.request.InitSession
import com.smartico.androidsdk.model.request.UA
import com.smartico.androidsdk.network.WebSocketConnector
import com.smartico.androidsdk.ui.SmarticoWebView
import java.lang.ref.WeakReference
import java.net.URL

/*
https://docs.google.com/document/d/1UCeW-101nR4cnwXCd0Iw-dCICUpJyWHXgIujOgjBHrA/edit#
https://docs.google.com/document/d/1UxdF07JqKfsEhAwikvNhVIyqi3Zkj9rxPyl9HsXd8PA/edit#
 */
class SmarticoSdk private constructor() {

    companion object {
        val instance = SmarticoSdk()
        val libraryVersion = "1.0.1"
        internal val os = "Android"

        internal val dpkGamification = "dp:gf"
    }

    private var webSocketConnector: WebSocketConnector? = null
    internal lateinit var context: WeakReference<Context>
    var listener: SmarticoSdkListener? = null

    fun init(context: Context, label: String, brand: String) {
        log("initialize")
        this.context = WeakReference(context)
        SdkSession.instance.labelName = label
        SdkSession.instance.brandKey = brand

        webSocketConnector = WebSocketConnector()
        webSocketConnector?.startConnector()
        webSocketConnector?.sendMessage(
            InitSession(
                cid = ClassId.InitRequest.id,
                labelName = label,
                brandKey = brand,
                deviceId = OSUtils.deviceId(),
                page = null,
                trackerVersion = libraryVersion,
                sessionId = OSUtils.generateNextRandomId(),
                ua = generateUA()
            )
        )
    }

    fun online(userId: String, language: String) {
        SdkSession.instance.userExtId = userId
        // TODO: What is the language param for
        // TODO: Get token from firebase and check permissions for push for pushNotificationUserStatus param
        val request = IdentifyUserRequest(
            extUserId = userId, token = "testToken",
            platform = PushClientPlatform.NativeIOS.id,
            pushNotificationUserStatus = PushNotificationUserStatus.Allowed.id,
            page = null,
            ua = generateUA()
        )
        webSocketConnector?.sendMessage(request)
    }

    fun executeDeeplink(context: Context, link: String, callback: ((WebView) -> Unit)) {
        android.os.Handler(Looper.getMainLooper()).post {
            if(link == dpkGamification) {
                SdkSession.instance.sessionResponse?.settings?.gamificationWrapperPage?.let { url ->
                    if (url.isNotEmpty()) {
                        val labelName = SdkSession.instance.labelName ?: ""
                        val brandKey = SdkSession.instance.brandKey ?: ""
                        val userExtId = SdkSession.instance.userExtId ?: ""

                        val finalUrl =  "$url?label_name=$labelName&brand_key=$brandKey&user_ext_id=$userExtId"
                        val webView = SmarticoWebView(context)
                        webView.executeDpk(finalUrl)
                        callback(webView)
                    }
                }
            } else {
                log("unknown deeplink: $link")
            }
        }
    }

    internal fun handleEngagementEvent(event: ClientEngagementEvent) {
        context.get()?.let { ctx ->
            executeDeeplink(ctx, dpkGamification) { webView ->
                (webView as? SmarticoWebView)?.let {
                    it.onClientEngagementEvent(event)
                    listener?.onEngagementEventAvailable(webView)
                }
            }
        }
    }

    internal fun forwardToServer(msg: String) {
        webSocketConnector?.forwardMessage(msg)
    }


    private fun generateUA(): UA {
        val smallestWidthDp = context.get()?.resources?.configuration?.smallestScreenWidthDp ?: -1
        val deviceType = if (smallestWidthDp > 600) {
            "NATIVE_TABLET"
        } else {
            "NATIVE_PHONE"
        }
        return UA(
            fp = null,
            agent = "SmarticoAndroidSDK/$libraryVersion",
            host = null,
            deviceType = deviceType,
            tzoffset = OSUtils.timezoneOffsetInMins(),
            os = os
        )
    }

}

interface SmarticoSdkListener {
    fun onEngagementEventAvailable(webView: WebView)
    fun closeWebView(webView: WebView)

    fun onConnected()
    fun onOnline()
    fun onDisconnected()
}

internal fun log(msg: String) {
    println("SmartiCoSDK: $msg")
}

internal fun log(throwable: Throwable) {
    println("SmartiCoSDK: Error")
    throwable.printStackTrace()
}
