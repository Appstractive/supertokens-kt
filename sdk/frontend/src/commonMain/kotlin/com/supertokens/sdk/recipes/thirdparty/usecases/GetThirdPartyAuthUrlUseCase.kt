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
import io.ktor.http.appendEncodedPathSegments

internal class GetThirdPartyAuthUrlUseCase(
    private val client: HttpClient,
    private val tenantId: String?,
) {

  suspend fun getAuthUrl(provider: Provider<*>, clientType: String? = null): String {
    val response =
        client.get {
          url {
            appendEncodedPathSegments(
                listOfNotNull(
                    tenantId,
                    Routes.ThirdParty.AUTH_URL,
                ),
            )
            parameters.append("thirdPartyId", provider.id)
            parameters.append(
                "redirectURIOnProviderDashboard",
                checkNotNull(provider.config.redirectUri),
            )
            clientType?.let {
              parameters.append("clientType", it)
            }
          }
        }

    val body = response.body<AuthorizationUrlResponseDTO>()

    return when (body.status) {
      SuperTokensStatus.OK.value -> checkNotNull(body.urlWithQueryParams)
      else -> throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
