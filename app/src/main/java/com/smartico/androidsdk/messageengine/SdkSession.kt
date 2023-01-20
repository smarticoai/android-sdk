package com.smartico.androidsdk.messageengine

import com.smartico.androidsdk.model.response.IdentifyUserResponse
import com.smartico.androidsdk.model.response.InitSessionResponse

internal class SdkSession private constructor() {
    var sessionResponse: InitSessionResponse? = null
    var identifyUserResponse: IdentifyUserResponse? = null
    var labelName: String? = null
    var brandKey: String? = null
    var userExtId: String? = null

    companion object {
        val instance = SdkSession()
    }
}