package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.recipes.sessions.SessionRecipe

class UpdateAccessTokenUseCase(
    private val sessionRecipe: SessionRecipe,
) {

  suspend fun updateAccessToken(token: String) {
    sessionRecipe.tokensRepository.setAccessToken(token)
    sessionRecipe.claimsRepository.setClaimsFromJwt(token)

    sessionRecipe.claimsRepository.getUserId()?.let {
      sessionRecipe.authRepository.setAuthenticated(
          userId = it,
          multiFactorVerified = sessionRecipe.claimsRepository.isMultiFactorVerified(),
      )
    }
  }
}
