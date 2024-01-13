package com.appstractive

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.sessions.Session
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.Google
import com.supertokens.sdk.superTokensClient

interface Dependencies {
    val superTokensClient: SuperTokensClient
}

var dependencies = object: Dependencies {
    override val superTokensClient = superTokensClient("https://auth.appstractive.com") {
        recipe(Session)
        recipe(EmailPassword)
        recipe(Passwordless)
        recipe(EmailVerification)
        recipe(ThirdParty) {
            provider(Google) {
                redirectUri = "localhost"
            }
        }
    }
}
