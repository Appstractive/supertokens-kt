package com.supertokens.ktor.plugins

import com.supertokens.ktor.utils.ResponseException
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.ErrorResponse
import com.supertokens.sdk.common.responses.StatusResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

fun StatusPagesConfig.superTokens(catchGeneralError: Boolean = false) {
    exception<SuperTokensStatusException> { call, cause ->
        call.respond(
            status = HttpStatusCode.BadRequest,
            message = cause.message?.let {
                ErrorResponse(status = cause.status.value, message = it)
            } ?: StatusResponse(cause.status.value)
        )
    }

    exception<ResponseException> { call, cause ->
        call.respond(
            status = cause.status,
            message = ErrorResponse(status = SuperTokensStatus.UnknownError.value, message = cause.message)
        )
    }

    if (catchGeneralError) {
        exception<Exception> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    message = cause.message ?: "Unknown Error"
                )
            )
        }
    }
}