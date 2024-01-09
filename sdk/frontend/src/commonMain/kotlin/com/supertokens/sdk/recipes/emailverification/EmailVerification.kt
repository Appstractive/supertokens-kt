package com.supertokens.sdk.recipes.emailverification

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.responses.VerifyEmailResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun SuperTokensClient.sendVerificationEmail(): SuperTokensStatus {
    val response = apiClient.post(Routes.EmailVerification.VERIFY_TOKEN)

    val body = response.body<StatusResponseDTO>()

    return body.status.toStatus()
}

suspend fun SuperTokensClient.verifyEmail(token: String): SuperTokensStatus {
    val response = apiClient.post(Routes.EmailVerification.VERIFY) {
        setBody(
            VerifyEmailTokenRequestDTO(
                token = token,
            )
        )
    }

    val body = response.body<StatusResponseDTO>()

    return body.status.toStatus()
}

suspend fun SuperTokensClient.checkEmailVerified(): Boolean {
    val response = apiClient.get(Routes.EmailVerification.CHECK_VERIFIED)

    val body = response.body<VerifyEmailResponseDTO>()

    return body.isVerified == true
}