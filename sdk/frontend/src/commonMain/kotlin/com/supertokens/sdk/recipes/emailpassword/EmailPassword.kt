package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.requests.FormField
import com.supertokens.sdk.common.requests.FormFieldRequest
import com.supertokens.sdk.common.responses.SignInResponse
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
                FormFieldRequest(
                    formFields = listOf(
                        FormField(
                            id = FORM_FIELD_EMAIL_ID,
                            value = config.email,
                        ),
                        FormField(
                            id = FORM_FIELD_PASSWORD_ID,
                            value = config.password,
                        ),
                    ),
                )
            )
        }

        val body = response.body<SignInResponse>()

        return when(body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.user).let {
                User(
                    id = it.id,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    timeJoined = it.timeJoined,
                )
            }
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

    override suspend fun signUp(superTokensClient: SuperTokensClient, configure: Config.() -> Unit): User {
        val config = Config().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.EmailPassword.SIGN_UP) {
            setBody(
                FormFieldRequest(
                    formFields = listOf(
                        FormField(
                            id = FORM_FIELD_EMAIL_ID,
                            value = config.email,
                        ),
                        FormField(
                            id = FORM_FIELD_PASSWORD_ID,
                            value = config.password,
                        ),
                    ),
                )
            )
        }

        val body = response.body<SignInResponse>()

        return when(body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.user).let {
                User(
                    id = it.id,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    timeJoined = it.timeJoined,
                )
            }
            SuperTokensStatus.FormFieldError.value -> throw FormFieldException(
                errors = checkNotNull(body.formFields)
            )
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

}