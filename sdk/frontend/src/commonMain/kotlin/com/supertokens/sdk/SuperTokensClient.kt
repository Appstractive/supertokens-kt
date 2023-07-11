package com.supertokens.sdk

import com.russhwolf.settings.Settings
import com.supertokens.sdk.recipes.sessions.tokenHeaderInterceptor
import com.supertokens.sdk.repositories.tokens.TokensRepositorySettings
import com.supertokens.sdk.repositories.tokens.TokensRepository
import com.supertokens.sdk.repositories.user.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

expect fun getDefaultSettings(): Settings

@SuperTokensDslMarker
class SuperTokensClientConfig(
    val apiBaseUrl: String,
) {

    var client: HttpClient? = null

    var tokensRepository: TokensRepository? = null

    var userRepository: UserRepository? = null

    var clientName: String = "MyMobileApp"

}

class SuperTokensClient(
    private val config: SuperTokensClientConfig,
) {

    @OptIn(ExperimentalSerializationApi::class)
    val apiClient by lazy {
        (config.client ?: HttpClient {

            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    explicitNulls = false
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                })
            }

            install(HttpCookies) {
                storage = tokensRepository
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokensRepository.getRefreshToken()?.let {
                            BearerTokens(tokensRepository.getAccessToken() ?: "", it)
                        }
                    }

                    refreshTokens {
                        with(tokensRepository) {
                            refreshTokens()
                        }
                    }
                }
            }

            defaultRequest {
                url(config.apiBaseUrl)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Origin, config.clientName)
            }
        }).also {
            it.plugin(HttpSend).intercept(tokenHeaderInterceptor())
        }
    }

    val tokensRepository = config.tokensRepository ?: TokensRepositorySettings(getDefaultSettings())

}

fun superTokensClient(apiBaseUrl: String, init: SuperTokensClientConfig.() -> Unit = {}): SuperTokensClient {
    val config = SuperTokensClientConfig(
        apiBaseUrl = apiBaseUrl,
    ).apply(init)
    return SuperTokensClient(config)
}