package com.supertokens.sdk.recipes.totp.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.requests.TotpDeviceRequestDTO
import com.supertokens.sdk.common.responses.CreateTotpDeviceResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

data class CreateTotpDeviceResult(
    val deviceName: String,
    val qrCodeString: String,
    val secret: String,
)

class CreateTotpDeviceUseCase(
    private val client: HttpClient,
) {

    suspend fun createDevice(name: String): CreateTotpDeviceResult {
        val response = client.post(Routes.Totp.CREATE_DEVICE) {
            setBody(
                TotpDeviceRequestDTO(
                    deviceName = name,
                )
            )
        }

        val body = response.body<CreateTotpDeviceResponseDTO>()

        return when (body.status) {
            SuperTokensStatus.OK.value -> CreateTotpDeviceResult(
                deviceName = checkNotNull(body.deviceName),
                qrCodeString = checkNotNull(body.qrCodeString),
                secret = checkNotNull(body.secret),
            )
            else -> throw SuperTokensStatusException(body.status.toStatus())
        }
    }

}