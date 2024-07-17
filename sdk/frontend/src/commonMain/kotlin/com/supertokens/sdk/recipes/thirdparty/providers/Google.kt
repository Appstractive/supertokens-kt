package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.recipes.thirdparty.Provider
import com.supertokens.sdk.recipes.thirdparty.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInAuthCode
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInTokens

class ProviderGoogle(
    superTokens: SuperTokensClient,
    config: ProviderConfig,
) :
    Provider<ProviderConfig>(
        id = ThirdPartyProvider.GOOGLE,
        config = config,
    )

object Google : ProviderBuilder<ProviderConfig, ProviderGoogle>() {

  const val id = ThirdPartyProvider.GOOGLE

  object AuthCode : ThirdPartySignInAuthCode(id)

  object Tokens : ThirdPartySignInTokens(id)

  override fun install(
      configure: ProviderConfig.() -> Unit
  ): (SuperTokensClient, ThirdPartyRecipe) -> ProviderGoogle {
    val config = ProviderConfig().apply(configure)

    return { superTokens, _ ->
      ProviderGoogle(
          superTokens,
          config,
      )
    }
  }
}
