package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.setCookie

class RefreshTokensUseCase(
    private val tokensRepository: TokensRepository,
    private val updateAccessTokenUseCase: UpdateAccessTokenUseCase,
    private val updateRefreshTokenUseCase: UpdateRefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) {

    suspend fun refreshTokens(client: HttpClient): BearerTokens? {
        return tokensRepository.getRefreshToken()?.let {
            refreshTokens(client, it)
        }
    }

    suspend fun refreshTokens(params: RefreshTokensParams): BearerTokens? {
        return (params.oldTokens?.refreshToken ?: tokensRepository.getRefreshToken())?.let {
            refreshTokens(params.client, it)
        }
    }

    private suspend fun refreshTokens(client: HttpClient, refreshToken: String): BearerTokens? {
        val response = client.post(Routes.Session.REFRESH) {
            header(HEADER_REFRESH_TOKEN, refreshToken)
            attributes.put(Auth.AuthCircuitBreaker, Unit)
        }

        if(response.status != HttpStatusCode.OK) {
            logoutUseCase.signOut()
            return null
        }

        val cookies = response.setCookie()

        val newRefreshToken = response.headers[HEADER_REFRESH_TOKEN]
            ?: cookies.firstOrNull {it.name == COOKIE_REFRESH_TOKEN}?.value
            ?: return null
        val newAccessToken = response.headers[HEADER_ACCESS_TOKEN]
            ?: cookies.firstOrNull {it.name == COOKIE_ACCESS_TOKEN}?.value
            ?: return null

        updateAccessTokenUseCase.updateAccessToken(newAccessToken)
        updateRefreshTokenUseCase.updateRefreshToken(newRefreshToken)

        return BearerTokens(newAccessToken, newRefreshToken)
    }

}