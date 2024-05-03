package com.appstractive

import androidx.compose.runtime.staticCompositionLocalOf
import com.appstractive.screens.auth.AuthScreenPresenterFactory
import com.appstractive.screens.auth.AuthScreenUiFactory
import com.appstractive.screens.auth.mfa.MfaScreenPresenterFactory
import com.appstractive.screens.auth.mfa.MfaScreenUiFactory
import com.appstractive.screens.home.HomeScreenPresenterFactory
import com.appstractive.screens.home.HomeScreenUiFactory
import com.slack.circuit.foundation.Circuit
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuth
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.sessions.Session
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.Google
import com.supertokens.sdk.recipes.totp.Totp
import com.supertokens.sdk.superTokensClient

interface Dependencies {
    val superTokensClient: SuperTokensClient

    val circuit: Circuit
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
        recipe(Totp)
        recipe(MultiFactorAuth)
    }

    override val circuit =
        Circuit.Builder()
            .addPresenterFactory(
                AppScreenPresenterFactory(),
                AuthScreenPresenterFactory(),
                MfaScreenPresenterFactory(),
                HomeScreenPresenterFactory(),
            )
            .addUiFactory(
                AppScreenUiFactory(),
                AuthScreenUiFactory(),
                MfaScreenUiFactory(),
                HomeScreenUiFactory(),
            )
            .build()
}

val LocalDependencies =
    staticCompositionLocalOf<Dependencies> {
        throw IllegalStateException("No Dependencies Provided")
    }
