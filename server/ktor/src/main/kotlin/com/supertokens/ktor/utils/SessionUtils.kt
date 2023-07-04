package com.supertokens.ktor.utils

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.models.Token
import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.date.GMTDate
import io.ktor.util.pipeline.PipelineContext

fun PipelineContext<Unit, ApplicationCall>.setSessionInResponse(
    accessToken: Token,
    refreshToken: Token? = null,
    antiCsrfToken: String? = null,
) {
    if (sessions.headerBasedSessions) {
        val exposeHeaders = mutableListOf(
            "st-access-token",
        )

        call.response.headers.append(
            "st-access-token",
            accessToken.token,
        )

        refreshToken?.let {
            call.response.headers.append(
                "st-refresh-token",
                it.token,
            )
            exposeHeaders.add("st-refresh-token")
        }

        antiCsrfToken?.let {
            call.response.headers.append(
                "anti-csrf",
                it,
            )
            exposeHeaders.add("anti-csrf")
        }

        call.response.headers.append(
            "Access-Control-Expose-Headers",
            exposeHeaders.joinToString(", "),
        )
    }

    if (sessions.cookieBasedSessions) {
        call.response.cookies.append(
            Cookie(
                name = "sAccessToken",
                value = accessToken.token,
                domain = sessions.cookieDomain,
                httpOnly = true,
                expires = GMTDate(accessToken.expiry),
                path = "/",
            )
        )

        refreshToken?.let {
            val basePath = superTokens.appConfig.websiteBasePath
            call.response.cookies.append(
                Cookie(
                    name = "sRefreshToken",
                    value = it.token,
                    domain = sessions.cookieDomain,
                    httpOnly = true,
                    expires = GMTDate(it.expiry),
                    path = "${basePath}/session/refresh",
                )
            )
        }
    }

}

fun PipelineContext<Unit, ApplicationCall>.clearSessionInResponse() {
    setSessionInResponse(
        accessToken = Token(),
        refreshToken = Token(),
    )
}