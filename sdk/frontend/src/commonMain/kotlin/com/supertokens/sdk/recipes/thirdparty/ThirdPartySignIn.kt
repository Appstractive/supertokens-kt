package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.models.SignInData



abstract class ThirdPartySignIn<C : SignInProviderConfig>: SignInProvider<C, SignInData> {

    internal abstract val providerId: String

}

abstract class ThirdPartySignInAuthCode(
    override val providerId: String,
) : ThirdPartySignIn<ThirdPartySignInAuthCode.Config>() {

    data class Config(
        var redirectURIQueryParams: Map<String, String>? = null,
        var clientType: String? = null,
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: Config.() -> Unit): SignInData {
        val config = Config().apply(configure)
        val provider = superTokensClient.getRecipe<ThirdPartyRecipe>().getProviderById(providerId)

        return superTokensClient.getRecipe<ThirdPartyRecipe>().thirdPartyAuthCodeSignIn(
            providerId = providerId,
            pkceCodeVerifier = superTokensClient.pkceRepository.getPkceCodeVerifier(providerId),
            redirectURI = checkNotNull(provider.config.redirectUri),
            redirectURIQueryParams = config.redirectURIQueryParams ?: emptyMap(),
            clientType = config.clientType,
        )
    }
}

abstract class ThirdPartySignInTokens(
    override val providerId: String,
) : ThirdPartySignIn<ThirdPartySignInTokens.Config>() {

    data class Config(
        var accessToken: String = "",
        var idToken: String? = null,
        var clientType: String? = null,
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: Config.() -> Unit): SignInData {
        val config = Config().apply(configure)

        return superTokensClient.getRecipe<ThirdPartyRecipe>().thirdPartyTokenSignIn(
            providerId = providerId,
            accessToken = config.accessToken,
            idToken = config.idToken,
            clientType = config.clientType,
        )
    }
}