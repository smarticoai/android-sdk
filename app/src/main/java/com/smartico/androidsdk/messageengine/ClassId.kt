package com.smartico.androidsdk.messageengine

internal enum class ClassId(val id: Int) {
    Ping(1),
    Pong(2),
    InitRequest(3),
    InitResponse(4),
    IdentifyRequest(5),
    IdentifyResponse(6),
    ClientEngagementEvent(110);


    companion object {
        fun classId(value: Int) = values().first { it.id == value }
    }
}