package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.ktor.utils.clearSessionInResponse
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ANTI_CSRF
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

open class SessionHandler(
    protected val scope: CoroutineScope,
) {

    /**
     * A call to POST /signout
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Session%20Recipe/signout">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.signOut() {
        val user =  call.requirePrincipal<AuthenticatedUser>()
        val session = sessions.getSession(user.sessionHandle)
        sessions.removeSessions(listOf(session.sessionHandle))
        clearSessionInResponse()
        call.respond(StatusResponse())
    }

    /**
     * A call to POST /session/refresh
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Session%20Recipe/refresh">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.refresh() {
        val refreshToken = call.request.headers[HEADER_REFRESH_TOKEN] ?: call.request.cookies[COOKIE_REFRESH_TOKEN] ?: throw UnauthorizedException()
        val antiCsrfToken = call.request.headers[HEADER_ANTI_CSRF]
        val session = kotlin.runCatching {
            sessions.refreshSession(
                refreshToken = refreshToken,
                antiCsrfToken = antiCsrfToken,
            )
        }.getOrElse {
            clearSessionInResponse()
            return call.respond(HttpStatusCode.Unauthorized)
        }

        setSessionInResponse(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            antiCsrfToken = session.antiCsrfToken,
        )

        call.respond(StatusResponse())
    }

}