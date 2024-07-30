package com.supertokens.sdk.recipes.thirdparty.providers.apple

import com.auth0.jwk.Jwk
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.ThirdPartyAuth
import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProviderConfig
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderEndpoint
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.util.decodeBase64Bytes
import java.net.URL
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val keyFactory = KeyFactory.getInstance("EC")

fun createAppleToken(teamId: String, keyId: String, privateKey: String, clientId: String): String {
  val key =
      keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKey.decodeBase64Bytes()))
          as ECPrivateKey
  val alg = Algorithm.ECDSA256(key)
  val jwt =
      JWT.create()
          .withIssuer(teamId)
          .withIssuedAt(Instant.now())
          .withSubject(clientId)
          .withExpiresAt(Instant.now().plusSeconds(60 * 60))
          .withAudience("https://appleid.apple.com")
          .withKeyId(keyId)

  return jwt.sign(alg)
}

@Serializable
private data class AppleTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("id_token") val idToken: String? = null,
)

class AppleConfig : OAuthProviderConfig() {
  var keyId: String? = null
  var privateKey: String? = null
  var teamId: String? = null
  override val clientSecret: String
    get() =
        createAppleToken(
            teamId = teamId ?: throw RuntimeException("teamId not configured for Apple provider"),
            keyId = keyId ?: throw RuntimeException("keyId not configured for Apple provider"),
            privateKey =
                privateKey
                    ?: throw RuntimeException("privateKey not configured for Apple provider"),
            clientId =
                clientId ?: throw RuntimeException("clientId not configured for Apple provider"),
        )
}

class AppleProvider(
    private val superTokens: SuperTokens,
    private val config: AppleConfig,
) : Provider<AppleConfig>(config) {

  override val id = ThirdPartyAuth.APPLE

  val clientSecret: String
    get() = config.clientSecret

  override fun getAccessTokenEndpoint(authCode: String?, redirectUrl: String?): ProviderEndpoint =
      ProviderEndpoint(
          url = TOKEN_URL,
          params =
              buildMap {
                put("client_id", clientId)
                put("client_secret", clientSecret)
                authCode?.let { put("code", it) }
                put("grant_type", "authorization_code")
                redirectUrl?.let { put("redirect_uri", it) }
              },
      )

  override fun getAuthorizationEndpoint(redirectUrl: String): ProviderEndpoint =
      ProviderEndpoint(
          url = AUTH_URL,
          params =
              buildMap {
                set("scope", "email")
                set("response_mode", "form_post")
                set("response_type", "code")
                set("client_id", clientId)
                set("redirect_uri", redirectUrl)
              },
      )

  override val clientId: String =
      config.clientId
          ?: throw RuntimeException(
              "clientId not configured for provider ${this::class.simpleName}",
          )

  override suspend fun getTokens(
      parameters: Map<String, String>,
      pkceCodeVerifier: String?,
      redirectUrl: String?
  ): ThirdPartyTokensDTO {
    val code =
        parameters["code"]
            ?: throw RuntimeException("'code' not in parameters for ${this::class.simpleName}")
    val response =
        superTokens.client.post(TOKEN_URL) {
          setBody(
              FormDataContent(
                  Parameters.build {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("code", code)
                    append("grant_type", "authorization_code")
                    redirectUrl?.let { append("redirect_uri", it) }
                  },
              ),
          )
        }

    if (response.status != HttpStatusCode.OK) {
      throw SuperTokensStatusException(
          SuperTokensStatus.WrongCredentialsError,
          response.bodyAsText(),
      )
    }

    val body = response.body<AppleTokenResponse>()

    return ThirdPartyTokensDTO(
        accessToken = body.accessToken,
        idToken = body.idToken,
    )
  }

  val jwkProvider = UrlJwkProvider(URL("https://appleid.apple.com/auth/keys"))

  override suspend fun getUserInfo(tokenResponse: ThirdPartyTokensDTO): ThirdPartyUserInfo {
    val idToken = tokenResponse.idToken ?: throw SuperTokensStatusException(
        SuperTokensStatus.WrongCredentialsError,
        "No IdToken in TokenResponse",
    )

    val decoded = JWT.decode(idToken)
    val keyId = decoded.keyId
    val jwk = jwkProvider.get(keyId)
    val alg = jwk.makeAlgorithm()

    val verifier =
        JWT.require(alg).withIssuer("https://appleid.apple.com").withAudience(clientId).build()

    val jwt = verifier.verify(decoded)

    val userId = jwt.subject
    val email = jwt.getClaim("email").asString()
    val emailVerified = jwt.getClaim("email_verified").asBoolean()

    return ThirdPartyUserInfo(
        id = userId,
        email = email?.let { ThirdPartyEmail(id = it, isVerified = emailVerified ?: false) },
    )
  }

  companion object {
    const val AUTH_URL = "https://appleid.apple.com/auth/authorize"
    const val TOKEN_URL = "https://appleid.apple.com/auth/token"
  }
}

private fun Jwk.makeAlgorithm(): Algorithm =
    when (algorithm) {
      "RS256" -> Algorithm.RSA256(publicKey as RSAPublicKey, null)
      "RS384" -> Algorithm.RSA384(publicKey as RSAPublicKey, null)
      "RS512" -> Algorithm.RSA512(publicKey as RSAPublicKey, null)
      "ES256" -> Algorithm.ECDSA256(publicKey as ECPublicKey, null)
      "ES384" -> Algorithm.ECDSA384(publicKey as ECPublicKey, null)
      "ES512" -> Algorithm.ECDSA512(publicKey as ECPublicKey, null)
      null -> Algorithm.RSA256(publicKey as RSAPublicKey, null)
      else -> throw IllegalArgumentException("Unsupported algorithm $algorithm")
    }

val Apple =
    object : ProviderBuilder<AppleConfig, AppleProvider>() {

      override fun install(
          configure: AppleConfig.() -> Unit
      ): (SuperTokens, ThirdPartyRecipe) -> AppleProvider {
        val config = AppleConfig().apply(configure)

        return { superTokens, _ ->
          AppleProvider(
              superTokens,
              config,
          )
        }
      }
    }
