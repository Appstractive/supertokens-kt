package com.appstractive.screens.auth.emailpassword

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstractive.util.isLoading
import com.slack.circuit.retained.rememberRetained

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailPassword(
    modifier: Modifier,
    state: EmailPasswordScreen.State,
) {
  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("Email/Password") },
            navigationIcon = {
              IconButton(onClick = { state.eventSink(EmailPasswordScreen.Event.GoBack) }) {
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
              SignIn(state)

              Spacer(modifier = Modifier.height(20.dp))

              SignUp(state)
            }
      }
}

@Composable
fun SignIn(
    state: EmailPasswordScreen.State,
) {
  var email: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue("test@test.de")) }
  var password: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue("a1234567")) }

  Text(
      text = "SignIn",
      style = TextStyle(fontSize = 32.sp),
  )

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Username") },
      value = email,
      onValueChange = { email = it },
  )

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Password") },
      value = password,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      onValueChange = { password = it },
  )

  Spacer(modifier = Modifier.height(20.dp))

  Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
    Button(
        enabled = !isLoading(),
        onClick = {
          state.eventSink(
              EmailPasswordScreen.Event.SignIn(
                  email = email.text,
                  password = password.text,
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
fun SignUp(
    state: EmailPasswordScreen.State,
) {
  var email: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue("test@test.de")) }
  var password: TextFieldValue by rememberRetained { mutableStateOf(TextFieldValue("a1234567")) }
  var passwordConfirm: TextFieldValue by rememberRetained {
    mutableStateOf(TextFieldValue("a1234567"))
  }

  Text(
      text = "SignUp",
      style = TextStyle(fontSize = 32.sp),
  )

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Username") },
      value = email,
      onValueChange = { email = it },
  )

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Password") },
      value = password,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      onValueChange = { password = it },
  )

  Spacer(modifier = Modifier.height(20.dp))
  TextField(
      label = { Text(text = "Confirm Password") },
      value = passwordConfirm,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      onValueChange = { passwordConfirm = it },
  )

  Spacer(modifier = Modifier.height(20.dp))
  Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
    Button(
        enabled = !isLoading(),
        onClick = {
          state.eventSink(
              EmailPasswordScreen.Event.SignUp(
                  email = email.text,
                  password = password.text,
              ))
        },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(text = "Submit")
    }
  }
}
