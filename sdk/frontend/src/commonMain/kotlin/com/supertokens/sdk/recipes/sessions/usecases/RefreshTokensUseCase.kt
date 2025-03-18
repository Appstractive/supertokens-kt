package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.sessions.SessionRecipe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.setCookie

internal class RefreshTokensUseCase(
  private val sessionRecipe: SessionRecipe,
) {

  suspend fun refreshTokens(client: HttpClient): BearerTokens? {
    return sessionRecipe.tokensRepository.getRefreshToken()?.let { refreshTokens(client, it) }
  }

  suspend fun refreshTokens(params: RefreshTokensParams): BearerTokens? {
    return (params.oldTokens?.refreshToken ?: sessionRecipe.tokensRepository.getRefreshToken())
        ?.let { refreshTokens(params.client, it) }
  }

  private suspend fun refreshTokens(client: HttpClient, refreshToken: String): BearerTokens? {
    val response =
        client.post(Routes.Session.REFRESH) {
          header(HEADER_REFRESH_TOKEN, refreshToken)
          attributes.put(AuthCircuitBreaker, Unit)
        }

    if (response.status != HttpStatusCode.OK) {
      if (response.status == HttpStatusCode.Unauthorized) {
        sessionRecipe.signOut()
      }

      return null
    }

    val cookies = response.setCookie()

    val newRefreshToken =
        response.headers[HEADER_REFRESH_TOKEN]
          ?: cookies.firstOrNull { it.name == COOKIE_REFRESH_TOKEN }?.value
          ?: return null
    val newAccessToken =
        response.headers[HEADER_ACCESS_TOKEN]
          ?: cookies.firstOrNull { it.name == COOKIE_ACCESS_TOKEN }?.value
          ?: return null

    sessionRecipe.updateAccessTokenUseCase.updateAccessToken(newAccessToken)
    sessionRecipe.updateRefreshTokenUseCase.updateRefreshToken(newRefreshToken)

    return BearerTokens(newAccessToken, newRefreshToken)
  }
}
