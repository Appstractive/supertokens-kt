package com.supertokens.sdk.recipes.thirdparty.providers

abstract class OAuthProviderConfig: ProviderConfig {
    override var isDefault = false
    var scopes: List<String>? = null
    var authParams: Map<String, String>? = null
    var clientId: String? = null
    var clientSecret: String? = null
}

abstract class OAuthProvider<out C: OAuthProviderConfig>(
    internal val config: C
): Provider<C>() {

    override val isDefault = config.isDefault
    val scopes by lazy {
        buildList {
            addAll(defaultScopes)
            config.scopes?.let { addAll(it) }
        }
    }

    val clientId: String = config.clientId ?: throw RuntimeException("clientId not configured for provider ${this::class.simpleName}")
    val clientSecret: String = config.clientSecret ?: throw RuntimeException("clientSecret not configured for provider ${this::class.simpleName}")

    abstract val authUrl: String
    abstract val tokenUrl: String
    abstract val defaultScopes: List<String>

    override fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?) = ProviderEndpoint(
        url = tokenUrl,
        params = buildMap {
            set("client_id", clientId)
            set("client_secret", clientSecret)

            authCode?.let {
                set("code", it)
            }

            redirectUrl?.let {
                set("redirect_uri", it)
            }
        }
    )

    override fun getAuthorizationEndpoint() = ProviderEndpoint(
        url = authUrl,
        params = buildMap {
            set("scope", scopes.joinToString(" "))
            set("client_id", clientId)
            config.authParams?.forEach { (key, value) -> set(key, value) }
        }
    )

}