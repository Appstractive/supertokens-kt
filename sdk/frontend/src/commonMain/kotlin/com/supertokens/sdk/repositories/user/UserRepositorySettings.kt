package com.supertokens.sdk.repositories.user

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.toJsonElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class UserRepositorySettings(
    private val settings: Settings,
    private val json: Json = Json,
): UserRepository {

    override val userId: MutableStateFlow<String?> = MutableStateFlow(null)
    override val roles: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val permissions: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val email: MutableStateFlow<String?> = MutableStateFlow(null)
    override val emailVerified: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        updateClaims(settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            json.decodeFromString<JsonObject>(it).extractedContent
        })
    }

    private fun updateClaims(claims: Map<String, Any?>?) {
        userId.value = claims.getUserID()
        roles.value = claims.getRoles()
        permissions.value = claims.getPermissions()
        email.value = claims.getEmail()
        emailVerified.value = claims.getEmailVerified()
    }

    override suspend fun setClaims(claims: Map<String, Any?>?) {
        settings[KEY_USER_CLAIMS] = claims?.let {
            json.encodeToString(it.toJsonElement())
        }
        updateClaims(claims)
    }

    override suspend fun getClaims(): Map<String, Any?>? {
        return settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            json.decodeFromString<JsonObject>(it).extractedContent
        }
    }

    companion object {
        private const val KEY_USER_CLAIMS = "userClaims"
    }

}