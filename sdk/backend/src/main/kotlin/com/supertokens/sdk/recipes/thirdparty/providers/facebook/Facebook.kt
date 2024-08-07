package com.supertokens.sdk.recipes.thirdparty.providers.facebook

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
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class FacebookConfig : OAuthProviderConfig() {
  override var clientSecret: String? = null
}

class FacebookProvider(
    superTokens: SuperTokens,
    config: FacebookConfig,
) : OAuthProvider<FacebookConfig>(superTokens, config) {

  override val id = ThirdPartyAuth.FACEBOOK

  override val authUrl = AUTH_URL
  override val tokenUrl = TOKEN_URL
  override val defaultScopes =
      listOf(
          "email",
      )

  override suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo {
    val response = superTokens.client.get("$USER_URL&access_token=${tokenResponse.accessToken}")

    if (response.status != HttpStatusCode.OK) {
      throw SuperTokensStatusException(
          SuperTokensStatus.WrongCredentialsError, response.bodyAsText())
    }

    val body = response.body<FacebookGetUserResponse>()

    return ThirdPartyUserInfo(
        id = body.id,
        email =
            ThirdPartyEmail(
                id = body.email,
                isVerified = true,
            ),
    )
  }

  companion object {
    const val AUTH_URL = "https://www.facebook.com/v9.0/dialog/oauth"
    const val TOKEN_URL = "https://graph.facebook.com/v9.0/oauth/access_token"
    const val USER_URL = "https://graph.facebook.com/me?fields=id,email&format=json"
  }
}

val Facebook =
    object : ProviderBuilder<FacebookConfig, FacebookProvider>() {

      override fun install(
          configure: FacebookConfig.() -> Unit
      ): (SuperTokens, ThirdPartyRecipe) -> FacebookProvider {
        val config = FacebookConfig().apply(configure)

        return { superTokens, _ ->
          FacebookProvider(
              superTokens,
              config,
          )
        }
      }
    }
