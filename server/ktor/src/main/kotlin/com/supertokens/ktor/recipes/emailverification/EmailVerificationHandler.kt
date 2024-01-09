package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.accessToken
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.frontend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.ServerConfig
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.responses.VerifyEmailResponseDTO
import com.supertokens.sdk.core.getUserByEMailOrNull
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.emailverification.models.EmailVerificationTemplate
import com.supertokens.sdk.recipes.session.getSessions
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class EmailVerificationHandler(
    protected val scope: CoroutineScope,
) {

    open suspend fun PipelineContext<Unit, ApplicationCall>.createVerificationLink(frontend: ServerConfig, token: String) =
        "${frontend.fullUrl}verify-email?token=$token"

    /**
     * Override this to send localized mails
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getResetPasswordTemplateName(emailService: EmailService) =
        emailService.emailVerificationTemplateName

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendVerificationMail(email: String) {
        val frontend = call.frontend

        emailVerification.emailService?.let {
            // launch the email sending in another scope, so the call is not blocked
            scope.launch {
                runCatching {
                    val user = superTokens.getUserByEMailOrNull(email) ?: return@launch
                    val token = emailVerification.createVerificationToken(
                        userId = user.id,
                        email = email,
                        tenantId = call.tenantId,
                    )

                    val body = it.processTemplate(
                        getResetPasswordTemplateName(it),
                        EmailVerificationTemplate(
                            appName = superTokens.appConfig.name,
                            email = email,
                            verificationLink = createVerificationLink(frontend, token),
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
     * A call to POST /user/email/verify/token
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/verifyEmailToken">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.sendEmailVerification() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val email = superTokens.getUserById(user.id).email ?: throw SuperTokensStatusException(SuperTokensStatus.UnknownEMailError)

        sendVerificationMail(email)

        call.respond(StatusResponseDTO())
    }

    /**
     * A call to POST /user/email/verify
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/verifyEmail">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.verifyEmail() {
        val body = call.receive<VerifyEmailTokenRequestDTO>()
        val tenantId = call.tenantId

        when (body.method) {
            "token" -> {
                val data = emailVerification.verifyToken(body.token)

                // update email verification state in existing sessions
                val jwtData = sessions.getJwtData(
                    user = superTokens.getUserById(data.userId),
                    tenantId = tenantId
                )

                val userSessions = superTokens.getSessions(data.userId)
                userSessions.forEach {
                    sessions.updateJwtData(
                        sessionHandle = it,
                        userDataInJWT = jwtData,
                        tenantId = tenantId
                    )
                }

                // update token if present and from same user
                runCatching {
                    call.request.header(HEADER_ACCESS_TOKEN)?.let { token ->
                        val session = sessions.verifySession(token, checkDatabase = true)

                        if(session.session.userId == data.userId) {

                            val newSession = sessions.regenerateSession(token, jwtData)

                            setSessionInResponse(
                                accessToken = newSession.accessToken,
                            )
                        }
                    }
                }

                call.respond(StatusResponseDTO())
            }

            else -> throw BadRequestException("Invalid verification method ${body.method}")
        }

    }

    /**
     * A call to GET /user/email/verify
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/getVerifyEmail">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.checkEmailVerified() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val email = superTokens.getUserById(user.id).email
        val isVerified = emailVerification.isVerified(user.id, email)

        if (sessionsEnabled && isVerified) {
            val session = sessions.regenerateSession(
                accessToken = call.accessToken,
                userDataInJWT = sessions.getJwtData(
                    user = superTokens.getUserById(user.id),
                    tenantId = call.tenantId
                ),
            )
            setSessionInResponse(
                accessToken = session.accessToken,
            )
        }

        call.respond(
            VerifyEmailResponseDTO(
                isVerified = isVerified,
            )
        )
    }

}