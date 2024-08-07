package com.supertokens.sdk.recipes.thirdparty.providers.gitlab

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
import io.ktor.http.HttpStatusCode

class GitLabConfig : OAuthProviderConfig() {

  var baseUrl = "https://gitlab.com"
  override var clientSecret: String? = null
}

class GitLabProvider(
    superTokens: SuperTokens,
    config: GitLabConfig,
) : OAuthProvider<GitLabConfig>(superTokens, config) {

  override val id = ThirdPartyAuth.GITLAB
  override val authUrl = "${config.baseUrl}$AUTH_PATH"
  override val tokenUrl = "${config.baseUrl}$TOKEN_PATH"
  override val defaultScopes =
      listOf(
          "read_user",
      )
  override val authParams by lazy {
    mapOf(
        "response_type" to "code",
    )
  }
  override val tokenParams by lazy {
    mapOf(
        "grant_type" to "authorization_code",
    )
  }

  override suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo {
    val response =
        superTokens.client.get("${config.baseUrl}$USER_PATH") {
          bearerAuth(tokenResponse.accessToken)
        }

    if (response.status != HttpStatusCode.OK) {
      throw SuperTokensStatusException(
          SuperTokensStatus.WrongCredentialsError, response.bodyAsText())
    }

    val body = response.body<GitLabGetUserResponse>()

    return ThirdPartyUserInfo(
        id = body.id.toString(),
        email =
            body.email?.let {
              ThirdPartyEmail(
                  id = it,
                  isVerified = true,
              )
            })
  }

  companion object {
    const val AUTH_PATH = "/oauth/authorize"
    const val TOKEN_PATH = "/oauth/token"
    const val USER_PATH = "/api/v4/user"
  }
}

val GitLab =
    object : ProviderBuilder<GitLabConfig, GitLabProvider>() {

      override fun install(
          configure: GitLabConfig.() -> Unit
      ): (SuperTokens, ThirdPartyRecipe) -> GitLabProvider {
        val config = GitLabConfig().apply(configure)

        return { superTokens, _ ->
          GitLabProvider(
              superTokens,
              config,
          )
        }
      }
    }
