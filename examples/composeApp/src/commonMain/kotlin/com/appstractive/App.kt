package com.appstractive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appstractive.util.ApiCallLoading
import com.appstractive.util.CommonParcelize
import com.appstractive.util.getHomeScreen
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import com.supertokens.sdk.SuperTokensClient

@Composable
fun App() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalDependencies provides dependencies,
        ) {
            CircuitCompositionLocals(dependencies.circuit) {
                ContentWithOverlays {
                    ApiCallLoading {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ) {
                            CircuitContent(AppScreen)
                        }
                    }
                }
            }
        }
    }
}

@CommonParcelize
data object AppScreen : Screen {
    sealed interface State : CircuitUiState {
        data object Loading : State

        data class Ready(
            val backStack: SaveableBackStack,
            val navigator: Navigator,
        ) : State
    }
}

@Composable
fun AppScreenPresenter(
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): AppScreen.State {
    val isInitialized by superTokensClient.isInitialized.collectAsState()

    if (isInitialized) {
        val backstack =
            rememberSaveableBackStack(
                listOf(superTokensClient.getHomeScreen()),
            )
        val navigator = getNavigator(backstack)

        return AppScreen.State.Ready(
            backStack = backstack,
            navigator = navigator,
        )
    }

    return AppScreen.State.Loading
}

@Composable
fun AppView(
    modifier: Modifier,
    state: AppScreen.State,
) {
    val appReady = state is AppScreen.State.Ready

    val enter = fadeIn(tween(400))
    val exit = fadeOut(tween(400))

    AnimatedVisibility(
        visible = !appReady,
        enter = enter,
        exit = exit,
    ) {
        SplashView(
            modifier = modifier,
        )
    }

    AnimatedVisibility(
        visible = appReady,
        enter = enter,
        exit = exit,
    ) {
        NavigableCircuitContent(
            modifier = modifier,
            navigator = (state as AppScreen.State.Ready).navigator,
            backStack = state.backStack,
        )
    }
}

@Composable
private fun SplashView(modifier: Modifier = Modifier) {
    Column(
        modifier =
        modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
            Modifier
                .size(288.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "LOADING",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        CircularProgressIndicator()
    }
}

class AppScreenPresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is AppScreen ->
                presenterOf { AppScreenPresenter() }

            else -> null
        }
    }
}

class AppScreenUiFactory : Ui.Factory {
    override fun create(
        screen: Screen,
        context: CircuitContext,
    ): Ui<*>? {
        return when (screen) {
            is AppScreen ->
                ui<AppScreen.State> { state, modifier ->
                    AppView(
                        modifier = modifier,
                        state = state,
                    )
                }

            else -> null
        }
    }
}
