package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.getEmailFormField
import com.supertokens.ktor.utils.getInvalidFormFields
import com.supertokens.ktor.utils.getPasswordFormField
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.FormField
import com.supertokens.sdk.common.requests.FormFieldRequest
import com.supertokens.sdk.common.requests.PasswordResetRequest
import com.supertokens.sdk.common.responses.EmailExistsResponse
import com.supertokens.sdk.common.responses.FormFieldError
import com.supertokens.sdk.common.responses.SignInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.responses.UserResponse
import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

private suspend fun ApplicationCall.validateFormFields(
    fields: List<FormField>,
    success: suspend (email: String, password: String) -> Unit,
) {
    val invalidFormFields = getInvalidFormFields(fields, emailPassword.formFields)

    if (invalidFormFields.isNotEmpty()) {
        return respond(
            SignInResponse(
                status = SuperTokensStatus.FormFieldError.value,
                formFields = invalidFormFields.map {
                    FormFieldError(
                        id = it.id,
                        error = "FormField '${it.id}' has invalid value: ${it.value}",
                    )
                }
            )
        )
    }

    val email: String = getEmailFormField(fields)?.value
        ?: return respond(
            SignInResponse(
                status = SuperTokensStatus.WrongCredentialsError.value,
            )
        )
    val password: String = getPasswordFormField(fields)?.value
        ?: return respond(
            SignInResponse(
                status = SuperTokensStatus.WrongCredentialsError.value,
            )
        )

    success.invoke(email, password)
}

open class EmailPasswordHandler {

    private suspend fun SuperTokens.getJwtData(user: User): Map<String, Any?> = buildMap {
        set("iss", appConfig.apiDomain)
        set("aud", appConfig.websiteDomain)
        getRecipe<SessionRecipe>().customJwtData?.let {
            it.invoke(this@getJwtData, user).forEach { entry ->
                set(entry.key, entry.value)
            }
        }
    }

    open suspend fun signIn(call: ApplicationCall) {
        val body = call.receive<FormFieldRequest>()

        call.validateFormFields(body.formFields) { email, password ->
            val user = call.emailPassword.signIn(email, password)

            if(call.sessionsEnabled) {
                val session = call.sessions.createSession(
                    userId = user.id,
                    userDataInJWT = call.superTokens.getJwtData(user),
                )

                call.setSessionInResponse(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    antiCsrfToken = session.antiCsrfToken,
                )
            }

            call.respond(
                SignInResponse(
                    user = UserResponse(
                        id = user.id,
                        email = user.email,
                        timeJoined = user.timeJoined,
                    )
                )
            )
        }
    }

    open suspend fun signUp(call: ApplicationCall) {
        val body = call.receive<FormFieldRequest>()

        call.validateFormFields(body.formFields) { email, password ->
            val user = call.emailPassword.signUp(email, password)

            if(call.sessionsEnabled) {
                val session = call.sessions.createSession(
                    userId = user.id,
                    userDataInJWT = call.superTokens.getJwtData(user),
                )

                call.setSessionInResponse(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    antiCsrfToken = session.antiCsrfToken,
                )
            }

            // TODO send verify email

            call.respond(
                SignInResponse(
                    user = UserResponse(
                        id = user.id,
                        email = user.email,
                        timeJoined = user.timeJoined,
                    )
                )
            )
        }
    }

    open suspend fun emailExists(call: ApplicationCall) {
        val email = call.parameters["email"] ?: return call.respond(HttpStatusCode.NotFound)

        val response = runCatching {
            call.emailPassword.getUserByEMail(email)
        }

        call.respond(
            EmailExistsResponse(
                exists = response.isSuccess,
            )
        )
    }

    open suspend fun passwordResetToken(call: ApplicationCall) {
        val body = call.receive<FormFieldRequest>()

        val email = body.formFields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_EMAIL_ID }?.value

        email?.let {
            val result = runCatching {
                val user = call.emailPassword.getUserByEMail(it)
                val token = call.emailPassword.createResetPasswordToken(user.id)

                // TODO send email
            }
        }

        call.respond(
            StatusResponse()
        )
    }

    open suspend fun resetPassword(call: ApplicationCall) {
        val body = call.receive<PasswordResetRequest>()

        when(body.method) {
            "token" -> {
                val token = body.token ?: return call.respond(StatusResponse(
                    status = SuperTokensStatus.ResetPasswordInvalidTokenError.value,
                ))

                val password: String = body.formFields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_PASSWORD_ID }?.value
                    ?: return call.respond(
                        SignInResponse(
                            status = SuperTokensStatus.FormFieldError.value,
                            formFields = listOf(
                                FormFieldError(
                                    id = EmailPasswordRecipe.FORM_FIELD_PASSWORD_ID,
                                    error = "Password missing",
                                )
                            )
                        )
                    )

                call.emailPassword.resetPasswordWithToken(token, password)

                call.respond(StatusResponse())
            }
            else -> call.respond(HttpStatusCode.BadRequest)
        }
    }

}