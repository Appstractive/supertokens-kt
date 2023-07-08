package com.supertokens.sdk.repositories.tokens

import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url

interface TokensRepository: CookiesStorage {

    suspend fun getAccessToken(): String?
    suspend fun setAccessToken(accessToken: String?)

    suspend fun getRefreshToken(): String?
    suspend fun setRefreshToken(refreshToken: String?)

    suspend fun clearTokens() {
        setAccessToken(null)
        setRefreshToken(null)
    }

    suspend fun RefreshTokensParams.refreshTokens(): BearerTokens? {
        return getRefreshToken()?.let {
            val response = client.post(Routes.Session.REFRESH) {
                header(HEADER_REFRESH_TOKEN, it)
                markAsRefreshTokenRequest()
            }

            if(response.status != HttpStatusCode.OK) {
                clearTokens()
                return null
            }

            val newRefreshToken = response.headers[HEADER_REFRESH_TOKEN] ?: return null
            val newAccessToken = response.headers[HEADER_ACCESS_TOKEN] ?: return null

            setAccessToken(newAccessToken)
            setRefreshToken(newRefreshToken)

            BearerTokens(newAccessToken, newRefreshToken)
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        when(cookie.name) {
            COOKIE_ACCESS_TOKEN -> setAccessToken(cookie.value.takeIf { it.isNotBlank() })
            COOKIE_REFRESH_TOKEN -> setRefreshToken(cookie.value.takeIf { it.isNotBlank() })
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = buildList {
        getAccessToken()?.let {
            add(Cookie(COOKIE_ACCESS_TOKEN, it))
        }
        getRefreshToken()?.let {
            add(Cookie(COOKIE_REFRESH_TOKEN, it))
        }
    }

    override fun close() = Unit

}