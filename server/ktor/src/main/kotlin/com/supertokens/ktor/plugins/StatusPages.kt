package com.supertokens.ktor.plugins

import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ErrorResponse
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

fun StatusPagesConfig.superTokens(catchGeneralError: Boolean = false) {
    exception<SuperTokensStatusException> { call, cause ->
        call.respond(cause.status)
    }

    if(catchGeneralError) {
        exception<Exception> { call, cause ->
            call.respond(ErrorResponse(
                message = cause.message ?: "Unknown Error"
            ))
        }
    }
}