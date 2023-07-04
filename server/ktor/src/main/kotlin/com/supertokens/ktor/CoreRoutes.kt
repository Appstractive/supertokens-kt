package com.supertokens.ktor

import com.supertokens.ktor.recipes.emailpassword.isEmailPasswordEnabled
import com.supertokens.ktor.recipes.passwordless.passwordlessEnabled
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.coreRoutes(
    handler: CoreHandler,
) {

    if(passwordlessEnabled) {
        get("/signup/phonenumber/exists") {
            with(handler) {
                phoneNumberExists()
            }
        }
    }

    if(passwordlessEnabled || isEmailPasswordEnabled) {
        get("/signup/email/exists") {
            with(handler) {
                emailExists()
            }
        }
    }

}