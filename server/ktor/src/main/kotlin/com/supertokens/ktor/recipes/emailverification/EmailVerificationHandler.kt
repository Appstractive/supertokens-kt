package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.accessToken
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequest
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.responses.VerifyEmailResponse
import com.supertokens.sdk.core.getUserById
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

open class EmailVerificationHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendEmailVerification() {
        val user =  call.requirePrincipal<AuthenticatedUser>()
        val email = superTokens.getUserById(user.id).email ?: throw SuperTokensStatusException(SuperTokensStatus.UnknownEMailError)
        emailVerification.createVerificationToken(user.id, email)

        // TODO send email
        call.respond(StatusResponse())
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.verifyEmail() {
        val body = call.receive<VerifyEmailTokenRequest>()

        when(body.method) {
            "token" -> {
                emailVerification.verifyToken(body.token)
                call.respond(StatusResponse())
            }
            else -> throw BadRequestException("Invalid verification meth ${body.method}")
        }

    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.checkEmailVerified() {
        val user =  call.requirePrincipal<AuthenticatedUser>()
        val email = superTokens.getUserById(user.id).email
        val isVerified = emailVerification.isVerified(user.id, email)

        if(sessionsEnabled && isVerified) {
            val session = sessions.regenerateSession(
                accessToken = call.accessToken,
                userDataInJWT = sessions.getJwtData(superTokens.getUserById(user.id)),
            )
            setSessionInResponse(
                accessToken = session.accessToken,
            )
        }

        call.respond(VerifyEmailResponse(
            isVerified = isVerified,
        ))
    }

}