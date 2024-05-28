package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.sessions.repositories.AuthRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.recipes.sessions.repositories.ClaimsRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post

class LogoutUseCase(
    private val tokensRepository: TokensRepository,
    private val claimsRepository: ClaimsRepository,
    private val authRepository: AuthRepository,
) {

    suspend fun signOut(client: HttpClient? = null) {
        client?.let {
            runCatching {
                it.post(Routes.Session.SIGN_OUT)
            }
        }
        tokensRepository.setAccessToken(null)
        tokensRepository.setRefreshToken(null)
        claimsRepository.clear()
        authRepository.setUnauthenticated()
    }

}