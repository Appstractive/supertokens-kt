package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensDslMarker
import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
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
    get() =
        if (params.isEmpty()) url
        else
            "$url?${params.map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }.joinToString("&")}"
}

@SuperTokensDslMarker
interface ProviderConfig {
  val isDefault: Boolean
  val clientType: String?
}

abstract class Provider<out C : ProviderConfig>(config: C) {

  abstract val id: String
  abstract val clientId: String
  val isDefault: Boolean = config.isDefault
  val clientType: String? = config.clientType

  abstract fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?): ProviderEndpoint

  abstract fun getAuthorizationEndpoint(redirectUrl: String): ProviderEndpoint

  abstract suspend fun getTokens(
      parameters: Map<String, String>,
      pkceCodeVerifier: String?,
      redirectUrl: String?
  ): ThirdPartyTokensDTO

  abstract suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo
}

typealias BuildProvider = (SuperTokens, ThirdPartyRecipe) -> Provider<*>

abstract class ProviderBuilder<out C : ProviderConfig, out R : Provider<C>> {

  abstract fun install(configure: C.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> R
}
