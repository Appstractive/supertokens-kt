package com.appstractive.screens.auth.mfa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.slack.circuit.retained.rememberRetained

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailOtpView(
    modifier: Modifier,
    state: EmailOtpScreen.State,
) {
  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("TOTP") },
            navigationIcon = {
              IconButton(onClick = { state.eventSink(EmailOtpScreen.Event.GoBack) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      }) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)) {
              if (state.email != null) {
                Text("Confirm Login with ${state.email}")

                Button(
                    onClick = { state.eventSink(EmailOtpScreen.Event.SendEmail) },
                ) {
                  Text(if (state.emailSend) "Resend OTP" else "Send OTP")
                }

                var otp by rememberRetained { mutableStateOf(TextFieldValue()) }

                TextField(
                    enabled = state.emailSend,
                    label = { Text(text = "Code") },
                    value = otp,
                    onValueChange = { otp = it },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                )

                Button(
                    enabled = state.emailSend && otp.text.isNotEmpty(),
                    onClick = { state.eventSink(EmailOtpScreen.Event.ConfirmCode(otp.text)) },
                ) {
                  Text("Confirm")
                }
              } else {
                CircularProgressIndicator()
              }
            }
      }
}
