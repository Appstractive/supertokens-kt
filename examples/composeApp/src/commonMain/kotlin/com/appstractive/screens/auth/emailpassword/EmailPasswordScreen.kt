package com.appstractive.screens.auth.emailpassword

import androidx.compose.runtime.Composable
import com.appstractive.LocalDependencies
import com.appstractive.util.CommonParcelize
import com.appstractive.util.getHomeScreen
import com.appstractive.util.rememberApiCallController
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword

@CommonParcelize
data object EmailPasswordScreen : Screen {
  data class State(
      val eventSink: (Event) -> Unit = {},
  ) : CircuitUiState

  sealed interface Event : CircuitUiEvent {
    data object GoBack : Event

    data class SignUp(val email: String, val password: String) : Event

    data class SignIn(val email: String, val password: String) : Event
  }
}

@Composable
fun EmailPasswordScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): EmailPasswordScreen.State {
  val apiCallController = rememberApiCallController()

  return EmailPasswordScreen.State {
    when (it) {
      is EmailPasswordScreen.Event.SignIn ->
          apiCallController.call {
            superTokensClient.signInWith(EmailPassword) {
              email = it.email
              password = it.password
            }
            navigator.resetRoot(superTokensClient.getHomeScreen())
          }
      is EmailPasswordScreen.Event.SignUp ->
          apiCallController.call {
            superTokensClient.signUpWith(EmailPassword) {
              email = it.email
              password = it.password
            }
            navigator.resetRoot(superTokensClient.getHomeScreen())
          }

      EmailPasswordScreen.Event.GoBack -> navigator.pop()
    }
  }
}
