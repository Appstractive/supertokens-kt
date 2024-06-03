package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.sessions.SessionRecipe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.post

class LogoutUseCase(
    private val sessionRecipe: SessionRecipe,
) {

    suspend fun signOut() {
        with(sessionRecipe.superTokens.apiClient) {
            runCatching {
                post(Routes.Session.SIGN_OUT)
            }

            pluginOrNull(Auth)?.let { bearerAuth ->
                bearerAuth.providers.filterIsInstance<BearerAuthProvider>()
                    .forEach { provider -> provider.clearToken() }
            }
        }

        sessionRecipe.tokensRepository.setAccessToken(null)
        sessionRecipe.tokensRepository.setRefreshToken(null)
        sessionRecipe.claimsRepository.clear()
        sessionRecipe.authRepository.setUnauthenticated()
    }

}