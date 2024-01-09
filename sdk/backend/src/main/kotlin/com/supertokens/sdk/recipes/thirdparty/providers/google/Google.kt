package com.supertokens.sdk.recipes.thirdparty.providers.google

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProvider
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProviderConfig
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class GoogleConfig: OAuthProviderConfig() {
    override var clientSecret: String? = null
}

class GoogleProvider(
    superTokens: SuperTokens,
    config: GoogleConfig,
): OAuthProvider<GoogleConfig>(superTokens, config) {

    override val id = ThirdPartyProvider.GOOGLE
    override val authUrl = AUTH_URL
    override val tokenUrl = TOKEN_URL
    override val defaultScopes = listOf(
        "https://www.googleapis.com/auth/userinfo.email"
    )
    override val authParams by lazy {
        mapOf(
            "access_type" to "offline",
            "response_type" to "code",
            "include_granted_scopes" to "true",
        )
    }
    override val tokenParams by lazy {
        mapOf(
            "grant_type" to "authorization_code",
        )
    }

    override suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo {
        val response = superTokens.client.get(USER_URL) {
            bearerAuth(tokenResponse.accessToken)
        }

        if (response.status != HttpStatusCode.OK) {
            throw SuperTokensStatusException(SuperTokensStatus.WrongCredentialsError, response.bodyAsText())
        }

        val body = response.body<GoogleGetUserResponse>()

        return ThirdPartyUserInfo(
            id = body.id,
            email = body.email?.let {
                ThirdPartyEmail(
                    id = it,
                    isVerified = body.verified_email,
                )
            }
        )
    }

    companion object {
        const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        const val USER_URL = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json"
    }
}

val Google = object : ProviderBuilder<GoogleConfig, GoogleProvider>() {

    override fun install(configure: GoogleConfig.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> GoogleProvider {
        val config = GoogleConfig().apply(configure)

        return { superTokens, _ ->
            GoogleProvider(
                superTokens, config,
            )
        }
    }

}