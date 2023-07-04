package com.supertokens.ktor

import com.supertokens.sdk.common.responses.ExistsResponse
import com.supertokens.sdk.core.getUserByEMail
import com.supertokens.sdk.core.getUserByPhoneNumber
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

open class CoreHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.emailExists() {
        val email = call.parameters["email"] ?: return call.respond(HttpStatusCode.NotFound)

        val response = runCatching {
            call.superTokens.getUserByEMail(email)
        }

        call.respond(
            ExistsResponse(
                exists = response.isSuccess,
            )
        )
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.phoneNumberExists() {
        val phoneNumber = call.parameters["phoneNumber"] ?: return call.respond(HttpStatusCode.NotFound)

        val response = runCatching {
            call.superTokens.getUserByPhoneNumber(phoneNumber)
        }

        call.respond(
            ExistsResponse(
                exists = response.isSuccess,
            )
        )
    }

}