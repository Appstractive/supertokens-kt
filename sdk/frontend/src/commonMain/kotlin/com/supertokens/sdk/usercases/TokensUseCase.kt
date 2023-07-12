package com.supertokens.sdk.usercases

import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.repositories.tokens.TokensRepository
import com.supertokens.sdk.repositories.user.UserRepository
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url

class TokensUseCase(
    private val tokensRepository: TokensRepository,
    private val userRepository: UserRepository,
) {

    internal val cookies = object : CookiesStorage {
        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
            when(cookie.name) {
                COOKIE_ACCESS_TOKEN -> {
                    val token = cookie.value
                    if(token.isBlank()) {
                        clearAccessToken()
                    }
                    else {
                        updateAccessToken(token)
                    }
                }
                COOKIE_REFRESH_TOKEN -> {
                    val token = cookie.value
                    if(token.isBlank()) {
                        clearRefreshToken()
                    }
                    else {
                        updateRefreshToken(token)
                    }
                }
            }
        }

        override suspend fun get(requestUrl: Url): List<Cookie> = buildList {
            tokensRepository.getAccessToken()?.let {
                add(Cookie(COOKIE_ACCESS_TOKEN, it))
            }
            tokensRepository.getRefreshToken()?.let {
                add(Cookie(COOKIE_REFRESH_TOKEN, it))
            }
        }

        override fun close() = Unit
    }

    suspend fun updateAccessToken(token: String) {
        tokensRepository.setAccessToken(token)
        userRepository.setClaimsFromJwt(token)
    }

    suspend fun clearAccessToken() {
        tokensRepository.setAccessToken(null)
    }

    suspend fun updateRefreshToken(token: String) {
        tokensRepository.setRefreshToken(token)
    }

    suspend fun clearRefreshToken() {
        tokensRepository.setRefreshToken(null)
    }

    suspend fun clearTokens(clearUserData: Boolean = false) {
        clearAccessToken()
        clearRefreshToken()
        if(clearUserData) {
            userRepository.clear()
        }
    }

    suspend fun RefreshTokensParams.refreshTokens(): BearerTokens? {
        return tokensRepository.getRefreshToken()?.let {
            val response = client.post(Routes.Session.REFRESH) {
                header(HEADER_REFRESH_TOKEN, it)
                markAsRefreshTokenRequest()
            }

            if(response.status != HttpStatusCode.OK) {
                clearTokens(true)
                return null
            }

            val newRefreshToken = response.headers[HEADER_REFRESH_TOKEN] ?: return null
            val newAccessToken = response.headers[HEADER_ACCESS_TOKEN] ?: return null

            updateAccessToken(newAccessToken)
            updateRefreshToken(newRefreshToken)

            BearerTokens(newAccessToken, newRefreshToken)
        }
    }

}