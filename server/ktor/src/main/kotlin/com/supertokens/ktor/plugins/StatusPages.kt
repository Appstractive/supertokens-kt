package com.supertokens.ktor.plugins

import com.supertokens.ktor.utils.ResponseException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.StatusResponseDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

fun StatusPagesConfig.superTokens(catchGeneralError: Boolean = false) {
  exception<SuperTokensStatusException> { call, cause ->
    call.respond(
        status = HttpStatusCode.BadRequest,
        message = StatusResponseDTO(status = cause.status.value, message = cause.message))
  }

  exception<ResponseException> { call, cause ->
    call.respond(
        status = cause.status,
        message =
            StatusResponseDTO(
                status = SuperTokensStatus.UnknownError.value, message = cause.message))
  }

  if (catchGeneralError) {
    exception<Exception> { call, cause ->
      call.respond(
          status = HttpStatusCode.InternalServerError,
          message =
              StatusResponseDTO(
                  status = SuperTokensStatus.UnknownError.value,
                  message = cause.message ?: "Unknown Error"))
    }
  }
}
