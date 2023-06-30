package com.supertokens.ktor.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.supertokens.ktor.utils.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential


data class AuthenticatedUser(
    val id: String,
    val sessionHandle: String,
): Principal

const val SuperTokensAuth = "SuperTokens"

val TokenVerifier = object: JWTVerifier {
    override fun verify(token: String?): DecodedJWT {
        return token?.let {
            JWT.decode(it)
        } ?: throw UnauthorizedException()
    }

    override fun verify(jwt: DecodedJWT?): DecodedJWT {
        return jwt ?: throw UnauthorizedException()
    }

}
val TokenValidator: ApplicationCall.(JWTCredential) -> Principal? = {
    val sub = it.subject
    val sessionHandle = it.get("sessionHandle")

    if(sub != null && sessionHandle != null) {
        AuthenticatedUser(id = sub, sessionHandle = sessionHandle)
    }
    else {
        null
    }

}

inline fun <reified P : Principal> ApplicationCall.requirePrincipal(): P = requirePrincipal(null)
inline fun <reified P : Principal> ApplicationCall.requirePrincipal(provider: String?): P =
    authentication.principal(provider) ?: throw UnauthorizedException()