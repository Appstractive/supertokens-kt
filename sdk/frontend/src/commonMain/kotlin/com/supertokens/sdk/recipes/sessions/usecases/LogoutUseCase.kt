package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.core.respositories.UserRepository
import com.supertokens.sdk.recipes.sessions.SessionRecipe
import io.ktor.client.request.post

internal class LogoutUseCase(
    private val sessionRecipe: SessionRecipe,
    private val userRepository: UserRepository,
) {

  suspend fun signOut(clearServerSession: Boolean = true) {
    if (clearServerSession) {
      with(sessionRecipe.superTokens.apiClient) { runCatching { post(Routes.Session.SIGN_OUT) } }
    }

    sessionRecipe.tokensRepository.setAccessToken(null)
    sessionRecipe.tokensRepository.setRefreshToken(null)
    sessionRecipe.clearClientTokens()
    sessionRecipe.claimsRepository.clear()
    sessionRecipe.authRepository.setUnauthenticated()
    userRepository.updateUser(null)
  }
}
