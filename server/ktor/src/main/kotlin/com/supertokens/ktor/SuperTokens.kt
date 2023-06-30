package com.supertokens.ktor

import com.auth0.jwt.interfaces.JWTVerifier
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.SuperTokensJwtVerifier
import com.supertokens.ktor.plugins.TokenValidator
import com.supertokens.ktor.recipes.emailpassword.emailPasswordRoutes
import com.supertokens.ktor.recipes.session.sessionRoutes
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json


class SuperTokensConfig {

    var superTokens: SuperTokens? = null

    var headerBasedSessions: Boolean = true

    var cookieBasedSessions: Boolean = true

    var jwtValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = TokenValidator

    var jwtVerifier: JWTVerifier? = null

}

val SuperTokens = createApplicationPlugin(name = "SuperTokens", createConfiguration = ::SuperTokensConfig) {

    val config = pluginConfig
    val superTokens = config.superTokens ?: throw RuntimeException("SuperTokens SDK not configured")

    on(MonitoringEvent(ApplicationStarted)) { application ->

    }

    application.attributes.put(SuperTokensAttributeKey, superTokens)

    application.routing {

        route(superTokens.appConfig.apiBasePath) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            if(superTokens.hasRecipe<EmailPasswordRecipe>()) {
                emailPasswordRoutes(
                    headerBasedSessions = config.headerBasedSessions,
                    cookieBasedSessions = config.cookieBasedSessions,
                )
            }

            if(superTokens.hasRecipe<SessionRecipe>()) {

                application.install(Authentication) {
                    jwt(name = SuperTokensAuth) {
                        validate(config.jwtValidator)
                        verifier(config.jwtVerifier ?: SuperTokensJwtVerifier(superTokens))
                    }
                }

                sessionRoutes(
                    headerBasedSessions = config.headerBasedSessions,
                    cookieBasedSessions = config.cookieBasedSessions,
                )
            }
        }
    }
}

val SuperTokensAttributeKey = AttributeKey<SuperTokens>("SuperTokens")

val PipelineContext<Unit, ApplicationCall>.superTokens: SuperTokens get() = application.attributes[SuperTokensAttributeKey]
val Route.superTokens: SuperTokens get() = attributes[SuperTokensAttributeKey]