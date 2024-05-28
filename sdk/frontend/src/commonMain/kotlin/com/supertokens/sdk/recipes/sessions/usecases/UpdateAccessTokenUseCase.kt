package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.recipes.sessions.repositories.AuthRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import com.supertokens.sdk.recipes.sessions.repositories.ClaimsRepository

class UpdateAccessTokenUseCase(
    private val tokensRepository: TokensRepository,
    private val claimsRepository: ClaimsRepository,
    private val authRepository: AuthRepository,
) {

    suspend fun updateAccessToken(token: String) {
        tokensRepository.setAccessToken(token)
        claimsRepository.setClaimsFromJwt(token)

        claimsRepository.getUserId()?.let {
            authRepository.setAuthenticated(
                userId = it,
                multiFactorVerified = claimsRepository.isMultiFactorVerified(),
            )
        }
    }

}