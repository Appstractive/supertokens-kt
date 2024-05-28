package com.supertokens.sdk.recipes.sessions.repositories

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class ClaimsRepositorySettings(
    private val settings: Settings,
    override val decoder: Json = Json {
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
) : ClaimsRepository {

    override val claims: MutableStateFlow<AccessTokenClaims?> = MutableStateFlow(null)

    init {
        updateClaims(settings.getStringOrNull(KEY_USER_CLAIMS))
    }

    private fun updateClaims(claims: String?) {
        this.claims.value = claims?.let {
            decoder.decodeFromString<AccessTokenClaims>(it)
        }
    }

    override suspend fun setClaims(claims: String?) {
        settings[KEY_USER_CLAIMS] = claims
        updateClaims(claims)
    }

    override suspend fun getClaims(): AccessTokenClaims? {
        return settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            decoder.decodeFromString<AccessTokenClaims>(it)
        }
    }

    companion object {
        private const val KEY_USER_CLAIMS = "userClaims"
    }

}