package com.supertokens.sdk.recipes.sessions.repositories

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.sessions.SessionRecipe

abstract class TokensRepository {

  abstract suspend fun getAccessToken(): String?

  abstract suspend fun setAccessToken(accessToken: String?)

  abstract suspend fun getRefreshToken(): String?

  abstract suspend fun setRefreshToken(refreshToken: String?)
}

suspend fun SuperTokensClient.getAccessToken(): String? {
  return getRecipe<SessionRecipe>().tokensRepository.getAccessToken()
}

suspend fun SuperTokensClient.getRefreshToken(): String? {
  return getRecipe<SessionRecipe>().tokensRepository.getRefreshToken()
}
