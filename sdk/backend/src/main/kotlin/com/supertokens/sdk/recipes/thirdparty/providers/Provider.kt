package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe

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

interface ProviderConfig {
    val isDefault: Boolean
}

abstract class Provider<out C: ProviderConfig> {

    abstract val id: String
    abstract val isDefault: Boolean

    abstract fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?): ProviderEndpoint
    abstract fun getAuthorizationEndpoint(): ProviderEndpoint
    abstract suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo

}

typealias BuildProvider = (SuperTokens, ThirdPartyRecipe) -> Provider<*>

abstract class ProviderBuilder<out C: ProviderConfig, out R: Provider<C>> {

    abstract fun install(configure: C.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> R

}