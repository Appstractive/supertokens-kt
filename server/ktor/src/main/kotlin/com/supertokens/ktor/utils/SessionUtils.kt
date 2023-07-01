package com.supertokens.ktor.utils

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.models.Token
import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.date.GMTDate
import io.ktor.util.pipeline.PipelineContext

fun ApplicationCall.setSessionInResponse(
    accessToken: Token,
    refreshToken: Token,
    antiCsrfToken: String? = null,
) {
    if(sessions.headerBasedSessions) {
        val exposeHeaders = mutableListOf(
            "st-access-token",
            "st-refresh-token",
        )

        response.headers.append(
            "st-access-token",
            accessToken.token,
        )

        response.headers.append(
            "st-refresh-token",
            refreshToken.token,
        )

        antiCsrfToken?.let {
            response.headers.append(
                "anti-csrf",
                it,
            )
            exposeHeaders.add("anti-csrf")
        }

        response.headers.append(
            "Access-Control-Expose-Headers",
            exposeHeaders.joinToString(", "),
        )
    }

    if(sessions.cookieBasedSessions) {
        response.cookies.append(Cookie(
            name = "sAccessToken",
            value = accessToken.token,
            domain = sessions.cookieDomain,
            httpOnly = true,
            expires = GMTDate(accessToken.expiry),
            path = "/",
        ))

        val basePath = superTokens.appConfig.websiteBasePath
        response.cookies.append(Cookie(
            name = "sRefreshToken",
            value = refreshToken.token,
            domain = sessions.cookieDomain,
            httpOnly = true,
            expires = GMTDate(refreshToken.expiry),
            path = "${basePath}/session/refresh",
        ))
    }

}

fun ApplicationCall.clearSessionInResponse() {
    setSessionInResponse(
        accessToken = Token(),
        refreshToken = Token(),
    )
}