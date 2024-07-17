package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.common.Routes
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

  authenticate(SuperTokensAuth) { post(Routes.Session.SIGN_OUT) { with(handler) { signOut() } } }

  post(Routes.Session.REFRESH) { with(handler) { refresh() } }

  get(Routes.Session.JWKS) { call.respond(superTokens.getJwks()) }

  get(Routes.Session.OIDC) {
    call.respond(
        JsonObject(
            mapOf(
                "issuer" to JsonPrimitive(sessions.issuer),
                "jwks_uri" to
                    JsonPrimitive(
                        "${superTokens.appConfig.api.scheme}://${superTokens.appConfig.api.host}${superTokens.appConfig.api.path}jwt/jwks.json"),
            )))
  }
}
