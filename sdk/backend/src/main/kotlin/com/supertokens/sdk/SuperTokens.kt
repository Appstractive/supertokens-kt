package com.supertokens.sdk

import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.BuildRecipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

data class AppConfig(
    val name: String,
    val websiteDomain: String = "localhost",
    val websiteBasePath: String = "/auth",
    val apiDomain: String = "localhost",
    val apiBasePath: String = "/auth",
)

fun <C: RecipeConfig, R: Recipe<C>> SuperTokensConfig.recipe(builder: RecipeBuilder<C, R>, configure: C.() -> Unit = {}) {
    +builder.install(configure)
}

class SuperTokensConfig(
    val connectionUrl: String,
    val appConfig: AppConfig,
) {

    var client: HttpClient? = null

    var enableRequestLogging: Boolean = false

    var apiKey: String? = null

    var recipes: List<BuildRecipe> = emptyList()
        private set

    operator fun BuildRecipe.unaryPlus() {
        recipes = recipes + this
    }

}

class SuperTokens(
    private val config: SuperTokensConfig,
) {

    val recipes: List<Recipe<*>> = config.recipes.map { it.invoke(this) }

    val appConfig: AppConfig = config.appConfig

    val jwksUrl: String = "${config.connectionUrl}/.well-known/jwks.json"

    @OptIn(ExperimentalSerializationApi::class)
    val client = config.client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }

        if(config.enableRequestLogging) {
            install(Logging)
        }

        defaultRequest {
            url(config.connectionUrl)

            config.apiKey?.let {
                header(Constants.HEADER_API_KEY, it)
            }

            header(Constants.HEADER_CDI_VERSION, Constants.CDI_VERSION)
            contentType(ContentType.Application.Json)
        }
    }

    inline fun <reified T : Recipe<*>> getRecipe(): T = recipes.filterIsInstance<T>().firstOrNull()
        ?: throw RuntimeException("Recipe ${T::class.java.simpleName} not configured")

    inline fun <reified T : Recipe<*>> hasRecipe(): Boolean = recipes.filterIsInstance<T>().isNotEmpty()

}

fun superTokens(connectionURI: String, appConfig: AppConfig, init: SuperTokensConfig.() -> Unit): SuperTokens {
    val config = SuperTokensConfig(
        connectionUrl = connectionURI,
        appConfig = appConfig,
    ).apply(init)
    return SuperTokens(config)
}