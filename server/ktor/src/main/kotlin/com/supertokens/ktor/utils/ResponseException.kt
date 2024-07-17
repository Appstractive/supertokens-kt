package com.supertokens.ktor.utils

import io.ktor.http.*

open class ResponseException(
    val status: HttpStatusCode,
    val error: String? = null,
    cause: Throwable? = null,
) : java.lang.RuntimeException(cause) {

  override val message: String
    get() = "Error ${status.value}: ${error ?: cause?.message ?: status.description}"
}

class BadRequestException(
    message: String? = null,
    cause: Throwable? = null,
) : ResponseException(status = HttpStatusCode.BadRequest, error = message, cause = cause)

class UnauthorizedException(
    cause: Throwable? = null,
) : ResponseException(status = HttpStatusCode.Unauthorized, cause = cause)

class NotFoundException(
    cause: Throwable? = null,
) : ResponseException(status = HttpStatusCode.NotFound, cause = cause)
