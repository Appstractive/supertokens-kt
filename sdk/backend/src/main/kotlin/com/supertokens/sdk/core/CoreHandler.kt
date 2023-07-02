package com.supertokens.sdk.core

import com.supertokens.sdk.SuperTokens
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.JsonObject

class CoreHandler {

    suspend fun SuperTokens.getJwks(): JsonObject {
        val response = client.get("/.well-known/jwks.json")

        return response.body()
    }

}

suspend fun SuperTokens.getJwks(): JsonObject = with(core) {
    return getJwks()
}