package com.supertokens.sdk.recipes.totp.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.VerifyTotpDeviceRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class VerifyTotpDeviceUseCase(
    private val client: HttpClient,
) {

  suspend fun verifyDevice(deviceName: String, totp: String): SuperTokensStatus {
    val response =
        client.post(Routes.Totp.VERIFY_DEVICE) {
          setBody(
              VerifyTotpDeviceRequestDTO(
                  deviceName = deviceName,
                  totp = totp,
              ))
        }

    val body = response.body<StatusResponseDTO>()

    return body.status.toStatus()
  }
}
