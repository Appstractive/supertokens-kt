package com.supertokens.ktor.utils

import com.supertokens.ktor.superTokens
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

val ApplicationCall.frontend get() = superTokens.getFrontEnd(request.headers[HttpHeaders.Origin])
val ApplicationCall.tenantId get() = parameters["tenantId"]