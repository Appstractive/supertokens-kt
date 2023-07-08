package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.recipes.emailverification.emailVerification
import com.supertokens.ktor.recipes.emailverification.emailVerificationEnabled
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.fronend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.ServerConfig
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequest
import com.supertokens.sdk.common.requests.ResendPasswordlessCodeRequest
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequest
import com.supertokens.sdk.common.responses.SignInUpResponse
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.passwordless.models.LoginMagicLinkOtpTemplate
import com.supertokens.sdk.recipes.passwordless.models.LoginMagicLinkTemplate
import com.supertokens.sdk.recipes.passwordless.models.LoginOtpTemplate
import com.supertokens.sdk.recipes.passwordless.models.PasswordlessCodeData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class PasswordlessHandler {

    /**
     * Override this to convert to localized duration
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.convertToTimeString(duration: Long) = "${(duration / 1000 / 60).toInt()} minutes"

    /**
     * Override this to send localized mails
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getTemplateName(emailService: EmailService) = when (passwordless.flowType) {
        PasswordlessMode.MAGIC_LINK -> emailService.magicLinkLoginTemplateName
        PasswordlessMode.USER_INPUT_CODE -> emailService.otpLoginTemplateName
        PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> emailService.magicLinkOtpLoginTemplateName
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.createMagicLinkUrl(frontend: ServerConfig, codeData: PasswordlessCodeData): String =
        "${frontend.fullUrl}/verify?preAuthSessionId=${codeData.preAuthSessionId}#${codeData.linkCode}"

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendLoginMail(email: String, codeData: PasswordlessCodeData): PasswordlessCodeData {
        val frontend = call.fronend
        passwordless.emailService?.let {
            // launch the email sending in another scope, so the call is not blocked
            CoroutineScope(Dispatchers.IO).launch {
                val appConfig = superTokens.appConfig

                val body: String = when (passwordless.flowType) {
                    PasswordlessMode.MAGIC_LINK -> {
                        it.processTemplate(
                            getTemplateName(it),
                            LoginMagicLinkTemplate(
                                appname = appConfig.name,
                                toEmail = email,
                                urlWithLinkCode = createMagicLinkUrl(frontend, codeData),
                                time = convertToTimeString(codeData.codeLifetime),
                            ),
                        )
                    }

                    PasswordlessMode.USER_INPUT_CODE -> {
                        it.processTemplate(
                            getTemplateName(it),
                            LoginOtpTemplate(
                                appname = appConfig.name,
                                toEmail = email,
                                otp = codeData.userInputCode,
                                time = convertToTimeString(codeData.codeLifetime),
                            ),
                        )
                    }

                    PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> {
                        it.processTemplate(
                            getTemplateName(it),
                            LoginMagicLinkOtpTemplate(
                                appname = appConfig.name,
                                toEmail = email,
                                urlWithLinkCode = createMagicLinkUrl(frontend, codeData),
                                otp = codeData.userInputCode,
                                time = convertToTimeString(codeData.codeLifetime),
                            ),
                        )
                    }
                }

                it.sendEmail(
                    EmailContent(
                        body = body,
                        isHtml = true,
                        subject = appConfig.name,
                        toEmail = email,
                    )
                )
            }
        }

        return codeData
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendLoginSms(phoneNumber: String, codeData: PasswordlessCodeData): PasswordlessCodeData {
        // TODO send sms
        return codeData
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.startSignInUp() {
        val body = call.receive<StartPasswordlessSignInUpRequest>()

        val codeData = body.email?.let {
            val data = passwordless.createEmailCode(it)
            sendLoginMail(it, data)
        } ?: body.phoneNumber?.let {
            val data = passwordless.createPhoneNumberCode(it)
            sendLoginSms(it, data)
        } ?: throw BadRequestException("email or phoneNumber is required")

        call.respond(
            StartPasswordlessSignInUpResponse(
                deviceId = codeData.deviceId,
                preAuthSessionId = codeData.preAuthSessionId,
                flowType = passwordless.flowType,
            )
        )
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.resendCode() {
        val body = call.receive<ResendPasswordlessCodeRequest>()
        val session = passwordless.getCodesByPreAuthSessionId(body.preAuthSessionId).firstOrNull {
            it.preAuthSessionId == body.preAuthSessionId
        } ?: throw SuperTokensStatusException(SuperTokensStatus.PasswordlessRestartFlowError)

        val data = passwordless.recreateCode(body.deviceId)
        session.email?.let {
            sendLoginMail(it, data)
        } ?: session.phoneNumber?.let {
            sendLoginSms(it, data)
        }

        call.respond(StatusResponse())
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.consumeCode() {
        val body = call.receive<ConsumePasswordlessCodeRequest>()
        val response = when (passwordless.flowType) {
            PasswordlessMode.MAGIC_LINK -> passwordless.consumeLinkCode(
                body.preAuthSessionId,
                body.linkCode ?: throw BadRequestException("linkCode is required"),
            )

            PasswordlessMode.USER_INPUT_CODE -> passwordless.consumeUserInputCode(
                preAuthSessionId = body.preAuthSessionId,
                deviceId = body.deviceId ?: throw BadRequestException("deviceId is required"),
                userInputCode = body.userInputCode ?: throw BadRequestException("userInputCode is required"),
            )

            PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> {
                val linkCode = body.linkCode
                val deviceId = body.deviceId
                val userInputCode = body.userInputCode

                if (linkCode != null) {
                    passwordless.consumeLinkCode(
                        body.preAuthSessionId,
                        linkCode,
                    )
                } else if (deviceId != null && userInputCode != null) {
                    passwordless.consumeUserInputCode(
                        preAuthSessionId = body.preAuthSessionId,
                        deviceId = deviceId,
                        userInputCode = userInputCode,
                    )
                } else {
                    throw BadRequestException("Either linkCode or deviceId and userInputCode is required")
                }
            }
        }

        if (emailVerificationEnabled) {
            val codeData = passwordless.getCodesByPreAuthSessionId(body.preAuthSessionId)
            codeData.forEach {
                it.email?.let { email ->
                    emailVerification.setVerified(response.user.id, email)
                }
            }
        }

        if (sessionsEnabled) {
            val session = sessions.createSession(
                userId = response.user.id,
                userDataInJWT = sessions.getJwtData(response.user),
            )
            setSessionInResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                antiCsrfToken = session.antiCsrfToken,
            )
        }

        call.respond(
            SignInUpResponse(
                createdNewUser = response.createdNewUser,
                user = response.user,
            )
        )
    }

}