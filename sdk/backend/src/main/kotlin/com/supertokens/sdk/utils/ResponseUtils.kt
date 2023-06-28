package com.supertokens.sdk.utils

import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.recipes.common.BaseResponse
import com.supertokens.sdk.recipes.common.StatusResponse
import com.supertokens.sdk.toStatus
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import it.czerwinski.kotlin.util.Either
import it.czerwinski.kotlin.util.Left
import it.czerwinski.kotlin.util.Right

suspend inline fun <reified R: BaseResponse, T> HttpResponse.parse(convert: R.() -> T): Either<SuperTokensStatus, T> {
    if (status != HttpStatusCode.OK) {
        return Left(bodyAsText().toStatus())
    }

    val body = body<R>()

    return when (val status = body.status.toStatus()) {
        SuperTokensStatus.OK -> Right(
            body.convert()
        )

        else -> Left(status)
    }
}

suspend fun HttpResponse.parse(): SuperTokensStatus {
    if(status != HttpStatusCode.OK) {
        return bodyAsText().toStatus()
    }

    val body = body<StatusResponse>()

    return body.status.toStatus()
}