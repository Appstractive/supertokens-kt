package com.supertokens.sdk.recipes.emailpassword.usecases

import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.requests.FormFieldDTO
import com.supertokens.sdk.common.requests.FormFieldRequestDTO
import com.supertokens.sdk.common.responses.SignInResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.handlers.FormFieldException
import com.supertokens.sdk.recipes.sessions.repositories.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendEncodedPathSegments

class EmailPasswordSignUpUseCase(
    private val client: HttpClient,
    private val authRepository: AuthRepository,
    private val tenantId: String?,
) {

  suspend fun signUp(email: String, password: String): User {
    val response =
        client.post {
          url {
            appendEncodedPathSegments(
                listOfNotNull(
                    tenantId,
                    Routes.EmailPassword.SIGN_UP,
                ))
          }
          setBody(
              FormFieldRequestDTO(
                  formFields =
                      listOf(
                          FormFieldDTO(
                              id = FORM_FIELD_EMAIL_ID,
                              value = email,
                          ),
                          FormFieldDTO(
                              id = FORM_FIELD_PASSWORD_ID,
                              value = password,
                          ),
                      ),
              ))
        }

    val body = response.body<SignInResponseDTO>()

    return when (body.status) {
      SuperTokensStatus.OK.value -> checkNotNull(body.user)
      SuperTokensStatus.FormFieldError.value ->
          throw FormFieldException(errors = checkNotNull(body.formFields))

      else -> throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
