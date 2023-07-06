package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensProviderDslMarker
import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import java.net.URLEncoder

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
        get() = if(params.isEmpty()) url else "$url?${params.map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }.joinToString("&")}"

}

@SuperTokensProviderDslMarker
interface ProviderConfig {
    val isDefault: Boolean
}

abstract class Provider<out C: ProviderConfig> {

    abstract val id: String
    abstract val clientId: String
    abstract val isDefault: Boolean

    abstract fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?): ProviderEndpoint
    abstract fun getAuthorizationEndpoint(): ProviderEndpoint
    abstract suspend fun getTokens(authCode: String, redirectUrl: String?): ThirdPartyTokenResponse
    abstract suspend fun getUserInfo(tokenResponse: ThirdPartyTokenResponse): ThirdPartyUserInfo

}

typealias BuildProvider = (SuperTokens, ThirdPartyRecipe) -> Provider<*>

abstract class ProviderBuilder<out C: ProviderConfig, out R: Provider<C>> {

    abstract fun install(configure: C.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> R

}