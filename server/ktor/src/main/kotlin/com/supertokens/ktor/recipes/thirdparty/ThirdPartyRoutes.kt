package com.supertokens.ktor.recipes.thirdparty

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.thirdPartyRoutes(
    handler: ThirdPartyHandler,
) {

    post("/signinup") {
        with(handler) {
            signInUp()
        }
    }

    get("/authorisationurl") {
        with(handler) {
            getAuthorizationUrl()
        }
    }

    post("/callback/apple") {
        with(handler) {
            appleAuthCallback()
        }
    }

}