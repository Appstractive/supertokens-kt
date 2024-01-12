package com.supertokens.sdk

import com.russhwolf.settings.Settings
import com.supertokens.sdk.recipes.BuildRecipe
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.sessions.defaultCookieStorage
import com.supertokens.sdk.recipes.sessions.tokenHeaderInterceptor
import com.supertokens.sdk.repositories.AuthRepository
import com.supertokens.sdk.repositories.AuthState
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepositorySettings
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.repositories.user.UserRepository
import com.supertokens.sdk.repositories.user.UserRepositorySettings
import com.supertokens.sdk.recipes.sessions.usecases.RefreshTokensUseCase
import com.supertokens.sdk.recipes.sessions.usecases.LogoutUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateAccessTokenUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateRefreshTokenUseCase
import com.supertokens.sdk.recipes.thirdparty.repositories.PkceRepository
import com.supertokens.sdk.recipes.thirdparty.repositories.PkceRepositoryImpl
import com.supertokens.sdk.repositories.AuthRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    var authRepository: AuthRepository? = null
    var pkceRepository: PkceRepository? = null

    var cookiesStorage: CookiesStorage? = null

    var clientName: String = "MyMobileApp"

    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var recipes: List<BuildRecipe> = emptyList()
        private set

    operator fun BuildRecipe.unaryPlus() {
        recipes = recipes + this
    }

}

fun <C: RecipeConfig, R: Recipe<C>> SuperTokensClientConfig.recipe(builder: RecipeBuilder<C, R>, configure: C.() -> Unit = {}) {
    +builder.install(configure)
}

class SuperTokensClient(
    private val config: SuperTokensClientConfig,
) {

    init {
        config.scope.launch {

        }
    }

    val recipes: List<Recipe<*>> = config.recipes.map { it.invoke(this) }

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
                storage = config.cookiesStorage ?: defaultCookieStorage(
                    logoutUseCase = logoutUseCase,
                    updateAccessTokenUseCase = updateAccessTokenUseCase,
                    updateRefreshTokenUseCase = updateRefreshTokenUseCase,
                    tokensRepository = tokensRepository,
                )
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokensRepository.getRefreshToken()?.let {
                            BearerTokens(tokensRepository.getAccessToken() ?: "", it)
                        }
                    }

                    refreshTokens {
                        refreshTokensUseCase.refreshTokens(this)
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
    val userRepository = config.userRepository ?: UserRepositorySettings(getDefaultSettings())
    val authRepository = config.authRepository ?: AuthRepositoryImpl()
    val pkceRepository = config.pkceRepository ?: PkceRepositoryImpl()

    val refreshTokensUseCase by lazy {
        RefreshTokensUseCase(
            tokensRepository = tokensRepository,
            updateAccessTokenUseCase = updateAccessTokenUseCase,
            updateRefreshTokenUseCase = updateRefreshTokenUseCase,
            logoutUseCase = logoutUseCase,
        )
    }

    val updateAccessTokenUseCase by lazy {
        UpdateAccessTokenUseCase(
            tokensRepository = tokensRepository,
            userRepository = userRepository,
            authRepository = authRepository,
        )
    }

    val updateRefreshTokenUseCase by lazy {
        UpdateRefreshTokenUseCase(
            tokensRepository = tokensRepository,
        )
    }

    val logoutUseCase by lazy {
        LogoutUseCase(
            tokensRepository = tokensRepository,
            userRepository = userRepository,
            authRepository = authRepository,
        )
    }

    inline fun <reified T : Recipe<*>> getRecipe(): T = recipes.filterIsInstance<T>().firstOrNull()
        ?: throw RuntimeException("Recipe ${T::class.simpleName} not configured")

    inline fun <reified T : Recipe<*>> hasRecipe(): Boolean = recipes.filterIsInstance<T>().isNotEmpty()

    suspend fun isLoggedIn(): Boolean = tokensRepository.getRefreshToken() != null
    suspend fun isAuthenticated():Boolean = authRepository.authState.value is AuthState.Authenticated

}

fun superTokensClient(apiBaseUrl: String, init: SuperTokensClientConfig.() -> Unit = {}): SuperTokensClient {
    val config = SuperTokensClientConfig(
        apiBaseUrl = apiBaseUrl,
    ).apply(init)
    return SuperTokensClient(config)
}