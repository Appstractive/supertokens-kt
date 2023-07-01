package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.ktor.utils.clearSessionInResponse
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

open class SessionHandler {

    open suspend fun signout(call: ApplicationCall) {
        val user =  call.requirePrincipal<AuthenticatedUser>(SuperTokensAuth)
        val session = call.sessions.getSession(user.sessionHandle)
        call.sessions.removeSessions(listOf(session.sessionHandle))
        call.clearSessionInResponse()
        call.respond(StatusResponse())
    }

    open suspend fun refresh(call: ApplicationCall) {
        val refreshToken = call.request.headers["st-refresh-token"] ?: call.request.cookies["sRefreshToken"] ?: throw UnauthorizedException()
        val session = call.sessions.refreshSession(refreshToken)

        call.setSessionInResponse(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            antiCsrfToken = session.antiCsrfToken,
        )

        call.respond(StatusResponse())
    }

}