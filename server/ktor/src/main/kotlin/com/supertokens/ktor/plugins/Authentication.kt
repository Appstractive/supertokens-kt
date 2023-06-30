package com.supertokens.ktor.plugins

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.supertokens.ktor.utils.UnauthorizedException
import com.supertokens.sdk.SuperTokens
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import java.net.URL
import java.security.interfaces.RSAPublicKey


data class AuthenticatedUser(
    val id: String,
    val sessionHandle: String,
): Principal

const val SuperTokensAuth = "SuperTokens"

class SuperTokensJwtVerifier(
    private val superTokens: SuperTokens,
): JWTVerifier {

    private val jwkProvider = UrlJwkProvider(URL(superTokens.jwksUrl))

    override fun verify(token: String?): DecodedJWT {
        val jwt = JWT.decode(token)
        return verify(jwt)
    }

    override fun verify(jwt: DecodedJWT?): DecodedJWT {
        return jwt?.let {
            val jwk = jwkProvider.get(jwt.keyId)

            //val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey // unsafe
            val publicKey = jwk.publicKey as? RSAPublicKey ?: throw JWTVerificationException("Invalid key type")

            // TODO supported algorithms?
            val algorithm = when (jwk.algorithm) {
                "RS256" -> Algorithm.RSA256(publicKey, null)
                else -> throw JWTVerificationException("Unsupported algorithm")
            }

            val verifier = JWT.require(algorithm)
                .withIssuer(superTokens.appConfig.apiBasePath)
                .build()

            verifier.verify(jwt)
        } ?: throw JWTVerificationException("jwt is null")
    }
}

val TokenValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = {
    val sub = it.subject
    val sessionHandle = it["sessionHandle"]

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