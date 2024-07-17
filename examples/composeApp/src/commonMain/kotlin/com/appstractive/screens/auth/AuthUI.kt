package com.appstractive.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appstractive.screens.auth.emailpassword.EmailPassword
import com.appstractive.screens.auth.emailpassword.EmailPasswordScreen
import com.appstractive.screens.auth.passwordless.PasswordlessScreen
import com.appstractive.screens.auth.passwordless.PasswordlessView
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui

@Composable
fun Auth(
    modifier: Modifier,
    state: AuthScreen.State,
) {
  Scaffold(
      modifier = modifier,
  ) { padding ->
    Column(
        modifier = Modifier.padding(padding).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)) {
          OutlinedButton(onClick = { state.eventSink(AuthScreen.Event.GotoEmailPassword) }) {
            Text("Email/Password")
          }

          OutlinedButton(onClick = { state.eventSink(AuthScreen.Event.GotoPasswordless) }) {
            Text("Passwordless")
          }

          OutlinedButton(enabled = false, onClick = {}) { Text("ThirdParty") }
        }
  }
}

class AuthScreenUiFactory : Ui.Factory {
  override fun create(
      screen: Screen,
      context: CircuitContext,
  ): Ui<*>? {
    return when (screen) {
      is AuthScreen ->
          ui<AuthScreen.State> { state, modifier ->
            Auth(
                state = state,
                modifier = modifier,
            )
          }

      is EmailPasswordScreen ->
          ui<EmailPasswordScreen.State> { state, modifier ->
            EmailPassword(
                state = state,
                modifier = modifier,
            )
          }

      is PasswordlessScreen ->
          ui<PasswordlessScreen.State> { state, modifier ->
            PasswordlessView(
                state = state,
                modifier = modifier,
            )
          }

      else -> null
    }
  }
}
