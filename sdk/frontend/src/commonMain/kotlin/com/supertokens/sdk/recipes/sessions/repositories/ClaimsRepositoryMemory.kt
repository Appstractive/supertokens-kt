package com.supertokens.sdk.recipes.sessions.repositories

import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class ClaimsRepositoryMemory: ClaimsRepository {

    override val claims: MutableStateFlow<AccessTokenClaims?> = MutableStateFlow(null)

    override val decoder: Json = Json {
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun setClaims(claims: String?) {
        this.claims.value = claims?.let {
            decoder.decodeFromString<AccessTokenClaims>(it)
        }
    }

    override suspend fun getClaims(): AccessTokenClaims? {
        return claims.value
    }

}