package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.core.respositories.UserRepository
import com.supertokens.sdk.recipes.sessions.SessionRecipe
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.post

internal class LogoutUseCase(
  private val sessionRecipe: SessionRecipe,
  private val userRepository: UserRepository,
) {

  suspend fun signOut(clearServerSession: Boolean = true) {
    if(clearServerSession) {
      with(sessionRecipe.superTokens.apiClient) {
        runCatching { post(Routes.Session.SIGN_OUT) }

        pluginOrNull(Auth)?.let { bearerAuth ->
          bearerAuth.providers.filterIsInstance<BearerAuthProvider>().forEach { provider ->
            provider.clearToken()
          }
        }
      }
    }

    sessionRecipe.tokensRepository.setAccessToken(null)
    sessionRecipe.tokensRepository.setRefreshToken(null)
    sessionRecipe.claimsRepository.clear()
    sessionRecipe.authRepository.setUnauthenticated()
    userRepository.updateUser(null)
  }
}
