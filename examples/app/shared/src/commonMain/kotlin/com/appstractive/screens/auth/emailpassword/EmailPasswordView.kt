package com.appstractive.screens.auth.emailpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.appstractive.screens.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun EmailPassword() {
    Column(
        modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignIn()

        Spacer(modifier = Modifier.height(20.dp))

        SignUp()
    }
}

@Composable
fun SignIn() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { EmailPasswordSignInViewModel() }
    val navigator = LocalNavigator.currentOrThrow

    Text(
        text = "SignIn",
        style = TextStyle(fontSize = 32.sp),
    )

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Username") },
        value = viewModel.email.value,
        onValueChange = viewModel::emailChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Password") },
        value = viewModel.password.value,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = viewModel::passwordChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))

    val error = viewModel.error.value
    if(error != null) {
        Text(
            text = error,
            style = TextStyle(color = Color.Red),
        )
    }

    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
        Button(
            enabled = !viewModel.isLoading.value,
            onClick = {
                scope.launch {
                    if(viewModel.submit()) {
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
}

@Composable
fun SignUp() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { EmailPasswordSignUpViewModel() }
    val navigator = LocalNavigator.currentOrThrow

    Text(
        text = "SignUp",
        style = TextStyle(fontSize = 32.sp),
    )

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Username") },
        value = viewModel.email.value,
        onValueChange = viewModel::emailChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Password") },
        value = viewModel.password.value,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = viewModel::passwordChanged,
    )

    Spacer(modifier = Modifier.height(20.dp))
    TextField(
        label = { Text(text = "Confirm Password") },
        value = viewModel.confirmPassword.value,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = viewModel::confirmPasswordChanged,
    )

    val error = viewModel.error.value
    if(error != null) {
        Text(
            text = error,
            style = TextStyle(color = Color.Red),
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
        Button(
            enabled = !viewModel.isLoading.value,
            onClick = {
                scope.launch {
                    if(viewModel.submit()) {
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
}