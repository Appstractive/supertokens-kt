package com.supertokens.sdk.recipes.sessions

import com.supertokens.sdk.common.COOKIE_ACCESS_TOKEN
import com.supertokens.sdk.common.COOKIE_REFRESH_TOKEN
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.recipes.sessions.usecases.LogoutUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateAccessTokenUseCase
import com.supertokens.sdk.recipes.sessions.usecases.UpdateRefreshTokenUseCase
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.fullPath

internal fun defaultCookieStorage(
    logoutUseCase: LogoutUseCase,
    updateAccessTokenUseCase: UpdateAccessTokenUseCase,
    updateRefreshTokenUseCase: UpdateRefreshTokenUseCase,
    tokensRepository: TokensRepository,
) = object : CookiesStorage {
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        when(cookie.name) {
            COOKIE_ACCESS_TOKEN -> {
                val token = cookie.value
                if(token.isNotBlank()) {
                    updateAccessTokenUseCase.updateAccessToken(token)
                }
            }
            COOKIE_REFRESH_TOKEN -> {
                val token = cookie.value
                if(token.isBlank() && !requestUrl.fullPath.endsWith(Routes.Session.SIGN_OUT)) {
                    logoutUseCase.signOut()
                }
                else {
                    updateRefreshTokenUseCase.updateRefreshToken(token)
                }
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = buildList {
        tokensRepository.getAccessToken()?.let {
            add(Cookie(COOKIE_ACCESS_TOKEN, it))
        }
        if(requestUrl.fullPath.endsWith(Routes.Session.REFRESH)) {
            tokensRepository.getRefreshToken()?.let {
                add(Cookie(COOKIE_REFRESH_TOKEN, it))
            }
        }
    }

    override fun close() = Unit
}