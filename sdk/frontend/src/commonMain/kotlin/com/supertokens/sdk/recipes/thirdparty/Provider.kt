package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.SuperTokensClient

open class ProviderConfig {
  var redirectUri: String? = null
}

abstract class Provider<out C : ProviderConfig>(
    val id: String,
    val config: C,
)

typealias BuildProvider = (SuperTokensClient, ThirdPartyRecipe) -> Provider<*>

abstract class ProviderBuilder<out C : ProviderConfig, out R : Provider<C>> {

  abstract fun install(configure: C.() -> Unit): (SuperTokensClient, ThirdPartyRecipe) -> R
}
