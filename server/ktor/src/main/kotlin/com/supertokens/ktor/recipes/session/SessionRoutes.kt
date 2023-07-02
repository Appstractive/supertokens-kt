package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.core.getJwks
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Route.sessionRoutes(
    handler: SessionHandler,
) {

    authenticate(SuperTokensAuth) {
        post("/signout") {
            with(handler) {
                signOut()
            }
        }
    }

    post("/session/refresh") {
        with(handler) {
            refresh()
        }
    }

    get("/jwt/jwks.json") {
        call.respond(superTokens.getJwks())
    }

    get("/.well-known/openid-configuration") {
        call.respond(JsonObject(
            mapOf(
                "issuer" to JsonPrimitive(superTokens.appConfig.apiDomain),
                "jwks_uri" to JsonPrimitive("https://${superTokens.appConfig.apiDomain}${superTokens.appConfig.apiBasePath}/jwt/jwks.json"),
            )
        ))
    }
}