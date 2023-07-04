package com.supertokens.ktor.recipes.passwordless

import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.passwordlessRoutes(
    handler: PasswordlessHandler,
) {

    post("/signinup/code") {
        with(handler) {
            startSignInUp()
        }
    }

    post("/signinup/code/resend") {
        with(handler) {
            resendCode()
        }
    }

    post("/signinup/code/consume") {
        with(handler) {
            consumeCode()
        }
    }

}