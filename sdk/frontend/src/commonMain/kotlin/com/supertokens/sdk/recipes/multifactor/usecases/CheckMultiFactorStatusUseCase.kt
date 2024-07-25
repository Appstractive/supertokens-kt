package com.supertokens.sdk.recipes.multifactor.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.MultiFactorStatusResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.models.EmailsStatus
import com.supertokens.sdk.models.FactorsStatus
import com.supertokens.sdk.models.MultiFactorAuthStatus
import com.supertokens.sdk.models.PhoneStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

internal class CheckMultiFactorStatusUseCase(
    private val client: HttpClient,
) {

  suspend fun checkStatus(): MultiFactorAuthStatus {
    val response = client.post(Routes.Mfa.CHECK)
    val body = response.body<MultiFactorStatusResponseDTO>()

    return when (body.status) {
      SuperTokensStatus.OK.value ->
          MultiFactorAuthStatus(
              factors =
                  FactorsStatus(
                      alreadySetup = checkNotNull(body.factors).alreadySetup,
                      allowedToSetup = checkNotNull(body.factors).allowedToSetup,
                      next = checkNotNull(body.factors).next,
                  ),
              emails = EmailsStatus(),
              phoneNumbers = PhoneStatus(),
          )
      else -> throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
