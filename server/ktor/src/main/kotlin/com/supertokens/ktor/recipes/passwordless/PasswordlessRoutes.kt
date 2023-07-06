package com.supertokens.ktor.recipes.passwordless

import com.supertokens.sdk.common.Routes
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.passwordlessRoutes(
    handler: PasswordlessHandler,
) {

    post(Routes.Passwordless.SIGNUP_CODE) {
        with(handler) {
            startSignInUp()
        }
    }

    post(Routes.Passwordless.SIGNUP_CODE_RESEND) {
        with(handler) {
            resendCode()
        }
    }

    post(Routes.Passwordless.SIGNUP_CODE_CONSUME) {
        with(handler) {
            consumeCode()
        }
    }

}