package com.supertokens.sdk.recipes.totp.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.VerifyTotpRequestDTO
import com.supertokens.sdk.common.responses.VerifyTotpResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

data class InvalidTotpException(
    val currentNumberOfFailedAttempts: Long,
    val maxNumberOfFailedAttempts: Long,
): SuperTokensStatusException(SuperTokensStatus.InvalidTotpCodeError)

data class InvalidTotpLimitException(
    val retryAfterMs: Long,
): SuperTokensStatusException(SuperTokensStatus.InvalidTotpCodeError)

class VerifyTotpUseCase(
    private val client: HttpClient,
) {

    suspend fun verifyCode(totp: String): Boolean {
        val response = client.post(Routes.Totp.VERIFY) {
            setBody(
                VerifyTotpRequestDTO(
                    totp = totp,
                )
            )
        }

        val body = response.body<VerifyTotpResponseDTO>()

        return when(body.status.toStatus()) {
            SuperTokensStatus.OK -> true
            SuperTokensStatus.InvalidTotpCodeError -> throw InvalidTotpException(
                currentNumberOfFailedAttempts = checkNotNull(body.currentNumberOfFailedAttempts),
                maxNumberOfFailedAttempts = checkNotNull(body.maxNumberOfFailedAttempts),
            )
            SuperTokensStatus.TotpLimitReachedError -> throw InvalidTotpLimitException(
                retryAfterMs = checkNotNull(body.retryAfterMs),
            )
            else -> false
        }
    }

}