package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.SuperTokensClient
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
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.handlers.SignUpProvider
import com.supertokens.sdk.handlers.SignUpProviderConfig
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

object EmailPassword : SignInProvider<EmailPassword.Config, User>, SignUpProvider<EmailPassword.Config, User> {

    data class Config(var email: String = "", var password: String = "") : SignInProviderConfig, SignUpProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: Config.() -> Unit): User {
        val config = Config().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.EmailPassword.SIGN_IN) {
            setBody(
                FormFieldRequestDTO(
                    formFields = listOf(
                        FormFieldDTO(
                            id = FORM_FIELD_EMAIL_ID,
                            value = config.email,
                        ),
                        FormFieldDTO(
                            id = FORM_FIELD_PASSWORD_ID,
                            value = config.password,
                        ),
                    ),
                )
            )
        }

        val body = response.body<SignInResponseDTO>()

        return when (body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.user)
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

    override suspend fun signUp(superTokensClient: SuperTokensClient, configure: Config.() -> Unit): User {
        val config = Config().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.EmailPassword.SIGN_UP) {
            setBody(
                FormFieldRequestDTO(
                    formFields = listOf(
                        FormFieldDTO(
                            id = FORM_FIELD_EMAIL_ID,
                            value = config.email,
                        ),
                        FormFieldDTO(
                            id = FORM_FIELD_PASSWORD_ID,
                            value = config.password,
                        ),
                    ),
                )
            )
        }

        val body = response.body<SignInResponseDTO>()

        return when (body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.user)
            SuperTokensStatus.FormFieldError.value -> throw FormFieldException(
                errors = checkNotNull(body.formFields)
            )

            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

}