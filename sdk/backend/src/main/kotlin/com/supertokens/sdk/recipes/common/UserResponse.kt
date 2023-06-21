package com.supertokens.sdk.recipes.common

import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.models.User
import com.supertokens.sdk.toStatus
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import it.czerwinski.kotlin.util.Either
import it.czerwinski.kotlin.util.Left
import it.czerwinski.kotlin.util.Right
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val status: String,
    val user: User? = null,
)


suspend fun HttpResponse.parseUserResponse(): Either<SuperTokensStatus, User> {
    if(status != HttpStatusCode.OK) {
        return Left(SuperTokensStatus.UnknownError(bodyAsText()))
    }

    val body = body<UserResponse>()

    return body.user?.let {
        Right(it)
    } ?: Left(body.status.toStatus())
}