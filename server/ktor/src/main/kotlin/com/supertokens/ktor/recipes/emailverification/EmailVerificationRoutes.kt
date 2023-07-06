package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.sdk.common.Routes
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.emailVerificationRoutes(
    handler: EmailVerificationHandler,
) {

    authenticate(SuperTokensAuth) {
        post(Routes.EmailVerification.VERIFY_TOKEN) {
            with(handler) {
                sendEmailVerification()
            }
        }

        get(Routes.EmailVerification.CHECK_VERIFIED) {
            with(handler) {
                checkEmailVerified()
            }
        }

    }

    post(Routes.EmailVerification.VERIFY) {
        with(handler) {
            verifyEmail()
        }
    }

}