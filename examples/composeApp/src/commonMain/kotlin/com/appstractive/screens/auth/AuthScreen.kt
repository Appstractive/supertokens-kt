package com.appstractive.screens.auth

import androidx.compose.runtime.Composable
import com.appstractive.screens.auth.emailpassword.EmailPasswordScreen
import com.appstractive.screens.auth.emailpassword.EmailPasswordScreenPresenter
import com.appstractive.screens.auth.passwordless.PasswordlessScreen
import com.appstractive.screens.auth.passwordless.PasswordlessScreenPresenter
import com.appstractive.util.CommonParcelize
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.screen.Screen

@CommonParcelize
data object AuthScreen : Screen {

    data class State(
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object GotoEmailPassword : Event
        data object GotoPasswordless : Event
    }
}

@Composable
fun AuthScreenPresenter(
    navigator: Navigator,
): AuthScreen.State {
    return AuthScreen.State {
        when (it) {
            AuthScreen.Event.GotoEmailPassword -> navigator.goTo(EmailPasswordScreen)
            AuthScreen.Event.GotoPasswordless -> navigator.goTo(PasswordlessScreen)
        }
    }
}

class AuthScreenPresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is AuthScreen -> presenterOf { AuthScreenPresenter(navigator) }
            is EmailPasswordScreen -> presenterOf { EmailPasswordScreenPresenter(navigator = navigator) }
            is PasswordlessScreen -> presenterOf { PasswordlessScreenPresenter(navigator = navigator) }
            else -> null
        }
    }
}
