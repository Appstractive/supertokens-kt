package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import it.czerwinski.kotlin.util.Either

data class ThirdPartyEmail(
    val id: String,
    val isVerified: Boolean,
)

data class ThirdPartyUserInfo(
    val id: String,
    val email: ThirdPartyEmail?,
)

data class ProviderEndpoint(
    val url: String,
    val params: Map<String, String>,
) {

    val fullUrl: String
        get() = if(params.isEmpty()) url else "$url?${params.map { "${it.key}=${it.value}" }.joinToString("&")}"

}

interface ProviderConfig

abstract class Provider<C: ProviderConfig> {

    abstract val id: String

    abstract fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?): ProviderEndpoint
    abstract fun getAuthorizationEndpoint(): ProviderEndpoint
    abstract suspend fun getUserInfo(accessToken: String): Either<SuperTokensStatus, ThirdPartyUserInfo>

}

typealias BuildProvider = (SuperTokens, ThirdPartyRecipe) -> Provider<*>

abstract class ProviderBuilder<C: ProviderConfig, R: Provider<C>> {

    abstract fun install(configure: C.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> R

}