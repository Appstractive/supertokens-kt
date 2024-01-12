package com.supertokens.sdk.recipes.emailverification.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

class SendEmailVerificationUseCase(
    private val client: HttpClient,
) {

    suspend fun sendVerificationEmail(): Boolean {
        val response = client.post(Routes.EmailVerification.VERIFY_TOKEN)

        val body = response.body<StatusResponseDTO>()

        return body.status.toStatus() == SuperTokensStatus.OK
    }

}