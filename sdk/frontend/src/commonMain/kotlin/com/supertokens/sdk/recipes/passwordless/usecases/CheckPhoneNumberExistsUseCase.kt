package com.supertokens.sdk.recipes.passwordless.usecases

import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ExistsResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.appendEncodedPathSegments

class CheckPhoneNumberExistsUseCase(
    private val client: HttpClient,
    private val tenantId: String?,
) {

  suspend fun checkPhoneNumberExists(phoneNumber: String): Boolean {
    val response =
        client.get {
          url {
            appendEncodedPathSegments(
                listOfNotNull(
                    tenantId,
                    Routes.PHONE_NUMBER_EXISTS,
                ))
            parameters.append("phoneNumber", phoneNumber)
          }
          header(HEADER_RECIPE_ID, RECIPE_PASSWORDLESS)
        }

    val body = response.body<ExistsResponseDTO>()

    return when (body.status) {
      SuperTokensStatus.OK.value -> body.exists
      else -> throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
