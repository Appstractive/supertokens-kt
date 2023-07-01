package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.clearSessionInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.sessionRoutes(
    handler: SessionHandler,
) {

    authenticate(SuperTokensAuth) {
        post("/signout") {
            handler.signout(call)
        }
    }

    post("/session/refresh") {
        handler.refresh(call)
    }
}