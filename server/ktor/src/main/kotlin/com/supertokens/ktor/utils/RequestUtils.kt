package com.supertokens.ktor.utils

import com.supertokens.ktor.superTokens
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

fun ApplicationCall.getFrontEnd() = superTokens.getFrontEnd(request.headers[HttpHeaders.Origin])