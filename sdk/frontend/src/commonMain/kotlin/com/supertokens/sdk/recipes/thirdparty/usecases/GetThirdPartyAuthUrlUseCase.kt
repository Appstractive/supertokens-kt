package com.supertokens.sdk.recipes.thirdparty.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.AuthorizationUrlResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.thirdparty.Provider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.path

class GetThirdPartyAuthUrlUseCase(
    private val client: HttpClient,
) {

    suspend fun getAuthUrl(provider: Provider<*>): String {
        val response = client.get {
            url {
                path(Routes.ThirdParty.AUTH_URL)
                parameters.append("thirdPartyId", provider.id)
                parameters.append("redirectURIOnProviderDashboard", checkNotNull(provider.config.redirectUri))
            }

        }

        val body = response.body<AuthorizationUrlResponseDTO>()

        return when(body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.urlWithQueryParams)
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

}