package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.recipes.thirdparty.Provider
import com.supertokens.sdk.recipes.thirdparty.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInAuthCode
import com.supertokens.sdk.recipes.thirdparty.ThirdPartySignInTokens

class ProviderGitHub(
    superTokens: SuperTokensClient,
    config: ProviderConfig,
): Provider<ProviderConfig>(
    id = ThirdPartyProvider.GITHUB,
    config = config,
)

object GitHub: ProviderBuilder<ProviderConfig, ProviderGitHub>() {

    const val id = ThirdPartyProvider.GITHUB

    object AuthCode: ThirdPartySignInAuthCode(id)
    object Tokens: ThirdPartySignInTokens(id)

    override fun install(configure: ProviderConfig.() -> Unit): (SuperTokensClient, ThirdPartyRecipe) -> ProviderGitHub {
        val config = ProviderConfig().apply(configure)

        return { superTokens, _ ->
            ProviderGitHub(
                superTokens, config,
            )
        }
    }

}