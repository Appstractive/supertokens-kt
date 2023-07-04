package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.SuperTokensAuth
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.emailVerificationRoutes(
    handler: EmailVerificationHandler,
) {

    authenticate(SuperTokensAuth) {
        post("/user/email/verify/token") {
            with(handler) {
                sendEmailVerification()
            }
        }

        get("/user/email/verify") {
            with(handler) {
                checkEmailVerified()
            }
        }

    }

    post("/user/email/verify") {
        with(handler) {
            verifyEmail()
        }
    }

}