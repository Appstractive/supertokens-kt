package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.repositories.AuthRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.repositories.user.UserRepository

class LogoutUseCase(
    private val tokensRepository: TokensRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) {

    suspend fun logout() {
        tokensRepository.setAccessToken(null)
        tokensRepository.setRefreshToken(null)
        userRepository.clear()
        authRepository.setUnauthenticated()
    }

}