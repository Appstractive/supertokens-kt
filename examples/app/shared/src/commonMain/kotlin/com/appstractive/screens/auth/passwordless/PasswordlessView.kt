package com.appstractive.screens.auth.passwordless

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.appstractive.screens.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun Passwordless() {
    Column(
        modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scope = rememberCoroutineScope()
        val viewModel = remember { PasswordlessViewModel() }

        Text(
            text = "Passwordless",
            style = TextStyle(fontSize = 32.sp),
        )

        val error = viewModel.error.value
        if(error != null) {
            Text(
                text = error,
                style = TextStyle(color = Color.Red),
            )
        }

        when(viewModel.inputState.value) {
            InputState.EMAIL_OR_PHONE -> EmailOrPhone(viewModel)
            InputState.CODE -> CodeInput(viewModel)
        }
    }
}

@Composable
private fun EmailOrPhone(viewModel: PasswordlessViewModel) {
    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Email") },
        value = viewModel.email.value,
        onValueChange = viewModel::emailChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "- OR -",
    )

    Spacer(modifier = Modifier.height(20.dp))

    TextField(
        label = { Text(text = "Phone Number") },
        value = viewModel.phoneNumber.value,
        onValueChange = viewModel::phoneNumberChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
        Button(
            enabled = !viewModel.isLoading.value,
            onClick = {
                viewModel.screenModelScope.launch {
                    viewModel.send()
                }
            },
            shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
private fun CodeInput(viewModel: PasswordlessViewModel) {
    val navigator = LocalNavigator.currentOrThrow

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Code") },
        value = viewModel.code.value,
        onValueChange = viewModel::codeChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
        Button(
            enabled = !viewModel.isLoading.value,
            onClick = {
                viewModel.screenModelScope.launch {
                    if(viewModel.confirm()) {
                        // need parent navigator, because current navigator is a tab navigator
                        navigator.parent?.replace(HomeScreen)
                    }
                }
            },
            shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(text = "Submit")
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
        Button(
            enabled = !viewModel.isLoading.value,
            onClick = {
                viewModel.screenModelScope.launch {
                    viewModel.cancel()
                }
            },
            shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(text = "Cancel")
        }
    }
}