package com.appstractive.screens.auth.mfa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.appstractive.LocalDependencies
import com.appstractive.screens.home.HomeScreen
import com.appstractive.util.CommonParcelize
import com.appstractive.util.rememberApiCallController
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.models.MultiFactorAuthStatus
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuthRecipe
import com.supertokens.sdk.recipes.totp.TotpRecipe
import com.supertokens.sdk.recipes.totp.usecases.CreateTotpDeviceResult

@CommonParcelize
data object TotpScreen : Screen {
  sealed interface State : CircuitUiState {
    val eventSink: (Event) -> Unit

    data class Loading(override val eventSink: (Event) -> Unit = {}) : State

    data class AddDevice(override val eventSink: (Event) -> Unit = {}) : State

    data class VerifyDevice(
        val deviceName: String,
        val secret: String,
        val qrCodeValue: String,
        override val eventSink: (Event) -> Unit = {}
    ) : State

    data class EnterTotp(override val eventSink: (Event) -> Unit = {}) : State
  }

  sealed interface Event : CircuitUiEvent {
    data object GoBack : Event

    data class CreateDevice(
        val name: String,
    ) : Event

    data class Verify(
        val totp: String,
    ) : Event
  }
}

@Composable
fun TotpScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): TotpScreen.State {
  val apiCallController = rememberApiCallController()
  var mfaStatus: MultiFactorAuthStatus? by rememberRetained { mutableStateOf(null) }
  var pendingVerifyDeviceData: CreateTotpDeviceResult? by rememberRetained { mutableStateOf(null) }

  LaunchedEffect(superTokensClient) {
    apiCallController.call {
      mfaStatus = superTokensClient.getRecipe<MultiFactorAuthRecipe>().checkMfaStatus()
    }
  }

  val eventHandler: (TotpScreen.Event) -> Unit = {
    when (it) {
      TotpScreen.Event.GoBack -> navigator.pop()
      is TotpScreen.Event.CreateDevice ->
          apiCallController.call {
            pendingVerifyDeviceData =
                superTokensClient
                    .getRecipe<TotpRecipe>()
                    .createDevice(
                        name = it.name,
                    )
          }

      is TotpScreen.Event.Verify ->
          apiCallController.call {
            if (pendingVerifyDeviceData != null) {
              superTokensClient
                  .getRecipe<TotpRecipe>()
                  .verifyDevice(
                      name = pendingVerifyDeviceData!!.deviceName,
                      totp = it.totp,
                  )
            } else {
              superTokensClient
                  .getRecipe<TotpRecipe>()
                  .verify(
                      totp = it.totp,
                  )
            }

            navigator.resetRoot(HomeScreen)
          }
    }
  }

  return when {
    pendingVerifyDeviceData != null ->
        TotpScreen.State.VerifyDevice(
            deviceName = pendingVerifyDeviceData!!.deviceName,
            secret = pendingVerifyDeviceData!!.secret,
            qrCodeValue = pendingVerifyDeviceData!!.qrCodeString,
            eventSink = eventHandler,
        )

    mfaStatus == null ->
        TotpScreen.State.Loading(
            eventSink = eventHandler,
        )

    mfaStatus?.factors?.alreadySetup?.contains(AuthFactor.TOTP.key) == false ->
        TotpScreen.State.AddDevice(
            eventSink = eventHandler,
        )

    else ->
        TotpScreen.State.EnterTotp(
            eventSink = eventHandler,
        )
  }
}
