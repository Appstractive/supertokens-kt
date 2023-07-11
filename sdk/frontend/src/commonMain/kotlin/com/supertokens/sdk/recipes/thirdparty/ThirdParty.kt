package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.ThirdPartySignInUpRequest
import com.supertokens.sdk.common.responses.AuthorizationUrlResponse
import com.supertokens.sdk.common.responses.SignInUpResponse
import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.models.SignInData
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

abstract class ThirdParty<C : SignInProviderConfig>: SignInProvider<C, SignInData> {

    internal abstract val providerId: String

    internal abstract val config: () -> C

    internal abstract val request: (C) -> ThirdPartySignInUpRequest

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: C.() -> Unit): SignInData {
        val config = config.invoke().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.ThirdParty.SIGN_IN_UP) {
            setBody(request.invoke(config))
        }

        val body = response.body<SignInUpResponse>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                SignInData(
                    user = checkNotNull(body.user),
                    createdNewUser = checkNotNull(body.createdNewUser),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

}

abstract class ThirdPartyAuthCode(
    override val providerId: String,
) : ThirdParty<ThirdPartyAuthCode.Config>() {

    override val config = {
        Config()
    }

    override val request = { config: Config ->
        ThirdPartySignInUpRequest(
            redirectURI = config.redirectURI,
            code = config.code,
            thirdPartyId = providerId,
            clientId = config.clientId,
        )
    }

    data class Config(
        var redirectURI: String? = null,
        var code: String = "",
        var clientId: String? = null,
    ) : SignInProviderConfig

}

abstract class ThirdPartyTokens(
    override val providerId: String,
) : ThirdParty<ThirdPartyTokens.Config>() {

    override val config = {
        Config()
    }

    override val request = { config: Config ->
        ThirdPartySignInUpRequest(
            redirectURI = config.redirectURI,
            authCodeResponse = ThirdPartyTokenResponse(
                accessToken = config.accessToken,
                idToken = config.idToken,
            ),
            thirdPartyId = providerId,
            clientId = config.clientId,
        )
    }

    data class Config(
        var redirectURI: String? = null,
        var accessToken: String = "",
        var idToken: String? = null,
        var clientId: String? = null,
    ) : SignInProviderConfig

}

suspend fun SuperTokensClient.getThirdPartyAuthorizationUrl(provider: Provider): String {
    val response = apiClient.get("${Routes.ThirdParty.AUTH_URL}?thirdPartyId=${provider.id}")

    val body = response.body<AuthorizationUrlResponse>()

    return when(body.status) {
        SuperTokensStatus.OK.value -> checkNotNull(body.url)
        else -> throw SuperTokensStatusException(body.status.toStatus())
    }
}