package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
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

    override val clientId: String = config.clientId ?: throw RuntimeException("clientId not configured for provider ${this::class.simpleName}")
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

    override fun getAuthorizationEndpoint(redirectUrl: String): ProviderEndpoint = ProviderEndpoint(
        url = authUrl,
        params = buildMap {
            set("scope", scopes.joinToString(" "))
            set("client_id", clientId)
            set("redirect_uri", redirectUrl)
            authParams?.forEach { (key, value) -> set(key, value) }
        }
    )

    open suspend fun convertTokenResponse(jsonObject: JsonObject): ThirdPartyTokensDTO = ThirdPartyTokensDTO(
        accessToken = jsonObject["access_token"]?.jsonPrimitive?.content ?: throw RuntimeException("'access_token' not in response for ${this::class.simpleName}"),
        idToken = jsonObject["id_token"]?.jsonPrimitive?.content,
    )

    override suspend fun getTokens(parameters: Map<String, String>, pkceCodeVerifier: String?, redirectUrl: String?): ThirdPartyTokensDTO {
        val code = parameters["code"] ?: throw RuntimeException("'code' not in parameters for ${this::class.simpleName}")
        val response = superTokens.client.get(getAccessTokenEndpoint(code, redirectUrl).fullUrl)

        if (response.status != HttpStatusCode.OK) {
            throw SuperTokensStatusException(SuperTokensStatus.WrongCredentialsError, response.bodyAsText())
        }

        val body = response.body<JsonObject>()

        return convertTokenResponse(body)
    }

}