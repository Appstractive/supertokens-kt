package com.supertokens.sdk.recipes.sessions.repositories

class TokensRepositoryMemory: TokensRepository() {

    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken() = accessToken

    override suspend fun setAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    override suspend fun getRefreshToken() = refreshToken

    override suspend fun setRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

}