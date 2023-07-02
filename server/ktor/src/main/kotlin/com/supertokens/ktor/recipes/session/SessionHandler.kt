package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.ktor.utils.clearSessionInResponse
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

open class SessionHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.signOut() {
        val user =  call.requirePrincipal<AuthenticatedUser>()
        val session = sessions.getSession(user.sessionHandle)
        sessions.removeSessions(listOf(session.sessionHandle))
        clearSessionInResponse()
        call.respond(StatusResponse())
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.refresh() {
        val refreshToken = call.request.headers["st-refresh-token"] ?: call.request.cookies["sRefreshToken"] ?: throw UnauthorizedException()
        val session = sessions.refreshSession(refreshToken)

        setSessionInResponse(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            antiCsrfToken = session.antiCsrfToken,
        )

        call.respond(StatusResponse())
    }

}