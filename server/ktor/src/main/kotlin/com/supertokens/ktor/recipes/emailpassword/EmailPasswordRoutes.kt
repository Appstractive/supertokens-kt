package com.supertokens.ktor.recipes.emailpassword

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.emailPasswordRoutes(handler: EmailPasswordHandler) {

    post("/signin") {
        with(handler) {
            signIn()
        }
    }

    post("/signup") {
        with(handler) {
            signUp()
        }
    }

    get("/signup/email/exists") {
        with(handler) {
            emailExists()
        }
    }

    post("/user/password/reset/token") {
        with(handler) {
            passwordResetToken()
        }
    }

    post("/user/password/reset") {
        with(handler) {
            resetPassword()
        }
    }

}