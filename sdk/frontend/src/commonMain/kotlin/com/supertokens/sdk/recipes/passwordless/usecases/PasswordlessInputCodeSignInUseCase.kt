package com.supertokens.sdk.recipes.passwordless.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequestDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.models.SignInData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendEncodedPathSegments

class PasswordlessInputCodeSignInUseCase(
    private val client: HttpClient,
    private val tenantId: String?,
) {

    suspend fun signIn(preAuthSessionId: String, deviceId: String, userInputCode: String): SignInData {
        val response = client.post {
            url {
                appendEncodedPathSegments(
                    listOfNotNull(
                        tenantId,
                        Routes.Passwordless.SIGNUP_CODE_CONSUME,
                    )
                )
            }
            setBody(
                ConsumePasswordlessCodeRequestDTO(
                    preAuthSessionId = preAuthSessionId,
                    deviceId = deviceId,
                    userInputCode = userInputCode,
                )
            )
        }

        val body = response.body<SignInUpResponseDTO>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                SignInData(
                    user = checkNotNull(body.user),
                    createdNewUser = checkNotNull(body.createdNewUser),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

}