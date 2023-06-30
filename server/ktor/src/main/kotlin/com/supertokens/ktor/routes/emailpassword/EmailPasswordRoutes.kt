package com.supertokens.ktor.routes.emailpassword

import com.supertokens.ktor.routes.session.sessions
import com.supertokens.ktor.routes.session.sessionsEnabled
import com.supertokens.ktor.utils.addSessionToResponse
import com.supertokens.sdk.common.requests.FormFieldRequest
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.UserResponse
import com.supertokens.sdk.common.requests.FormField
import com.supertokens.sdk.common.requests.PasswordResetRequest
import com.supertokens.sdk.common.responses.EmailExistsResponse
import com.supertokens.sdk.common.responses.FormFieldError
import com.supertokens.sdk.common.responses.SignInResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext

private suspend fun PipelineContext<Unit, ApplicationCall>.validateFormFields(
    fields: List<FormField>,
    success: suspend (email: String, password: String) -> Unit,
) {
    val invalidFormFields = mutableListOf<FormField>()

    fields.forEach { field ->
        if (emailPassword.formFields.firstOrNull { it.id == field.id }?.validate?.invoke(field.value) == false) {
            invalidFormFields.add(field)
        }
    }

    if (invalidFormFields.isNotEmpty()) {
        return call.respond(
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

    val email: String = fields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_EMAIL_ID }?.value
        ?: return call.respond(
            SignInResponse(
                status = SuperTokensStatus.WrongCredentialsError.value,
            )
        )
    val password: String = fields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_PASSWORD_ID }?.value
        ?: return call.respond(
            SignInResponse(
                status = SuperTokensStatus.WrongCredentialsError.value,
            )
        )

    success.invoke(email, password)
}

fun Route.emailPasswordRoutes(
    headerBasedSessions: Boolean = true,
    cookieBasedSessions: Boolean = true,
) {

    post("/signin") {
        val body = call.receive<FormFieldRequest>()

        validateFormFields(body.formFields) { email, password ->
            val user = emailPassword.signIn(email, password)

            if(sessionsEnabled) {
                val session = sessions.createSession(user.id)

                addSessionToResponse(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    antiCsrfToken = session.antiCsrfToken,
                    addToHeaders = headerBasedSessions,
                    addToCookies = cookieBasedSessions,
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

    post("/signup") {
        val body = call.receive<FormFieldRequest>()

        validateFormFields(body.formFields) { email, password ->
            val user = emailPassword.signUp(email, password)

            if(sessionsEnabled) {
                val session = sessions.createSession(user.id)

                addSessionToResponse(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    antiCsrfToken = session.antiCsrfToken,
                    addToHeaders = headerBasedSessions,
                    addToCookies = cookieBasedSessions,
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

    get("/signup/email/exists") {
        val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.NotFound)

        val response = runCatching {
            emailPassword.getUserByEMail(email)
        }

        call.respond(
            EmailExistsResponse(
                exists = response.isSuccess,
            )
        )
    }

    post("/user/password/reset/token") {
        val body = call.receive<FormFieldRequest>()

        val email = body.formFields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_EMAIL_ID }?.value

        email?.let {
            val result = runCatching {
                val user = emailPassword.getUserByEMail(it)
                val token = emailPassword.createResetPasswordToken(user.id)

                // TODO send email
            }
        }

        call.respond(
            StatusResponse()
        )
    }

    post("/user/password/reset") {
        val body = call.receive<PasswordResetRequest>()

        when(body.method) {
            "token" -> {
                val token = body.token ?: return@post call.respond(StatusResponse(
                    status = SuperTokensStatus.ResetPasswordInvalidTokenError.value,
                ))

                val password: String = body.formFields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_PASSWORD_ID }?.value
                    ?: return@post call.respond(
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