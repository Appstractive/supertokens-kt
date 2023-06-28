package com.supertokens.sdk.recipes.common

import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.toStatus
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
)

suspend fun HttpResponse.parseStatusResponse(): SuperTokensStatus {
    if(status != HttpStatusCode.OK) {
        return SuperTokensStatus.UnknownError(bodyAsText())
    }

    val body = body<UserResponse>()

    return body.status.toStatus()
}