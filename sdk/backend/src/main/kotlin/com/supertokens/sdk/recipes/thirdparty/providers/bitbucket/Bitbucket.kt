package com.supertokens.sdk.recipes.thirdparty.providers.bitbucket

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
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

class BitbucketConfig: OAuthProviderConfig() {
    override var clientSecret: String? = null
}

class BitbucketProvider(
    superTokens: SuperTokens,
    config: BitbucketConfig,
): OAuthProvider<BitbucketConfig>(superTokens, config) {

    override val id = ID

    override val authUrl = AUTH_URL
    override val tokenUrl = TOKEN_URL
    override val defaultScopes = listOf(
        "account",
        "email",
    )
    override val authParams by lazy {
        mapOf(
            "access_type" to "offline",
            "response_type" to "code",
        )
    }
    override val tokenParams by lazy {
        mapOf(
            "grant_type" to "authorization_code",
        )
    }

    override suspend fun getUserInfo(tokenResponse: ThirdPartyTokenResponse): ThirdPartyUserInfo {
        val response = superTokens.client.get(USER_URL) {
            bearerAuth(tokenResponse.accessToken)
        }

        if (response.status != HttpStatusCode.OK) {
            throw ThirdPartyProviderException(response.bodyAsText())
        }

        val body = response.body<BitbucketGetUserResponse>()

        return ThirdPartyUserInfo(
            id = body.uuid,
            email = getEmail(tokenResponse.accessToken)?.let {
                ThirdPartyEmail(
                    id = it.email,
                    isVerified = it.is_confirmed
                )
            }
        )
    }

    private suspend fun getEmail(accessToken: String): BitbucketGetEmailsResponse? {
        val response = superTokens.client.get(EMAIL_URL) {
            bearerAuth(accessToken)
        }

        if (response.status != HttpStatusCode.OK) {
            return null
        }

        val body = response.body<List<BitbucketGetEmailsResponse>>()

        return (body.firstOrNull { it.is_primary } ?: body.firstOrNull())
    }

    companion object {
        const val ID = "bitbucket"

        const val AUTH_URL = "https://bitbucket.org/site/oauth2/authorize"
        const val TOKEN_URL = "https://bitbucket.org/site/oauth2/access_token"
        const val USER_URL = "https://api.bitbucket.org/2.0/user"
        const val EMAIL_URL = "https://api.bitbucket.org/2.0/user/emails"
    }

}

val Bitbucket = object : ProviderBuilder<BitbucketConfig, BitbucketProvider>() {

    override fun install(configure: BitbucketConfig.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> BitbucketProvider {
        val config = BitbucketConfig().apply(configure)

        return { superTokens, _ ->
            BitbucketProvider(
                superTokens, config,
            )
        }
    }

}