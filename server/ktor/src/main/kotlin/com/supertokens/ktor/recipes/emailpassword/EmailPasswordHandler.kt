package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.userHandler
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.fronend
import com.supertokens.ktor.utils.getEmailField
import com.supertokens.ktor.utils.getInvalidFormFields
import com.supertokens.ktor.utils.getNewPasswordField
import com.supertokens.ktor.utils.getPasswordField
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.ServerConfig
import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_NEW_PASSWORD_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.FormField
import com.supertokens.sdk.common.requests.FormFieldRequest
import com.supertokens.sdk.common.requests.PasswordChangeRequest
import com.supertokens.sdk.common.requests.PasswordResetRequest
import com.supertokens.sdk.common.responses.FormFieldError
import com.supertokens.sdk.common.responses.SignInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.responses.UserResponse
import com.supertokens.sdk.core.getUserByEMail
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.emailpassword.models.EmailResetTemplate
import com.supertokens.sdk.recipes.emailpassword.updatePassword
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun ApplicationCall.validateFormFields(
    fields: List<FormField>,
    success: suspend (email: String, password: String) -> Unit,
) {
    val invalidFormFields = getInvalidFormFields(fields, emailPassword.formFields)

    if (invalidFormFields.isNotEmpty()) {
        return respond(
            HttpStatusCode.BadRequest,
            SignInResponse(
                status = SuperTokensStatus.FormFieldError.value,
                formFields = invalidFormFields.map {
                    FormFieldError(
                        id = it.id,
                        error = "Invalid value: ${it.value}",
                    )
                }
            )
        )
    }

    val email = fields.getEmailField()?.value
    val password = fields.getPasswordField()?.value

    if(email == null || password == null) {
        return respond(
            HttpStatusCode.Unauthorized,
            SignInResponse(
                status = SuperTokensStatus.WrongCredentialsError.value,
            )
        )
    }

    success.invoke(email, password)
}

open class EmailPasswordHandler(
    protected val scope: CoroutineScope,
) {

    /**
     * A call to POST /signin
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailPassword%20Recipe/signIn">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.signIn() {
        val body = call.receive<FormFieldRequest>()

        val email = body.formFields.getEmailField()?.value
        val password = body.formFields.getPasswordField()?.value

        if(email == null || password == null) {
            return call.respond(
                HttpStatusCode.Unauthorized,
                SignInResponse(
                    status = SuperTokensStatus.WrongCredentialsError.value,
                )
            )
        }

        val user = emailPassword.signIn(email, password)

        with(userHandler) {
            onUserSignedIn(user)
        }

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

    /**
     * A call to POST /signup
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailPassword%20Recipe/signUp">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.signUp() {
        val body = call.receive<FormFieldRequest>()

        call.validateFormFields(body.formFields) { email, password ->
            val user = emailPassword.signUp(email, password)

            with(userHandler) {
                onUserSignedUp(user)
            }

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
        "${frontend.fullUrl}reset-password?token=$token"

    /**
     * Override this to send localized mails
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getResetPasswordTemplateName(emailService: EmailService) =
        emailService.passwordResetTemplateName

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendPasswordResetMail(email: String) {
        val frontend = call.fronend
        emailPassword.emailService?.let {
            // launch the email sending in another scope, so the call is not blocked
            scope.launch {
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

    /**
     * A call to POST /user/password/reset/token
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailPassword%20Recipe/passwordResetToken">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.passwordResetWithToken() {
        val body = call.receive<FormFieldRequest>()

        val emailField = body.formFields.getEmailField()

        emailField?.value?.let { email ->
            sendPasswordResetMail(email)
        }

        call.respond(
            StatusResponse()
        )
    }

    /**
     * A call to POST /user/password/reset
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailPassword%20Recipe/passwordReset">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.resetPassword() {
        val body = call.receive<PasswordResetRequest>()

        when (body.method) {
            "token" -> {
                val token = body.token ?: return call.respond(
                    StatusResponse(
                        status = SuperTokensStatus.ResetPasswordInvalidTokenError.value,
                    )
                )

                val password: String = body.formFields.getPasswordField()?.value
                    ?: return call.respond(
                        SignInResponse(
                            status = SuperTokensStatus.FormFieldError.value,
                            formFields = listOf(
                                FormFieldError(
                                    id = FORM_FIELD_PASSWORD_ID,
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

    open suspend fun PipelineContext<Unit, ApplicationCall>.changePassword() {
        var user = superTokens.getUserById(call.requirePrincipal<AuthenticatedUser>().id)
        val body = call.receive<PasswordChangeRequest>()

        val currentPassword: String = body.formFields.getPasswordField()?.value
            ?: return call.respond(
                SignInResponse(
                    status = SuperTokensStatus.FormFieldError.value,
                    formFields = listOf(
                        FormFieldError(
                            id = FORM_FIELD_PASSWORD_ID,
                            error = "Password missing",
                        )
                    )
                )
            )

        val newPassword: String = body.formFields.getNewPasswordField()?.value
            ?: return call.respond(
                SignInResponse(
                    status = SuperTokensStatus.FormFieldError.value,
                    formFields = listOf(
                        FormFieldError(
                            id = FORM_FIELD_PASSWORD_ID,
                            error = "New Password missing",
                        )
                    )
                )
            )

        val email = user.email ?: throw SuperTokensStatusException(SuperTokensStatus.UnknownEMailError)
        // will throw an exception if wrong
        user = emailPassword.signIn(email, currentPassword)

        val status = superTokens.updatePassword(user.id, newPassword)

        if(status != SuperTokensStatus.OK) {
            throw SuperTokensStatusException(status)
        }

        call.respond(StatusResponse())
    }

}