package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.fronend
import com.supertokens.ktor.utils.getEmailFormField
import com.supertokens.ktor.utils.getInvalidFormFields
import com.supertokens.ktor.utils.getPasswordFormField
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.ServerConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.FormField
import com.supertokens.sdk.common.requests.FormFieldRequest
import com.supertokens.sdk.common.requests.PasswordResetRequest
import com.supertokens.sdk.common.responses.FormFieldError
import com.supertokens.sdk.common.responses.SignInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.responses.UserResponse
import com.supertokens.sdk.core.getUserByEMail
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe.Companion.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe.Companion.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.recipes.emailpassword.models.EmailResetTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    open suspend fun PipelineContext<Unit, ApplicationCall>.signIn() {
        val body = call.receive<FormFieldRequest>()

        call.validateFormFields(body.formFields) { email, password ->
            val user = emailPassword.signIn(email, password)

            if (sessionsEnabled) {
                val session = sessions.createSession(
                    userId = user.id,
                    userDataInJWT = sessions.getJwtData(user),
                )

                setSessionInResponse(
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

    open suspend fun PipelineContext<Unit, ApplicationCall>.signUp() {
        val body = call.receive<FormFieldRequest>()

        call.validateFormFields(body.formFields) { email, password ->
            val user = emailPassword.signUp(email, password)

            if (sessionsEnabled) {
                val additionalFormField = body.formFields.filter {
                    it.id != FORM_FIELD_EMAIL_ID && it.id != FORM_FIELD_PASSWORD_ID
                }

                val session = sessions.createSession(
                    userId = user.id,
                    userDataInJWT = sessions.getJwtData(user),
                    userDataInDatabase = buildMap {
                        additionalFormField.forEach {
                            set(it.id, it.value)
                        }
                    },
                )

                setSessionInResponse(
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

    open suspend fun createPasswordResetLink(frontend: ServerConfig, token: String) =
        "${frontend.fullUrl}/reset-password?token=$token"

    /**
     * Override this to send localized mails
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getResetPasswordTemplateName(emailService: EmailService) =
        emailService.passwordResetTemplateName

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendPasswordResetMail(email: String) {
        val frontend = call.fronend
        emailPassword.emailService?.let {
            // launch the email sending in another scope, so the call is not blocked
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val user = superTokens.getUserByEMail(email)
                    val token = emailPassword.createResetPasswordToken(user.id)

                    val body = it.processTemplate(
                        getResetPasswordTemplateName(it),
                        EmailResetTemplate(
                            appname = superTokens.appConfig.name,
                            toEmail = email,
                            resetLink = createPasswordResetLink(frontend, token),
                        ),
                    )

                    it.sendEmail(
                        EmailContent(
                            body = body,
                            isHtml = true,
                            subject = superTokens.appConfig.name,
                            toEmail = email,
                        )
                    )
                }
            }
        }
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.passwordResetToken() {
        val body = call.receive<FormFieldRequest>()

        val emailField = body.formFields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_EMAIL_ID }

        emailField?.value?.let { email ->
            sendPasswordResetMail(email)
        }

        call.respond(
            StatusResponse()
        )
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.resetPassword() {
        val body = call.receive<PasswordResetRequest>()

        when (body.method) {
            "token" -> {
                val token = body.token ?: return call.respond(
                    StatusResponse(
                        status = SuperTokensStatus.ResetPasswordInvalidTokenError.value,
                    )
                )

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

                emailPassword.resetPasswordWithToken(token, password)

                call.respond(StatusResponse())
            }

            else -> call.respond(HttpStatusCode.BadRequest)
        }
    }

}