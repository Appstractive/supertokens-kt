package com.supertokens.sdk.recipes.emailverification.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.responses.VerifyEmailResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CheckEmailVerifiedUseCase(
    private val client: HttpClient,
) {

  suspend fun checkEmailVerified(): Boolean {
    val response = client.get(Routes.EmailVerification.CHECK_VERIFIED)

    val body = response.body<VerifyEmailResponseDTO>()

    return body.isVerified == true
  }
}
