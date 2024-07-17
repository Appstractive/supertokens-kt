package com.appstractive.screens.auth.mfa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import com.supertokens.sdk.common.models.AuthFactor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaView(
    modifier: Modifier,
    state: MfaScreen.State,
) {
  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("Multi Factor Auth") },
            actions = {
              IconButton(onClick = { state.eventSink(MfaScreen.Event.Refresh) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
              }
            })
      }) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)) {
              OutlinedButton(
                  enabled = state.mfaStatus?.factors?.next?.contains(AuthFactor.TOTP.key) == true,
                  onClick = { state.eventSink(MfaScreen.Event.GotoTotp) },
              ) {
                Text("TOTP")
              }
              OutlinedButton(
                  enabled =
                      state.mfaStatus?.factors?.next?.any { it == AuthFactor.OTP_EMAIL.key } ==
                          true,
                  onClick = { state.eventSink(MfaScreen.Event.GotoEmailOtp) },
              ) {
                Text("Email OTP")
              }
              OutlinedButton(
                  enabled =
                      state.mfaStatus?.factors?.next?.any { it == AuthFactor.OTP_PHONE.key } ==
                          true,
                  onClick = { state.eventSink(MfaScreen.Event.GotoEmailOtp) },
              ) {
                Text("Phone OTP")
              }
              TextButton(onClick = { state.eventSink(MfaScreen.Event.Cancel) }) { Text("Cancel") }
            }
      }
}

class MfaScreenUiFactory : Ui.Factory {
  override fun create(
      screen: Screen,
      context: CircuitContext,
  ): Ui<*>? {
    return when (screen) {
      is MfaScreen ->
          ui<MfaScreen.State> { state, modifier ->
            MfaView(
                state = state,
                modifier = modifier,
            )
          }

      is EmailOtpScreen ->
          ui<EmailOtpScreen.State> { state, modifier ->
            EmailOtpView(
                state = state,
                modifier = modifier,
            )
          }

      is TotpScreen ->
          ui<TotpScreen.State> { state, modifier ->
            TotpView(
                state = state,
                modifier = modifier,
            )
          }

      else -> null
    }
  }
}
