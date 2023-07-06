package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.sdk.common.Routes
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.emailPasswordRoutes(handler: EmailPasswordHandler) {

    post(Routes.EmailPassword.SIGN_IN) {
        with(handler) {
            signIn()
        }
    }

    post(Routes.EmailPassword.SIGN_UP) {
        with(handler) {
            signUp()
        }
    }

    post(Routes.EmailPassword.PASSWORD_RESET_TOKEN) {
        with(handler) {
            passwordResetToken()
        }
    }

    post(Routes.EmailPassword.PASSWORD_RESET) {
        with(handler) {
            resetPassword()
        }
    }

}