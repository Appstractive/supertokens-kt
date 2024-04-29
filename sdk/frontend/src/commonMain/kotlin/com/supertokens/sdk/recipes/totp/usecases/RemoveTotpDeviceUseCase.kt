package com.supertokens.sdk.recipes.totp.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.TotpDeviceRequestDTO
import com.supertokens.sdk.common.responses.RemoveTotpDeviceResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RemoveTotpDeviceUseCase(
    private val client: HttpClient,
) {

    suspend fun removeDevice(name: String): Boolean {
        val response = client.post(Routes.Totp.REMOVE_DEVICE) {
            setBody(
                TotpDeviceRequestDTO(
                    deviceName = name,
                )
            )
        }

        val body = response.body<RemoveTotpDeviceResponseDTO>()

        return when (body.status) {
            SuperTokensStatus.OK.value -> checkNotNull(body.didDeviceExist)
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

}