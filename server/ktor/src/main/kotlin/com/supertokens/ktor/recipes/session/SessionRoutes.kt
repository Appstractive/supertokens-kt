package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.ktor.utils.addSessionToResponse
import com.supertokens.ktor.utils.clearSessionInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.sessionRoutes(
    headerBasedSessions: Boolean,
    cookieBasedSessions: Boolean,
) {

    authenticate(SuperTokensAuth) {
        post("/signout") {
            val user =  call.requirePrincipal<AuthenticatedUser>(SuperTokensAuth)
            val session = sessions.getSession(user.sessionHandle)
            sessions.removeSessions(listOf(session.sessionHandle))
            clearSessionInResponse()
            call.respond(StatusResponse())
        }

        post("/session/refresh") {
            val refreshToken = call.request.headers["st-refresh-token"] ?: throw UnauthorizedException()
            val session = sessions.refreshSession(refreshToken)

            addSessionToResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                antiCsrfToken = session.antiCsrfToken,
                addToHeaders = headerBasedSessions,
                addToCookies = cookieBasedSessions,
            )

            call.respond(StatusResponse())
        }
    }
}