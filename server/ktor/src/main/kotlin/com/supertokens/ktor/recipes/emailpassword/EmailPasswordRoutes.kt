package com.supertokens.ktor.recipes.emailpassword

import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.emailPasswordRoutes(handler: EmailPasswordHandler) {

    post("/signin") {
        handler.signIn(call)
    }

    post("/signup") {
        handler.signUp(call)
    }

    get("/signup/email/exists") {
        handler.emailExists(call)
    }

    post("/user/password/reset/token") {
        handler.passwordResetToken(call)
    }

    post("/user/password/reset") {
        handler.resetPassword(call)
    }

}