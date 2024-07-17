package com.supertokens.sdk.recipes.thirdparty.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.RedirectUriInfoDTO
import com.supertokens.sdk.common.requests.ThirdPartySignInUpRequestDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.models.SignInData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendEncodedPathSegments

class ThirdPartyAuthCodeSignInUseCase(
    private val client: HttpClient,
    private val tenantId: String?,
) {

  suspend fun signIn(
      providerId: String,
      pkceCodeVerifier: String,
      redirectURI: String,
      redirectURIQueryParams: Map<String, String>,
      clientType: String? = null,
  ): SignInData {
    val response =
        client.post {
          url {
            appendEncodedPathSegments(
                listOfNotNull(
                    tenantId,
                    Routes.ThirdParty.SIGN_IN_UP,
                ))
          }
          setBody(
              ThirdPartySignInUpRequestDTO(
                  thirdPartyId = providerId,
                  redirectURIInfo =
                      RedirectUriInfoDTO(
                          redirectURIOnProviderDashboard = redirectURI,
                          redirectURIQueryParams = redirectURIQueryParams ?: emptyMap(),
                          pkceCodeVerifier = pkceCodeVerifier,
                      ),
                  clientType = clientType,
              ))
        }

    val body = response.body<SignInUpResponseDTO>()

    return when (val status = body.status.toStatus()) {
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
