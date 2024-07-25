package com.supertokens.sdk.recipes.emailpassword.usecases

import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.FormFieldDTO
import com.supertokens.sdk.common.requests.PasswordResetRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendEncodedPathSegments

internal class PasswordResetUseCase(
    private val client: HttpClient,
    private val tenantId: String?,
) {

  suspend fun resetPassword(token: String, newPassword: String) {
    val response =
        client.post {
          url {
            appendEncodedPathSegments(
                listOfNotNull(
                    tenantId,
                    Routes.EmailPassword.PASSWORD_RESET,
                ),
            )
          }
          setBody(
              PasswordResetRequestDTO(
                  token = token,
                  formFields =
                      listOf(
                          FormFieldDTO(
                              id = FORM_FIELD_PASSWORD_ID,
                              value = newPassword,
                          ),
                      ),
              ),
          )
        }

    val body = response.body<StatusResponseDTO>()

    if (body.status != SuperTokensStatus.OK.value) {
      throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
