package com.appstractive.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.appstractive.screens.splash.SplashScreen
import kotlinx.coroutines.launch

object HomeScreen : Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val viewModel = rememberScreenModel { HomeViewModel() }
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello ${viewModel.userId.value}",
                style = TextStyle(fontSize = 32.sp),
            )

            Text(
                text = "Private API response: ${viewModel.privateResponse.value ?: "LOADING"}",
                style = TextStyle(fontSize = 18.sp),
            )

            Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                Button(
                    enabled = !viewModel.isLoading.value,
                    onClick = {
                        scope.launch {
                            viewModel.signOut()
                            navigator.replace(SplashScreen())
                        }
                    },
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Text(text = "SignOut")
                }
            }
        }
    }

}