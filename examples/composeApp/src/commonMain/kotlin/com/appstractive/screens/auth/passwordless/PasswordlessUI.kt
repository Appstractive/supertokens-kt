package com.appstractive.screens.auth.passwordless

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.appstractive.util.isLoading
import com.slack.circuit.retained.rememberRetained

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordlessView(
    modifier: Modifier,
    state: PasswordlessScreen.State,
) {
  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Passwordless",
              )
            },
            navigationIcon = {
              IconButton(onClick = { state.eventSink(PasswordlessScreen.Event.GoBack) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      }) {
        Column(
            modifier =
                Modifier.padding(it)
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              when (state) {
                is PasswordlessScreen.State.ConfirmCode -> CodeInput(state)
                is PasswordlessScreen.State.SignIn -> EmailOrPhone(state)
              }
            }
      }
}

@Composable
private fun EmailOrPhone(state: PasswordlessScreen.State.SignIn) {
  var email: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue("test@test.de")) }

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Email") },
      value = email,
      onValueChange = { email = it },
  )

  Spacer(modifier = Modifier.height(20.dp))

  Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
    Button(
        enabled = !isLoading(),
        onClick = {
          state.eventSink(
              PasswordlessScreen.Event.SignIn(
                  email = email.text,
              ))
        },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(text = "Submit")
    }
  }
}

@Composable
private fun CodeInput(state: PasswordlessScreen.State.ConfirmCode) {
  var code: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue()) }

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Code") },
      value = code,
      onValueChange = { code = it },
  )

  Spacer(modifier = Modifier.height(20.dp))

  Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
    Button(
        enabled = !isLoading(),
        onClick = { state.eventSink(PasswordlessScreen.Event.ConfirmCode(code = code.text)) },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(text = "Submit")
    }
  }

  Spacer(modifier = Modifier.height(20.dp))

  Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
    Button(
        enabled = !isLoading(),
        onClick = { state.eventSink(PasswordlessScreen.Event.GoBack) },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(text = "Cancel")
    }
  }
}
