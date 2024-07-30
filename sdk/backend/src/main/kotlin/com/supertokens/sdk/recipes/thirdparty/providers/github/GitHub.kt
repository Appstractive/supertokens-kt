package com.supertokens.sdk.recipes.thirdparty.providers.github

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.ThirdPartyAuth
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
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class GitHubConfig : OAuthProviderConfig() {
  override var clientSecret: String? = null
}

class GitHubProvider(
    superTokens: SuperTokens,
    config: GitHubConfig,
) : OAuthProvider<GitHubConfig>(superTokens, config) {

  override val id = ThirdPartyAuth.GITHUB
  override val authUrl = AUTH_URL
  override val tokenUrl = TOKEN_URL
  override val defaultScopes =
      listOf(
          "read:user",
          "user:email",
      )

  override suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo {
    val response =
        superTokens.client.get(USER_URL) {
          bearerAuth(tokenResponse.accessToken)
          contentType(HEADER_CONTENT_TYPE)
        }

    if (response.status != HttpStatusCode.OK) {
      throw SuperTokensStatusException(
          SuperTokensStatus.WrongCredentialsError, response.bodyAsText())
    }

    val body = response.body<GitHubGetUserResponse>()

    return ThirdPartyUserInfo(
        id = body.id.toString(),
        email =
            getEmail(tokenResponse.accessToken)?.let {
              ThirdPartyEmail(id = it.email, isVerified = it.verified)
            })
  }

  private suspend fun getEmail(accessToken: String): GitHubGetEmailsResponse? {
    val response =
        superTokens.client.get(EMAIL_URL) {
          bearerAuth(accessToken)
          contentType(HEADER_CONTENT_TYPE)
        }

    if (response.status != HttpStatusCode.OK) {
      return null
    }

    val body = response.body<List<GitHubGetEmailsResponse>>()

    return (body.firstOrNull { it.primary } ?: body.firstOrNull())
  }

  companion object {
    const val AUTH_URL = "https://github.com/login/oauth/authorize"
    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    const val USER_URL = "https://api.github.com/user"
    const val EMAIL_URL = "https://api.github.com/user/emails"

    val HEADER_CONTENT_TYPE = ContentType("application", "vnd.github.v3+json")
  }
}

val Github =
    object : ProviderBuilder<GitHubConfig, GitHubProvider>() {

      override fun install(
          configure: GitHubConfig.() -> Unit
      ): (SuperTokens, ThirdPartyRecipe) -> GitHubProvider {
        val config = GitHubConfig().apply(configure)

        return { superTokens, _ ->
          GitHubProvider(
              superTokens,
              config,
          )
        }
      }
    }
