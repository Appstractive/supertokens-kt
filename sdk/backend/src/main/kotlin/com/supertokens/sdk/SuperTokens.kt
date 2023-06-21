package com.supertokens.sdk

import com.supertokens.sdk.recipes.Recipe
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

data class AppConfig(
    val name: String,
    val websiteDomain: String,
    val websiteBasePath: String? = null,
    val apiDomain: String,
    val apiBasePath: String? = null,
    val apiGatewayPath: String? = null,
)

class SuperTokensConfig(
    val connectionURI: String,
    val appConfig: AppConfig,
) {

    var client: HttpClient? = null

    var apiKey: String? = null

    var recipes: List<Recipe> = emptyList()
        private set

    var isInServerlessEnv = false

    var telemetryEnabled = false

    operator fun Recipe.unaryPlus() {
        recipes = recipes + this
    }

}

class SuperTokens(
    val config: SuperTokensConfig,
) {

    val client = config.client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        install(Logging)

        defaultRequest {
            url(config.connectionURI)

            config.apiKey?.let {
                header("api-key", it)
            }

            header("cdi-version", "2.21")
            contentType(ContentType.Application.Json)
        }
    }

    inline fun <reified T : Recipe> getRecipe(): T = config.recipes.filterIsInstance<T>().firstOrNull()
        ?: throw RuntimeException("Recipe ${T::class.java.simpleName} not configured")

}

fun superTokens(connectionURI: String, appConfig: AppConfig, init: SuperTokensConfig.() -> Unit): SuperTokens {
    val config = SuperTokensConfig(
        connectionURI = connectionURI,
        appConfig = appConfig,
    )
    config.init()
    return SuperTokens(config)
}