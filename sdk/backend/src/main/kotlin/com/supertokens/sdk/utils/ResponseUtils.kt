package com.supertokens.sdk.utils

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.recipes.common.UserResponse
import com.supertokens.sdk.common.toStatus
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

suspend inline fun <reified R: BaseResponse, T> HttpResponse.parse(convert: R.() -> T): T {
    if (status != HttpStatusCode.OK) {
        throw SuperTokensStatusException(bodyAsText().toStatus())
    }

    val body = body<R>()

    return when (val status = body.status.toStatus()) {
        SuperTokensStatus.OK -> body.convert()
        else -> throw SuperTokensStatusException(status)
    }
}

suspend fun HttpResponse.parse(): SuperTokensStatus {
    if(status != HttpStatusCode.OK) {
        return bodyAsText().toStatus()
    }

    val body = body<StatusResponse>()

    return body.status.toStatus()
}

suspend fun HttpResponse.parseUser(): User {
    if(status != HttpStatusCode.OK) {
        throw SuperTokensStatusException(bodyAsText().toStatus())
    }

    val body = body<UserResponse>()

    return body.user ?: throw SuperTokensStatusException(body.status.toStatus())
}