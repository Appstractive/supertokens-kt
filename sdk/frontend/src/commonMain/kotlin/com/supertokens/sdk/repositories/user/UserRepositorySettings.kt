package com.supertokens.sdk.repositories.user

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.supertokens.sdk.common.claims.AccessTokenClaims
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.toJsonElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class UserRepositorySettings(
    private val settings: Settings,
    private val json: Json = Json,
): UserRepository {

    override val claims: MutableStateFlow<AccessTokenClaims?> = MutableStateFlow(null)

    init {
        updateClaims(settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            json.decodeFromString<AccessTokenClaims>(it)
        })
    }

    private fun updateClaims(claims: AccessTokenClaims?) {
        this.claims.value = claims
    }

    override suspend fun setClaims(claims: AccessTokenClaims?) {
        settings[KEY_USER_CLAIMS] = claims?.let {
            json.encodeToString(it)
        }
        updateClaims(claims)
    }

    override suspend fun getClaims(): AccessTokenClaims? {
        return settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            json.decodeFromString<AccessTokenClaims>(it)
        }
    }

    companion object {
        private const val KEY_USER_CLAIMS = "userClaims"
    }

}