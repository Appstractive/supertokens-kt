package com.appstractive

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.superTokensClient

interface Dependencies {
    val superTokensClient: SuperTokensClient
}

var dependencies = object: Dependencies {
    override val superTokensClient = superTokensClient("https://auth.appstractive.com") {

    }
}
