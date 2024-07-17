package com.supertokens.sdk.recipes.totp.usecases

import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.GetTotpDevicesResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.models.TotpDevice
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class GetTotpDevicesUseCase(
    private val client: HttpClient,
) {

  suspend fun getTotpDevices(): List<TotpDevice> {
    val response = client.get(Routes.Totp.GET_DEVICES)

    val body = response.body<GetTotpDevicesResponseDTO>()

    return when (body.status) {
      SuperTokensStatus.OK.value ->
          checkNotNull(body.devices).map {
            TotpDevice(
                name = it.name,
                period = it.period,
                skew = it.skew,
                verified = it.verified,
            )
          }
      else -> throw SuperTokensStatusException(body.status.toStatus())
    }
  }
}
