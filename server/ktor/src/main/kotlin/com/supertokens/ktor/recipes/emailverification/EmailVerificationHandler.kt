package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.emailpassword.emailPassword
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequest
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.responses.VerifyEmailResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

open class EmailVerificationHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.sendEmailVerification() {
        val user =  call.requirePrincipal<AuthenticatedUser>()
        val email = emailPassword.getUserById(user.id).email
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
        val email = emailPassword.getUserById(user.id).email

        val isVerified = emailVerification.verifyEmail(user.id, email)

        call.respond(VerifyEmailResponse(
            isVerified = isVerified,
        ))
    }

}