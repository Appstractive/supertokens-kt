package com.supertokens.ktor.plugins

import com.auth0.jwt.interfaces.Payload
import com.supertokens.ktor.recipes.multifactor.isMultiFactorAuthEnabled
import com.supertokens.ktor.recipes.multifactor.mfaHandler
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.HEADER_ANTI_CSRF
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext


data class AuthenticatedUser(
    val id: String,
    val accessToken: String,
    val sessionHandle: String,
    val jwtPayload: Payload,
    val roles: Set<String>? = null,
    val permissions: Set<String>? = null,
) : Principal

const val SuperTokensAuth = "SuperTokens"

val TokenValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = {
    accessToken?.let { token ->
        if (sessions.verifySessionInCore) {
            sessions.verifySession(
                accessToken = token,
                antiCsrfToken = request.headers[HEADER_ANTI_CSRF],
            )
        }

        val sub = it.subject ?: return@let null
        val sessionHandle = it["sessionHandle"] ?: return@let null

        if (isMultiFactorAuthEnabled) {
            if (!with(mfaHandler) { checkMfaAuth(credential = it) }) {
                return@let null
            }
        }

        AuthenticatedUser(
            id = sub,
            accessToken = token,
            sessionHandle = sessionHandle,
            jwtPayload = it.payload,
            roles = it.payload.claims[Claims.ROLES]?.asList(String::class.java)?.toSet(),
            permissions = (it.payload.claims[Claims.PERMISSIONS]?.asList(String::class.java)
                ?.toSet()),
        )
    }
}


val authHeaderCookieWrapper: (ApplicationCall) -> HttpAuthHeader? = { call ->
    val authHeader = try {
        call.request.parseAuthorizationHeader()
    } catch (cause: IllegalArgumentException) {
        null
    }

    authHeader?.also {
        if(it is HttpAuthHeader.Single) {
            call.attributes.put(AccessTokenAttributeKey, it.blob)
        }
    } ?: call.request.cookies[COOKIE_ACCESS_TOKEN]?.let {
        HttpAuthHeader.Single(
            "Bearer",
            it
        )
    }?.also {
        call.attributes.put(AccessTokenAttributeKey, it.blob)
    }
}

inline fun <reified P : Principal> ApplicationCall.requirePrincipal(): P = requirePrincipal(null)
inline fun <reified P : Principal> ApplicationCall.requirePrincipal(provider: String?): P =
    authentication.principal(provider) ?: throw UnauthorizedException()

val AccessTokenAttributeKey = AttributeKey<String>("AccessToken")

val ApplicationCall.accessToken: String? get() = attributes.getOrNull(AccessTokenAttributeKey)
val PipelineContext<Unit, ApplicationCall>.accessToken: String? get() = call.accessToken