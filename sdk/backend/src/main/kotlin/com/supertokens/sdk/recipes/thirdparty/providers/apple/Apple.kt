package com.supertokens.sdk.recipes.thirdparty.providers.apple

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProvider
import com.supertokens.sdk.recipes.thirdparty.providers.OAuthProviderConfig
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import com.supertokens.sdk.recipes.thirdparty.providers.TokenResponse
import io.fusionauth.jwt.Verifier
import io.fusionauth.jwt.domain.Algorithm
import io.fusionauth.jwt.domain.JWT
import io.fusionauth.jwt.ec.ECSigner
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun createAppleToken(teamId: String, keyId: String, privateKey: String, clientId: String): String {
    val signer = ECSigner.newSHA256Signer(privateKey, keyId)
    val jwt = JWT()
        .setIssuer(teamId)
        .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
        .setSubject(clientId)
        .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(60))
        .setAudience("https://appleid.apple.com")

    return JWT.getEncoder().encode(jwt, signer)
}

class AppleConfig : OAuthProviderConfig() {
    var keyId: String? = null
    var privateKey: String? = null
    var teamId: String? = null
    override val clientSecret: String
        get() = createAppleToken(
            teamId = teamId ?: throw RuntimeException("teamId not configured for Apple provider"),
            keyId = keyId ?: throw RuntimeException("keyId not configured for Apple provider"),
            privateKey = privateKey ?: throw RuntimeException("privateKey not configured for Apple provider"),
            clientId = clientId ?: throw RuntimeException("clientId not configured for Apple provider"),
        )
}

class AppleProvider(
    superTokens: SuperTokens,
    config: AppleConfig,
) : OAuthProvider<AppleConfig>(superTokens, config) {

    override val id = ID
    override val authUrl = AUTH_URL
    override val tokenUrl = TOKEN_URL
    override val defaultScopes = listOf(
        "email"
    )
    override val authParams by lazy {
        mapOf(
            "response_mode" to "form_post",
            "response_type" to "code",
        )
    }
    override val tokenParams by lazy {
        mapOf(
            "grant_type" to "authorization_code",
        )
    }

    override suspend fun getUserInfo(tokenResponse: TokenResponse): ThirdPartyUserInfo {
        val idToken = tokenResponse.idToken ?: throw RuntimeException("No IdToken in TokenResponse")

        // TODO actual verifier
        val jwt = JWT.getDecoder().decode(idToken, object: Verifier {
            override fun canVerify(algorithm: Algorithm?) = true

            override fun verify(algorithm: Algorithm?, message: ByteArray?, signature: ByteArray?) {}
        })
        val userId = jwt.subject
        val email = jwt.otherClaims["email"] as? String
        val emailVerified = jwt.otherClaims["email_verified"] as? Boolean

        return ThirdPartyUserInfo(
            id = userId,
            email = email?.let {
                ThirdPartyEmail(
                    id = it,
                    isVerified = emailVerified ?: false
                )
            }
        )
    }

    companion object {
        const val ID = "apple"

        const val AUTH_URL = "https://appleid.apple.com/auth/authorize"
        const val TOKEN_URL = "https://appleid.apple.com/auth/token"
    }
}

val Apple = object : ProviderBuilder<AppleConfig, AppleProvider>() {

    override fun install(configure: AppleConfig.() -> Unit): (SuperTokens, ThirdPartyRecipe) -> AppleProvider {
        val config = AppleConfig().apply(configure)

        return { superTokens, _ ->
            AppleProvider(
                superTokens, config,
            )
        }
    }

}