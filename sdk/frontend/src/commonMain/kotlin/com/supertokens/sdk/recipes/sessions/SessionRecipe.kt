package com.supertokens.sdk.recipes.sessions

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.getDefaultSettings
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepositorySettings
import com.supertokens.sdk.recipes.sessions.usecases.LogoutUseCase
import com.supertokens.sdk.recipes.sessions.usecases.RefreshTokensUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateAccessTokenUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateRefreshTokenUseCase
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.Sender
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath

class SessionRecipeConfig: RecipeConfig {

    var tokensRepository: TokensRepository? = null

    var cookiesStorage: CookiesStorage? = null

    var refreshTokensOnStart: Boolean = true

}

class SessionRecipe(
    private val superTokens: SuperTokensClient,
    private val config: SessionRecipeConfig,
): Recipe<SessionRecipeConfig> {

    val tokensRepository by lazy { config.tokensRepository ?: TokensRepositorySettings(getDefaultSettings()) }

    private val refreshTokensUseCase by lazy {
        RefreshTokensUseCase(
            tokensRepository = tokensRepository,
            updateAccessTokenUseCase = updateAccessTokenUseCase,
            updateRefreshTokenUseCase = updateRefreshTokenUseCase,
            logoutUseCase = logoutUseCase,
        )
    }

    private val updateAccessTokenUseCase by lazy {
        UpdateAccessTokenUseCase(
            tokensRepository = tokensRepository,
            userRepository = superTokens.userRepository,
            authRepository = superTokens.authRepository,
        )
    }

    private val updateRefreshTokenUseCase by lazy {
        UpdateRefreshTokenUseCase(
            tokensRepository = tokensRepository,
        )
    }

    private val logoutUseCase by lazy {
        LogoutUseCase(
            tokensRepository = tokensRepository,
            userRepository = superTokens.userRepository,
            authRepository = superTokens.authRepository,
        )
    }

    override suspend fun postInit() {
        superTokens.apiClient.plugin(HttpSend).intercept(tokenHeaderInterceptor())

        if(config.refreshTokensOnStart) {
            runCatching {
                refreshTokens()
            }
        }
    }

    suspend fun refreshTokens(): BearerTokens? = refreshTokensUseCase.refreshTokens(superTokens.apiClient)

    override fun HttpClientConfig<*>.configureClient() {
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
    }

    private fun tokenHeaderInterceptor(): suspend Sender.(HttpRequestBuilder) -> HttpClientCall = { request ->
        execute(request).also {
            if(it.response.status == HttpStatusCode.OK) {
                it.response.headers[HEADER_ACCESS_TOKEN]?.let { token ->
                    if(token.isNotBlank()) {
                        updateAccessTokenUseCase.updateAccessToken(token)
                    }
                    else if(!it.request.url.fullPath.endsWith(Routes.Session.SIGN_OUT)) {
                        signOut()
                    }
                }

                it.response.headers[HEADER_REFRESH_TOKEN]?.let { token ->
                    if(token.isNotBlank()) {
                        updateRefreshTokenUseCase.updateRefreshToken(token)
                    }
                    else if(!it.request.url.fullPath.endsWith(Routes.Session.SIGN_OUT)) {
                        signOut()
                    }
                }
            }
        }
    }

    suspend fun signOut() = logoutUseCase.signOut(superTokens.apiClient)

}

object Session : RecipeBuilder<SessionRecipeConfig, SessionRecipe>() {

    override fun install(configure: SessionRecipeConfig.() -> Unit): (SuperTokensClient) -> SessionRecipe {
        val config = SessionRecipeConfig().apply(configure)

        return {
            SessionRecipe(it, config)
        }
    }

}

suspend fun SuperTokensClient.signOut() = getRecipe<SessionRecipe>().signOut()