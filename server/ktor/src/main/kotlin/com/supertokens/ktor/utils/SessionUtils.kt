package com.supertokens.ktor.utils

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_ANTI_CSRF
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.models.Token
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
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
            HEADER_ACCESS_TOKEN,
        )

        call.response.headers.append(
            HEADER_ACCESS_TOKEN,
            accessToken.token,
        )

        refreshToken?.let {
            call.response.headers.append(
                HEADER_REFRESH_TOKEN,
                it.token,
            )
            exposeHeaders.add(HEADER_REFRESH_TOKEN)
        }

        antiCsrfToken?.let {
            call.response.headers.append(
                HEADER_ANTI_CSRF,
                it,
            )
            exposeHeaders.add(HEADER_ANTI_CSRF)
        }

        call.response.headers.append(
            HttpHeaders.AccessControlAllowHeaders,
            exposeHeaders.joinToString(", "),
        )
    }

    if (sessions.cookieBasedSessions) {
        val frontend = call.fronend
        call.response.cookies.append(
            Cookie(
                name = COOKIE_ACCESS_TOKEN,
                value = sessions.cookieDomain,
                domain = frontend.host,
                httpOnly = true,
                expires = GMTDate(accessToken.expiry),
                path = "/",
                secure = sessions.secureCookies,
                extensions = mapOf(
                    "SameSite" to sessions.cookieSameSite,
                ),
            )
        )

        refreshToken?.let {
            call.response.cookies.append(
                Cookie(
                    name = COOKIE_REFRESH_TOKEN,
                    value = it.token,
                    domain = sessions.cookieDomain,
                    httpOnly = true,
                    expires = GMTDate(it.expiry),
                    secure = sessions.secureCookies,
                    path = "${frontend.path}${Routes.Session.REFRESH}",
                    extensions = mapOf(
                        "SameSite" to sessions.cookieSameSite,
                    ),
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