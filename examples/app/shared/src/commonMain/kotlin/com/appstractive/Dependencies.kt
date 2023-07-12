package com.appstractive

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.superTokensClient

interface Dependencies {
    val apiClient: SuperTokensClient
}

var dependencies = object: Dependencies {
    override val apiClient = superTokensClient("https://auth.appstractive.com") {

    }
}
