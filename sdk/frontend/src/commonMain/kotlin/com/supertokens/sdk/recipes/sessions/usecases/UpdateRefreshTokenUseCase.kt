package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository

class UpdateRefreshTokenUseCase(
    private val tokensRepository: TokensRepository,
) {

    suspend fun updateRefreshToken(token: String) {
        tokensRepository.setRefreshToken(token)
    }

}