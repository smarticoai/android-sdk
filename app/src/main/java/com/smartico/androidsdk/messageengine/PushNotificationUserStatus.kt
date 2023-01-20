package com.smartico.androidsdk.messageengine

internal enum class PushNotificationUserStatus(val id: Int) {
    Allowed(0),
    Ask(1),
    Blocked(2),
    Suspended(3),
    NotSupported(4);

    companion object {
        fun pushNotificationUserStatus(value: Int) = values().first { it.id == value }
    }
}
