package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequest
import com.supertokens.sdk.common.requests.ResendPasswordlessCodeRequest
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequest
import com.supertokens.sdk.common.responses.SignInUpResponse
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

open class PasswordlessHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.startSignInUp() {
        val body = call.receive<StartPasswordlessSignInUpRequest>()

        val codeData = body.email?.let {
            passwordless.createEmailCode(it)
        } ?: body.phoneNumber?.let {
            passwordless.createPhoneNumberCode(it)
        } ?: throw BadRequestException("email or phoneNumber is required")

        // TODO send email/sms

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
        val codeData = passwordless.getCodesByPreAuthSessionId(body.preAuthSessionId)

        if(codeData.isEmpty()) {
            throw SuperTokensStatusException(SuperTokensStatus.PasswordlessRestartFlowError)
        }

        passwordless.recreateCode(body.deviceId)

        // TODO send email/sms

        call.respond(StatusResponse())
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.consumeCode() {
        val body = call.receive<ConsumePasswordlessCodeRequest>()
        val response = when(passwordless.flowType) {
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

                if(linkCode != null) {
                    passwordless.consumeLinkCode(
                        body.preAuthSessionId,
                        linkCode,
                    )
                }
                else if(deviceId != null && userInputCode != null) {
                    passwordless.consumeUserInputCode(
                        preAuthSessionId = body.preAuthSessionId,
                        deviceId = deviceId,
                        userInputCode = userInputCode,
                    )
                }
                else {
                    throw BadRequestException("Either linkCode or deviceId and userInputCode is required")
                }
            }
        }

        if(sessionsEnabled) {
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

        call.respond(SignInUpResponse(
            createdNewUser = response.createdNewUser,
            user = response.user,
        ))
    }

}