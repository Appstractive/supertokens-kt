package com.supertokens.sdk.recipes.sessions.usecases

import com.supertokens.sdk.recipes.sessions.SessionRecipe

class UpdateRefreshTokenUseCase(
    private val sessionRecipe: SessionRecipe,
) {

    suspend fun updateRefreshToken(token: String) {
        sessionRecipe.tokensRepository.setRefreshToken(token)
    }

}