package com.supertokens.sdk.recipes.sessions.repositories

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.repositories.user.UserRepository
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url

abstract class TokensRepository {

    abstract suspend fun getAccessToken(): String?
    abstract suspend fun setAccessToken(accessToken: String?)

    abstract suspend fun getRefreshToken(): String?
    abstract suspend fun setRefreshToken(refreshToken: String?)
}

suspend fun SuperTokensClient.getAccessToken(): String? {
    return tokensRepository.getAccessToken()
}

suspend fun SuperTokensClient.getRefreshToken(): String? {
    return tokensRepository.getRefreshToken()
}