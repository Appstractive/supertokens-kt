package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.recipes.thirdparty.Provider
import com.supertokens.sdk.recipes.thirdparty.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInAuthCode
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInTokens

class ProviderGitLab(
    superTokens: SuperTokensClient,
    config: ProviderConfig,
) :
    Provider<ProviderConfig>(
        id = ThirdPartyProvider.GITLAB,
        config = config,
    )

object GitLab : ProviderBuilder<ProviderConfig, ProviderGitLab>() {

  const val id = ThirdPartyProvider.GITLAB

  object AuthCode : ThirdPartySignInAuthCode(id)

  object Tokens : ThirdPartySignInTokens(id)

  override fun install(
      configure: ProviderConfig.() -> Unit
  ): (SuperTokensClient, ThirdPartyRecipe) -> ProviderGitLab {
    val config = ProviderConfig().apply(configure)

    return { superTokens, _ ->
      ProviderGitLab(
          superTokens,
          config,
      )
    }
  }
}
