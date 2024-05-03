package com.appstractive.screens.auth.mfa

import androidx.compose.runtime.Composable
import com.appstractive.LocalDependencies
import com.appstractive.util.CommonParcelize
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient

@CommonParcelize
data object EmailOtpScreen : Screen {
    data class State(
        val eventSink: (Event) -> Unit = {},
    ): CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object GoBack : Event
    }
}

@Composable
fun EmailOtpScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): EmailOtpScreen.State {
    return EmailOtpScreen.State {
        when (it) {
            EmailOtpScreen.Event.GoBack -> navigator.pop()
        }
    }
}
