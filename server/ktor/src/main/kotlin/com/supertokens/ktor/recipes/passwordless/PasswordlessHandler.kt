package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.recipes.emailverification.emailVerification
import com.supertokens.ktor.recipes.emailverification.isEmailVerificationEnabled
import com.supertokens.ktor.recipes.multifactor.isMultiFactorAuthEnabled
import com.supertokens.ktor.recipes.multifactor.multiFactorAuth
import com.supertokens.ktor.recipes.session.isSessionsEnabled
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.userHandler
import com.supertokens.ktor.utils.frontend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.EndpointConfig
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequestDTO
import com.supertokens.sdk.common.requests.ResendPasswordlessCodeRequestDTO
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequestDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponseDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.common.models.SignInUpData
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.recipes.passwordless.models.LoginMagicLinkOtpTemplate
import com.supertokens.sdk.recipes.passwordless.models.LoginMagicLinkTemplate
import com.supertokens.sdk.recipes.passwordless.models.LoginOtpTemplate
import com.supertokens.sdk.recipes.passwordless.models.PasswordlessCodeData
import com.supertokens.sdk.recipes.passwordless.responses.PasswordlessDevices
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class PasswordlessHandler(
    protected val scope: CoroutineScope,
) {

    /**
     * Override this to convert to localized duration
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.convertToTimeString(duration: Long) =
        "${(duration / 1000 / 60).toInt()} minutes"

    /**
     * Override this to send localized mails
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getTemplateName(emailService: EmailService) =
        when (passwordless.flowType) {
            PasswordlessMode.MAGIC_LINK -> emailService.magicLinkLoginTemplateName
            PasswordlessMode.USER_INPUT_CODE -> emailService.otpLoginTemplateName
            PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> emailService.magicLinkOtpLoginTemplateName
        }

    open suspend fun PipelineContext<Unit, ApplicationCall>.createMagicLinkUrl(
        frontend: EndpointConfig,
        codeData: PasswordlessCodeData
    ): String =
        "${frontend.fullUrl}verify?preAuthSessionId=${codeData.preAuthSessionId}#${codeData.linkCode}"

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendLoginMail(
        email: String,
        codeData: PasswordlessCodeData
    ): PasswordlessCodeData {
        val frontend = call.frontend
        passwordless.emailService?.let {
            // launch the email sending in another scope, so the call is not blocked
            scope.launch {
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

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendLoginSms(
        phoneNumber: String,
        codeData: PasswordlessCodeData
    ): PasswordlessCodeData {
        // TODO send sms
        return codeData
    }

    /**
     * A call to POST /signinup/code
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Passwordless%20Recipe/passwordlessSignInUpStart">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.startSignInUp() {
        val body = call.receive<StartPasswordlessSignInUpRequestDTO>()
        val tenantId = call.tenantId

        val codeData = body.email?.let {
            val data = passwordless.createEmailCode(it, tenantId)
            sendLoginMail(it, data)
        } ?: body.phoneNumber?.let {
            val data = passwordless.createPhoneNumberCode(it, tenantId)
            sendLoginSms(it, data)
        } ?: throw SuperTokensStatusException(SuperTokensStatus.FormFieldError)

        call.respond(
            StartPasswordlessSignInUpResponseDTO(
                deviceId = codeData.deviceId,
                preAuthSessionId = codeData.preAuthSessionId,
                flowType = passwordless.flowType,
            )
        )
    }

    /**
     * A call to POST /signinup/code/resend
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Passwordless%20Recipe/passwordlessSignInUpResend">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.resendCode() {
        val body = call.receive<ResendPasswordlessCodeRequestDTO>()
        val tenantId = call.tenantId

        val session =
            passwordless.getCodesByPreAuthSessionId(body.preAuthSessionId, tenantId).firstOrNull {
                it.preAuthSessionId == body.preAuthSessionId
            } ?: throw SuperTokensStatusException(SuperTokensStatus.PasswordlessRestartFlowError)

        val data = passwordless.recreateCode(body.deviceId, tenantId)
        session.email?.let {
            sendLoginMail(it, data)
        } ?: session.phoneNumber?.let {
            sendLoginSms(it, data)
        }

        call.respond(StatusResponseDTO())
    }

    /**
     * A call to POST /signinup/code/consume
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Passwordless%20Recipe/passwordlessSignInUpConsume">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.consumeCode() {
        val body = call.receive<ConsumePasswordlessCodeRequestDTO>()
        val tenantId = call.tenantId

        val codeData = passwordless.getCodesByPreAuthSessionId(
            preAuthSessionId = body.preAuthSessionId,
            tenantId = tenantId
        )

        if (isMultiFactorAuthEnabled && !multiFactorAuth.firstFactors.contains(RECIPE_PASSWORDLESS)) {
            consumeCodeSecondFactorFactor(
                body = body,
                tenantId = tenantId,
                codeData = codeData,
            )
        } else {
            consumeCodeFirstFactor(
                body = body,
                tenantId = tenantId,
                codeData = codeData,
            )
        }
    }

    protected open suspend fun PipelineContext<Unit, ApplicationCall>.consumeCodeFirstFactor(
        body: ConsumePasswordlessCodeRequestDTO,
        tenantId: String?,
        codeData: List<PasswordlessDevices>,
    ) {
        val response = exchangeCode(
            body = body,
            codeData = codeData,
            tenantId = tenantId,
        )

        if (isSessionsEnabled) {
            val session = sessions.createSession(
                userId = response.user.id,
                userDataInJWT = sessions.getJwtData(
                    user = response.user,
                    tenantId = tenantId,
                    recipeId = RECIPE_PASSWORDLESS,
                    multiAuthFactor = null,
                    accessToken = null,
                ),
                tenantId = tenantId,
            )
            setSessionInResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                antiCsrfToken = session.antiCsrfToken,
            )
        }

        call.respond(
            SignInUpResponseDTO(
                createdNewUser = response.createdNewUser,
                user = response.user,
            )
        )
    }

    protected open suspend fun PipelineContext<Unit, ApplicationCall>.consumeCodeSecondFactorFactor(
        body: ConsumePasswordlessCodeRequestDTO,
        tenantId: String?,
        codeData: List<PasswordlessDevices>,
    ) {
        val user =
            call.principal<AuthenticatedUser>() ?: return call.respond(HttpStatusCode.Unauthorized)

        val response = exchangeCode(
            body = body,
            codeData = codeData,
            tenantId = tenantId,
        )

        val session = sessions.verifySession(user.accessToken, checkDatabase = true)

        if (session.session.userId == user.id) {
            val isInputCode = body.userInputCode != null

            val newSession = sessions.regenerateSession(
                accessToken = user.accessToken,
                userDataInJWT = sessions.getJwtData(
                    user = superTokens.getUserById(user.id),
                    tenantId = call.tenantId,
                    recipeId = RECIPE_PASSWORDLESS,
                    multiAuthFactor = when {
                        codeData.any { it.email != null } -> if (isInputCode) {
                            AuthFactor.OTP_EMAIL
                        } else {
                            AuthFactor.LINK_EMAIL
                        }

                        codeData.any { it.phoneNumber != null } -> if (isInputCode) {
                            AuthFactor.OTP_PHONE
                        } else {
                            AuthFactor.LINK_PHONE
                        }

                        else -> null
                    },
                    accessToken = user.accessToken,
                )
            )

            setSessionInResponse(
                accessToken = newSession.accessToken,
            )
        }

        call.respond(
            SignInUpResponseDTO(
                createdNewUser = response.createdNewUser,
                user = response.user,
            )
        )
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.exchangeCode(
        body: ConsumePasswordlessCodeRequestDTO,
        tenantId: String?,
        codeData: List<PasswordlessDevices>,
    ): SignInUpData {
        val response = when (passwordless.flowType) {
            PasswordlessMode.MAGIC_LINK -> passwordless.consumeLinkCode(
                preAuthSessionId = body.preAuthSessionId,
                linkCode = body.linkCode
                    ?: throw SuperTokensStatusException(SuperTokensStatus.FormFieldError),
                tenantId = tenantId,
            )

            PasswordlessMode.USER_INPUT_CODE -> passwordless.consumeUserInputCode(
                preAuthSessionId = body.preAuthSessionId,
                deviceId = body.deviceId
                    ?: throw SuperTokensStatusException(SuperTokensStatus.FormFieldError),
                userInputCode = body.userInputCode ?: throw SuperTokensStatusException(
                    SuperTokensStatus.FormFieldError
                ),
                tenantId = tenantId,
            )

            PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> {
                val linkCode = body.linkCode
                val deviceId = body.deviceId
                val userInputCode = body.userInputCode

                if (linkCode != null) {
                    passwordless.consumeLinkCode(
                        preAuthSessionId = body.preAuthSessionId,
                        linkCode = linkCode,
                        tenantId = tenantId,
                    )
                } else if (deviceId != null && userInputCode != null) {
                    passwordless.consumeUserInputCode(
                        preAuthSessionId = body.preAuthSessionId,
                        deviceId = deviceId,
                        userInputCode = userInputCode,
                        tenantId = tenantId,
                    )
                } else {
                    throw SuperTokensStatusException(SuperTokensStatus.FormFieldError)
                }
            }
        }

        if (isEmailVerificationEnabled) {
            codeData.forEach {
                it.email?.let { email ->
                    if (response.user.loginMethods?.any { method ->
                            method.email == email && !method.verified
                        } == true) {
                        emailVerification.setVerified(
                            userId = response.user.id,
                            email = email,
                            tenantId = tenantId,
                        )
                    }
                }
            }
        }

        with(userHandler) {
            if (response.createdNewUser) {
                onUserSignedUp(response.user)
            } else {
                onUserSignedIn(response.user)
            }
        }

        return response
    }

}
