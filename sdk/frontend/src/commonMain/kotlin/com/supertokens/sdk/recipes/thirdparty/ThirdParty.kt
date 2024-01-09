package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.RedirectUriInfoDTO
import com.supertokens.sdk.common.requests.ThirdPartySignInUpRequestDTO
import com.supertokens.sdk.common.responses.AuthorizationUrlResponseDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
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

    internal abstract val request: (C) -> ThirdPartySignInUpRequestDTO

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: C.() -> Unit): SignInData {
        val config = config.invoke().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.ThirdParty.SIGN_IN_UP) {
            setBody(request.invoke(config))
        }

        val body = response.body<SignInUpResponseDTO>()

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
        ThirdPartySignInUpRequestDTO(
            thirdPartyId = providerId,
            redirectURIInfo = RedirectUriInfoDTO(
                redirectURIOnProviderDashboard = config.redirectURI,
                redirectURIQueryParams = config.redirectURIQueryParams ?: emptyMap(),
                pkceCodeVerifier = config.pkceCodeVerifier,
            ),
            clientType = config.clientType,
        )
    }

    data class Config(
        var pkceCodeVerifier: String = "",
        var redirectURI: String = "",
        var redirectURIQueryParams: Map<String, String>? = null,
        var clientType: String? = null,
    ) : SignInProviderConfig

}

abstract class ThirdPartyTokens(
    override val providerId: String,
) : ThirdParty<ThirdPartyTokens.Config>() {

    override val config = {
        Config()
    }

    override val request = { config: Config ->
        ThirdPartySignInUpRequestDTO(
            oAuthTokens = ThirdPartyTokensDTO(
                accessToken = config.accessToken,
                idToken = config.idToken,
            ),
            thirdPartyId = providerId,
            clientType = config.clientType,
        )
    }

    data class Config(
        var accessToken: String = "",
        var idToken: String? = null,
        var clientType: String? = null,
    ) : SignInProviderConfig

}

suspend fun SuperTokensClient.getThirdPartyAuthorizationUrl(provider: Provider): String {
    val response = apiClient.get("${Routes.ThirdParty.AUTH_URL}?thirdPartyId=${provider.id}")

    val body = response.body<AuthorizationUrlResponseDTO>()

    return when(body.status) {
        SuperTokensStatus.OK.value -> checkNotNull(body.urlWithQueryParams)
        else -> throw SuperTokensStatusException(body.status.toStatus())
    }
}