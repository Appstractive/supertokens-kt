package com.supertokens.ktor.plugins

import com.supertokens.ktor.utils.ResponseException
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ErrorResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

fun StatusPagesConfig.superTokens(catchGeneralError: Boolean = false) {
    exception<SuperTokensStatusException> { call, cause ->
        call.respond(StatusResponse(cause.status.value))
    }

    exception<ResponseException> { call, cause ->
        call.respondText(status = cause.status, text = cause.message)
    }

    if (catchGeneralError) {
        exception<Exception> { call, cause ->
            call.respond(
                message = ErrorResponse(
                    message = cause.message ?: "Unknown Error"
                )
            )
        }
    }
}