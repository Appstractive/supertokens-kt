package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.repositories.AuthRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.repositories.user.UserRepository

class UpdateAccessTokenUseCase(
    private val tokensRepository: TokensRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) {

    suspend fun updateAccessToken(token: String) {
        tokensRepository.setAccessToken(token)
        userRepository.setClaimsFromJwt(token)

        userRepository.getUserId()?.let {
            authRepository.setAuthenticated(it)
        }
    }

}