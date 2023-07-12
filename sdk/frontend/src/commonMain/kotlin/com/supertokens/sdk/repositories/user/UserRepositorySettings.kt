package com.supertokens.sdk.repositories.user

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.toJsonElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class UserRepositorySettings(
    private val settings: Settings,
    private val json: Json = Json,
): UserRepository {

    override suspend fun setClaims(claims: Map<String, Any?>?) {
        settings[KEY_USER_CLAIMS] = claims?.let {
            json.encodeToString(it.toJsonElement())
        }
    }

    override suspend fun getClaims(): Map<String, Any?>? {
        return settings.getStringOrNull(KEY_USER_CLAIMS)?.let {
            json.decodeFromString<JsonObject>(it).extractedContent
        }
    }

    companion object {
        const val KEY_USER_CLAIMS = "userClaims"
    }

}