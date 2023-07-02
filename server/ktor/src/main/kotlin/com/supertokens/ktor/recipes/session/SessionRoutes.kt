package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.SuperTokensAuth
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.sessionRoutes(
    handler: SessionHandler,
) {

    authenticate(SuperTokensAuth) {
        post("/signout") {
            with(handler) {
                signOut()
            }
        }
    }

    post("/session/refresh") {
        with(handler) {
            refresh()
        }
    }
}