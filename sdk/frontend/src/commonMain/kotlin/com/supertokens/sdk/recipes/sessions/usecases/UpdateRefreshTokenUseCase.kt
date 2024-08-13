package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.recipes.sessions.SessionRecipe

internal class UpdateRefreshTokenUseCase(
    private val sessionRecipe: SessionRecipe,
) {

  suspend fun updateRefreshToken(token: String) {
    sessionRecipe.tokensRepository.setRefreshToken(token)
    sessionRecipe.clearClientTokens()
  }
}
