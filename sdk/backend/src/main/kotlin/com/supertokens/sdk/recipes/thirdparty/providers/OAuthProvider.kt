package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

abstract class OAuthProviderConfig: ProviderConfig {
    override var isDefault = false
    var scopes: List<String>? = null
    var clientId: String? = null
    open val clientSecret: String? = null
}

abstract class OAuthProvider<out C: OAuthProviderConfig>(
    internal val superTokens: SuperTokens,
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
    val clientSecret: String
        get() = config.clientSecret ?: throw RuntimeException("clientSecret not configured for provider ${this::class.simpleName}")

    abstract val authUrl: String
    abstract val tokenUrl: String
    abstract val defaultScopes: List<String>
    open val authParams: Map<String, String>? = null
    open val tokenParams: Map<String, String>? = null

    override fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?) = ProviderEndpoint(
        url = tokenUrl,
        params = buildMap {
            set("client_id", clientId)
            set("client_secret", clientSecret)
            tokenParams?.forEach { (key, value) -> set(key, value) }

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
            authParams?.forEach { (key, value) -> set(key, value) }
        }
    )

    open suspend fun convertTokenResponse(jsonObject: JsonObject): ThirdPartyTokenResponse = ThirdPartyTokenResponse(
        accessToken = jsonObject["access_token"]?.jsonPrimitive?.content ?: throw ThirdPartyProviderException("'access_token' not in response for ${this::class.simpleName}"),
        idToken = jsonObject["id_token"]?.jsonPrimitive?.content,
    )

    override suspend fun getTokens(authCode: String, redirectUrl: String?): ThirdPartyTokenResponse {
        val response = superTokens.client.get(getAccessTokenEndpoint(authCode, redirectUrl).fullUrl)

        if (response.status != HttpStatusCode.OK) {
            throw ThirdPartyProviderException(response.bodyAsText())
        }

        val body = response.body<JsonObject>()

        return convertTokenResponse(body)
    }

}