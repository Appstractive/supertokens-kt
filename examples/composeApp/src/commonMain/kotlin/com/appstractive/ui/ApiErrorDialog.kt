package com.appstractive.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import com.supertokens.sdk.common.SuperTokensStatusException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.utils.io.errors.IOException

class ApiErrorDialog(
    private val exception: Exception,
) : Overlay<Unit> {
  @Composable
  override fun Content(navigator: OverlayNavigator<Unit>) {
    AlertDialog(
        confirmButton = { TextButton(onClick = { navigator.finish(Unit) }) { Text("OK") } },
        onDismissRequest = { navigator.finish(Unit) },
        text = { Text(getErrorMessage(exception)) },
        title = { Text("Error") },
    )
  }

  @Composable
  private fun getErrorMessage(exception: Exception): String =
      when (exception) {
        is SuperTokensStatusException ->
            when (exception.status) {
              else -> exception.status.value
            }

        is IOException,
        is ConnectTimeoutException,
        is SocketTimeoutException, -> "Connection Error"

        else -> "Unknown Error"
      }
}
