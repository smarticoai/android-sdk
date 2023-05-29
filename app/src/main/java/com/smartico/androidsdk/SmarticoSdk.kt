package com.smartico.androidsdk

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.get
import com.smartico.androidsdk.messageengine.PushClientPlatform
import com.smartico.androidsdk.messageengine.PushNotificationUserStatus
import com.smartico.androidsdk.messageengine.SdkSession
import com.smartico.androidsdk.model.request.ChangeUserSettingsEvent
import com.smartico.androidsdk.model.request.ChangeUserSettingsEventPayload
import com.smartico.androidsdk.model.request.ClientEngagementEvent
import com.smartico.androidsdk.model.request.IdentifyUserRequest
import com.smartico.androidsdk.model.request.InitSession
import com.smartico.androidsdk.model.request.UA
import com.smartico.androidsdk.model.response.TriggerMiniGameResponse
import com.smartico.androidsdk.network.WebSocketConnector
import com.smartico.androidsdk.ui.SmarticoWebView
import java.lang.ref.WeakReference
import java.net.URL


/*
https://docs.google.com/document/d/1UCeW-101nR4cnwXCd0Iw-dCICUpJyWHXgIujOgjBHrA/edit#
https://docs.google.com/document/d/1UxdF07JqKfsEhAwikvNhVIyqi3Zkj9rxPyl9HsXd8PA/edit#
https://demo.smartico.ai/
 */
class SmarticoSdk private constructor() {

    companion object {
        val instance = SmarticoSdk()
        val libraryVersion = "1.0.1"
        internal val os = "Android"

    }

    private var webSocketConnector: WebSocketConnector? = null
    internal lateinit var context: WeakReference<Context>
    var listener: SmarticoSdkListener? = null
    private var shouldRetryToReconnect: Boolean = false
    private var networkMonitorStarted: Boolean = false
    private var gamificationHolder: WeakReference<ViewGroup>? = null
    private var popupHolder: WeakReference<ViewGroup>? = null

    fun init(context: Context, label: String, brand: String) {
        log("initialize")
        this.context = WeakReference(context)
        SdkSession.instance.labelKey = label
        SdkSession.instance.brandKey = brand
        setupConnectivityMonitor()

        webSocketConnector = WebSocketConnector()
        webSocketConnector?.startConnector()
        webSocketConnector?.sendMessage(
            InitSession(
                labelKey = label,
                brandKey = brand,
                deviceId = OSUtils.deviceId(),
                page = null,
                trackerVersion = libraryVersion,
                sessionId = OSUtils.generateNextRandomId(),
                ua = generateUA()
            )
        )
    }

    private fun reconnect() {
        context.get()?.let {
            log("reconnect")
            logout(it)
            val session = SdkSession.instance
            val userExtId = session.userExtId
            val language = session.language
            if (userExtId != null && language != null) {
                log("online")
                online(userExtId, language)
            }
            return
        }
        log("reconnect -> no context")
    }

    fun logout(context: Context) {
        try {
            if (SdkSession.instance.brandKey == null) {
                // This means init was not called so we don't have anything to clear here
                log("no session present -> skip logout")
                return
            }
            webSocketConnector?.stopConnector()
            log("logout -> socket closed")
            val oldBrand = SdkSession.instance.brandKey
            val oldLabel = SdkSession.instance.labelKey
            SdkSession.instance.clearSession()
            log("logout -> session cleared")
            if (oldBrand != null && oldLabel != null) {
                log("logout -> reinitialize")
                init(context, oldLabel, oldBrand)
            }
        } catch (e: java.lang.Exception) {
            log(e)
        }
    }

    internal fun changeUserLanguage(event: ChangeUserSettingsEvent) {
        webSocketConnector?.sendMessage(event)
    }

    // triggerEngagementEvent and triggerMiniGameEvent shoudn't be part of SDK
    // SDK should expose a method "event" that is accepting 2 parameters - eventType and any kind of JSON object.
    // and it will be called from the Sample app in the following way
    // .event("client_action", { "action": "native_trigger_popup" }
    // .event("client_action", { "action": "native_trigger_saw" }

    // changeUserLanguage is part of SDK and in fact just a wrapper to the ".event" method,
    // it should look like
    // changeUserLanguage(language: String) {
    //      .event("core_language_changed", { "language": language })
    // }

    fun triggerEngagementEvent() {
        webSocketConnector?.sendMessage(
            ChangeUserSettingsEvent(
                eventType = "client_action", payload = ChangeUserSettingsEventPayload(
                    action = "native_trigger_popup"
                )
            )
        )
    }

    fun triggerMiniGameEvent() {
        webSocketConnector?.sendMessage(
            ChangeUserSettingsEvent(
                eventType = "client_action", payload = ChangeUserSettingsEventPayload(
                    action = "native_trigger_saw"
                )
            )
        )
    }

