package com.supertokens.sdk.recipes.sessions.repositories

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class TokensRepositorySettings(
    private val settings: Settings,
): TokensRepository() {

    // AccessToken is always in memory, because it is short lived anyway and will need to be refreshed on app start.
    private var accessToken: String? = null

    override suspend fun getAccessToken() = accessToken

    override suspend fun setAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    override suspend fun getRefreshToken(): String? {
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    override suspend fun setRefreshToken(refreshToken: String?) {
        settings[KEY_REFRESH_TOKEN] = refreshToken
    }

    companion object {

        const val KEY_REFRESH_TOKEN = "sRefreshToken"

    }

}