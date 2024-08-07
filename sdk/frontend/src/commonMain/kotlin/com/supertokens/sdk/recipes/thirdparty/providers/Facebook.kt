package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.ThirdPartyAuth
import com.supertokens.sdk.recipes.thirdparty.Provider
import com.supertokens.sdk.recipes.thirdparty.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInAuthCode
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInTokens

class ProviderFacebook(
    superTokens: SuperTokensClient,
    config: ProviderConfig,
) :
    Provider<ProviderConfig>(
        id = ThirdPartyAuth.FACEBOOK,
        config = config,
    )

object Facebook : ProviderBuilder<ProviderConfig, ProviderFacebook>() {

  const val id = ThirdPartyAuth.FACEBOOK

  object AuthCode : ThirdPartySignInAuthCode(id)

  object Tokens : ThirdPartySignInTokens(id)

  override fun install(
      configure: ProviderConfig.() -> Unit
  ): (SuperTokensClient, ThirdPartyRecipe) -> ProviderFacebook {
    val config = ProviderConfig().apply(configure)

    return { superTokens, _ ->
      ProviderFacebook(
          superTokens,
          config,
      )
    }
  }
}
