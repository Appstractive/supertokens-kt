package com.appstractive.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstractive.util.isLoading
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.launch

@Composable
fun Home(
    modifier: Modifier,
    state: HomeScreen.State,
) {
    val isLoading by isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello ${state.claims?.email ?: state.claims?.phoneNumber ?: state.claims?.sub}",
            style = TextStyle(fontSize = 32.sp),
        )

        Text(
            text = "Private API response: ${state.privateApiResponse ?: "LOADING"}",
            style = TextStyle(fontSize = 18.sp),
        )

        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                enabled = isLoading == 0,
                onClick = {
                    scope.launch {
                        state.eventSink(HomeScreen.Event.Logout)
                    }
                },
                shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Text(text = "SignOut")
            }
        }
    }
}

class HomeScreenUiFactory : Ui.Factory {
    override fun create(
        screen: Screen,
        context: CircuitContext,
    ): Ui<*>? {
        return when (screen) {
            is HomeScreen ->
                ui<HomeScreen.State> { state, modifier ->
                    Home(
                        state = state,
                        modifier = modifier,
                    )
                }

            else -> null
        }
    }
}
