package com.supertokens.ktor.utils

import io.ktor.http.*

open class ResponseException(
    val status: HttpStatusCode,
    cause: Throwable? = null,
): java.lang.RuntimeException(cause) {

    override val message: String
        get() = "Error ${status.value}: ${cause?.message ?: status.description}"
}

class UnauthorizedException(
    cause: Throwable? = null,
): ResponseException(HttpStatusCode.Unauthorized, cause)