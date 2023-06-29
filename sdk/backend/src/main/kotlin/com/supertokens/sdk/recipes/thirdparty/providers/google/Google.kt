package com.supertokens.sdk.recipes.thirdparty.providers.google

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProvider
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProviderConfig
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyProviderException
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class GoogleConfig: OAuthProviderConfig()

class GoogleProvider(
    private val superTokens: SuperTokens,
    config: GoogleConfig,
): OAuthProvider<GoogleConfig>(config) {

    override val id = ID
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

    override suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo {
        val response = superTokens.client.get(USER_URL) {
            bearerAuth(accessToken)
        }

        if (response.status != HttpStatusCode.OK) {
            throw ThirdPartyProviderException(response.bodyAsText())
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
        const val ID = "google"

        const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        const val USER_URL = "https://www.googleapis.com/oauth2/v1/userinfo"
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