    fun setGamificationHolder(container: ViewGroup) {
        gamificationHolder = WeakReference(container)
    }

    fun setPopupHolder(container: ViewGroup) {
        popupHolder = WeakReference(container)
    }

    private fun addToContainer(webView: WebView, container: ViewGroup) {
        when (container) {
            is LinearLayout -> {
                container.addView(
                    webView,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }

            is RelativeLayout -> {
                container.addView(
                    webView,
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }

            is FrameLayout -> {
                container.addView(
                    webView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }

            else -> {
                container.addView(webView)
            }
        }
    }

    fun online(userId: String, language: String) {
        SdkSession.instance.userExtId = userId
        SdkSession.instance.language = language
        // TODO: What is the language param for
        // AA> when user is identified, we will get in the public properties current language of user
        // AA> if this language is different from what is passed in the "online", then we need to send event to change it
        // AA> put example in google doc
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

    fun executeDeeplink(context: Context, link: String) {
        this.executeDeeplink(context, link, "")
    }

    fun executeDeeplink(context: Context, link: String, query: String) {
        gamificationHolder?.get()?.let {
            executeDeeplink(context, link, it, query)
        }
    }

    private fun executeDeeplink(
        context: Context,
        link: String,
        viewGroup: ViewGroup,
        queryString: String
    ) {
        android.os.Handler(Looper.getMainLooper()).post {
            SdkSession.instance.sessionResponse?.settings?.gamificationWrapperPage?.let { url ->
                if (url.isNotEmpty()) {
                    // AA: pass known deep-links to gamification widget as part of URL
                    val finalUrl =
                        "$url?$queryString"
                    val webView = SmarticoWebView(context)
                    webView.setBackgroundColor(Color.TRANSPARENT)
                    webView.executeDpk(finalUrl)
                    addToContainer(webView, viewGroup)
                }
            }
        }
    }

    internal fun triggerMiniGame(response: TriggerMiniGameResponse) {
        context.get()?.let {
            val session = SdkSession.instance
            val dp = "dp:gf_saw&id=${response.sawTemplateId}&standalone=true"
            val query = "label_name=${Uri.encode(session.labelKey)}&brand_key=${Uri.encode(session.brandKey)}&user_ext_id=${Uri.encode(session.userExtId)}&$dp"
            executeDeeplink(it, "", query)
        }
    }

    internal fun handleEngagementEvent(event: ClientEngagementEvent) {
        popupHolder?.get()?.let { popupHolderView ->
            android.os.Handler(Looper.getMainLooper()).post {
                log("handleEngagementEvent holder ok")
                var webView: SmarticoWebView? = null
                if (popupHolderView.childCount > 0) {
                    popupHolderView[0].let { view ->
                        if (view is SmarticoWebView) {
                            log("handleEngagementEvent holder ok")
                            webView = view
                        }
                    }
                } else {
                    context.get()?.let {
                        val wv = SmarticoWebView(it)
                        wv.setBackgroundColor(Color.TRANSPARENT)
                        addToContainer(wv, popupHolderView)
                        webView = wv
                    }
                }
                SdkSession.instance.sessionResponse?.settings?.engagementWrapperPage?.let {
                    popupHolderView.visibility = View.VISIBLE
                    popupHolderView.bringToFront()
                    webView?.executeDpk(it)
                    event.bcid = SmarticoWebView.BridgeMessageInitializeWithEngagementEvent
                    webView?.onClientEngagementEvent(event)
                }
            }
        }
    }

    fun hidePopup() {
        popupHolder?.get()?.visibility = View.GONE
    }

    internal fun forwardToServer(msg: String) {
        webSocketConnector?.forwardMessage(msg)
    }

    internal fun bundleId(): String {
        return context.get()?.applicationContext?.packageName ?: ""
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

    private fun setupConnectivityMonitor() {
        if (networkMonitorStarted) {
            return
        }
        try {
            val ctx = context.get() ?: return
            val networkCallback: NetworkCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    log("network -> onAvailable() shouldRetryToReconnect=$shouldRetryToReconnect")
                    if (shouldRetryToReconnect) {
                        shouldRetryToReconnect = false
                        reconnect()
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    val session = SdkSession.instance
                    if (session.brandKey != null) {
                        shouldRetryToReconnect = true
                    }
                    log("network -> onLost() shouldRetryToReconnect=$shouldRetryToReconnect")
                }

            }
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            ctx.getSystemService(ConnectivityManager::class.java).let {
                it.requestNetwork(networkRequest, networkCallback)
                log("network -> register callback")
                networkMonitorStarted = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface SmarticoSdkListener {
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
