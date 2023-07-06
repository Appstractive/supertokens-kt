package com.supertokens.sdk

import com.supertokens.sdk.core.CoreHandler
import com.supertokens.sdk.models.SuperTokensEvent
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

data class ServerConfig(
    val scheme: String = "http",
    val host: String = "localhost",
    val path: String = "/",
) {

    val fullUrl = "$scheme://$host$path"

}

data class AppConfig(
    val name: String,
    val frontends: List<ServerConfig> = listOf(
        ServerConfig(),
    ),
    val api: ServerConfig = ServerConfig(),
)

fun <C: RecipeConfig, R: Recipe<C>> SuperTokensConfig.recipe(builder: RecipeBuilder<C, R>, configure: C.() -> Unit = {}) {
    +builder.install(configure)
}

@SuperTokensDslMarker
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

    internal val core: CoreHandler = CoreHandler()

    internal val _events = MutableSharedFlow<SuperTokensEvent>(
        extraBufferCapacity = 50,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events = _events.asSharedFlow()

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

    fun getFrontEnd(origin: String?): ServerConfig {
        val frontends = appConfig.frontends
        return origin.takeIf { !it.equals("null", ignoreCase = true) }?.let {
            frontends.firstOrNull {
                origin.equals("${it.scheme}://${it.host}", ignoreCase = true)
            }
        } ?: frontends.first()
    }

}

fun superTokens(connectionURI: String, appConfig: AppConfig, init: SuperTokensConfig.() -> Unit): SuperTokens {
    val config = SuperTokensConfig(
        connectionUrl = connectionURI,
        appConfig = appConfig,
    ).apply(init)
    return SuperTokens(config)
}