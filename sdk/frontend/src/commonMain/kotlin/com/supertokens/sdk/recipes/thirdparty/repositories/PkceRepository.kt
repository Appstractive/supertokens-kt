package com.supertokens.sdk.recipes.thirdparty.repositories

import com.supertokens.sdk.common.util.generateCodeChallenge
import com.supertokens.sdk.common.util.generateCodeVerifier

interface PkceRepository {

    suspend fun generatePkceCodeVerifier(providerId: String): String
    suspend fun getPkceCodeVerifier(providerId: String): String
    suspend fun getPkceCodeChallenge(providerId: String): String

}

class PkceRepositoryImpl: PkceRepository {

    private val codeVerifiers: MutableMap<String, String> = mutableMapOf()

    override suspend fun generatePkceCodeVerifier(providerId: String): String {
        val codeVerifier = generateCodeVerifier()
        codeVerifiers[providerId] = codeVerifier
        return codeVerifier
    }

    override suspend fun getPkceCodeVerifier(providerId: String): String {
        return codeVerifiers[providerId] ?: generatePkceCodeVerifier(providerId)
    }

    override suspend fun getPkceCodeChallenge(providerId: String): String {
        return generateCodeChallenge(getPkceCodeVerifier(providerId))
    }


}