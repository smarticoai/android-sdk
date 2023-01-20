package com.smartico.androidsdk.messageengine

internal enum class PushClientPlatform(val id: Int) {
    ChromeDesktop(0),
    FirefoxDesktop(1),
    EdgeDesktop(2),
    OperaDesktop(3),
    SafariDesktop(4),
    Other(5),
    NativeIOS(6),
    NativeAndroid(7),
    ChromeMobile(8),
    FirefoxMobile(9),
    SafariMobile(10),
    OperaMobile(11);

    companion object {
        fun platform(value: Int) = values().first { it.id == value }
    }
}