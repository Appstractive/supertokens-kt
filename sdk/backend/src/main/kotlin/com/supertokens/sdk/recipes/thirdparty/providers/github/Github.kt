package com.supertokens.sdk.recipes.thirdparty.providers.github

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderEndpoint
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import it.czerwinski.kotlin.util.Either
import it.czerwinski.kotlin.util.Left
import it.czerwinski.kotlin.util.Right

class GithubConfig : ProviderConfig {

    var scopes: List<String>? = null
    var authParams: Map<String, String>? = null
    var clientId: String? = null
    var clientSecret: String? = null

}

class GithubProvider(
    private val superTokens: SuperTokens,
    private val config: GithubConfig
) : Provider<GithubConfig>() {

    override val id = ID

    val scopes = buildList {
        addAll(DEFAULT_SCOPES)
        config.scopes?.let { addAll(it) }
    }

    val clientId: String = config.clientId ?: throw RuntimeException("clientId not configured for provider Github")
    val clientSecret: String = config.clientSecret ?: throw RuntimeException("clientSecret not configured for provider Github")

    override fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?) = ProviderEndpoint(
        url = TOKEN_URL,
        params = buildMap {
            set("client_id", clientId)
            set("client_secret", clientSecret)

            authCode?.let {
                set("code", it)
            }

            redirectUrl?.let {
                set("redirect_uri", it)
            }
        }
    )

    override fun getAuthorizationEndpoint() = ProviderEndpoint(
        url = AUTH_URL,
        params = buildMap {
            set("scope", scopes.joinToString(" "))
            set("client_id", clientId)
            config.authParams?.forEach { (key, value) -> set(key, value) }
        }
    )

    override suspend fun getUserInfo(accessToken: String): Either<SuperTokensStatus, ThirdPartyUserInfo> {
        val response = superTokens.client.get(USER_URL) {
            bearerAuth(accessToken)
            contentType(HEADER_CONTENT_TYPE)
        }

        if (response.status != HttpStatusCode.OK) {
            return Left(SuperTokensStatus.ThirdPartyProviderError(response.bodyAsText()))
        }

        val body = response.body<GithubGetUserResponse>()

        return Right(
            ThirdPartyUserInfo(
                id = body.id.toString(),
                email = getEmail(accessToken)?.let {
                    ThirdPartyEmail(
                        id = it.email,
                        isVerified = it.verified
                    )
                }
            )
        )
    }

    private suspend fun getEmail(accessToken: String): GithubGetEmailResponse? {
        val response = superTokens.client.get(EMAIL_URL) {
            bearerAuth(accessToken)
            contentType(HEADER_CONTENT_TYPE)
        }

        if (response.status != HttpStatusCode.OK) {
            return null
        }

        val body = response.body<List<GithubGetEmailResponse>>()

        return (body.firstOrNull { it.primary } ?: body.firstOrNull())
    }

    companion object {
        const val ID = "github"

        const val AUTH_URL = "https://github.com/login/oauth/authorize"
        const val TOKEN_URL = "https://github.com/login/oauth/access_token"
        const val USER_URL = "https://api.github.com/user"
        const val EMAIL_URL = "https://api.github.com/user/emails"

        val HEADER_CONTENT_TYPE = ContentType("application", "vnd.github.v3+json")

        val DEFAULT_SCOPES = listOf(
            "read:user",
            "user:email",
        )
    }

}

val Github = object : ProviderBuilder<GithubConfig, GithubProvider>() {

    override fun install(configure: GithubConfig.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> GithubProvider {
        val config = GithubConfig().apply(configure)

        return { superTokens, _ ->
            GithubProvider(
                superTokens, config,
            )
        }
    }

}