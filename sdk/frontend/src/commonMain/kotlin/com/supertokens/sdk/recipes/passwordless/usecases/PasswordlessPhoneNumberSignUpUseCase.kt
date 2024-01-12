package com.supertokens.sdk.recipes.passwordless.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequestDTO
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.passwordless.PasswordlessSignUpData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class PasswordlessPhoneNumberSignUpUseCase(
    private val client: HttpClient,
) {

    suspend fun signUp(phoneNumber: String): PasswordlessSignUpData {
        val response = client.post(Routes.Passwordless.SIGNUP_CODE) {
            setBody(
                StartPasswordlessSignInUpRequestDTO(
                    phoneNumber = phoneNumber,
                )
            )
        }

        val body = response.body<StartPasswordlessSignInUpResponseDTO>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                PasswordlessSignUpData(
                    deviceId = checkNotNull(body.deviceId),
                    preAuthSessionId = checkNotNull(body.preAuthSessionId),
                    flowType = checkNotNull(body.flowType),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

}