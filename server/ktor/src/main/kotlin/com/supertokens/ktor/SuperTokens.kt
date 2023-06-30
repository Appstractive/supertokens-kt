package com.supertokens.ktor

import com.supertokens.ktor.routes.emailpassword.emailPasswordRoutes
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json


class SuperTokensConfig {

    var superTokens: SuperTokens? = null

    var headerBasedSessions: Boolean = true

    var cookieBasedSessions: Boolean = true

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
        }
    }



}

val SuperTokensAttributeKey = AttributeKey<SuperTokens>("SuperTokens")

val PipelineContext<Unit, ApplicationCall>.superTokens: SuperTokens get() = application.attributes[SuperTokensAttributeKey]
val Route.superTokens: SuperTokens get() = attributes[SuperTokensAttributeKey]