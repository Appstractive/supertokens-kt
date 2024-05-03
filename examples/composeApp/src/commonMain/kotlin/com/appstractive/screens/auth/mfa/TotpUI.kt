package com.appstractive.screens.auth.mfa

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotpView(
    modifier: Modifier,
    state: TotpScreen.State,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("TOTP")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(TotpScreen.Event.GoBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
        ) {
            when (state) {
                is TotpScreen.State.AddDevice -> AddDeviceView(state)
                is TotpScreen.State.VerifyDevice -> VerifyDeviceView(state)
                is TotpScreen.State.EnterTotp -> VerifyView(state)
                is TotpScreen.State.Loading -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AddDeviceView(
    state: TotpScreen.State.AddDevice,
) {
    var deviceName by rememberRetained {
        mutableStateOf(TextFieldValue())
    }

    TextField(
        label = { Text(text = "Device Name") },
        value = deviceName,
        onValueChange = { deviceName = it },
    )

    Button(
        onClick = {
            state.eventSink(TotpScreen.Event.CreateDevice(deviceName.text))
        }
    ) {
        Text("Create")
    }
}

@Composable
private fun VerifyDeviceView(
    state: TotpScreen.State.VerifyDevice,
) {
    var totp by rememberRetained {
        mutableStateOf(TextFieldValue())
    }

    TextField(
        label = { Text(text = "Code") },
        value = totp,
        onValueChange = { totp = it },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )

    Text(state.deviceName)

    Image(
        modifier = Modifier.size(200.dp),
        painter = rememberQrCodePainter(state.qrCodeValue),
        contentDescription = "QR code referring to the example.com website"
    )

    Text(state.secret, style = MaterialTheme.typography.bodyLarge)

    Button(
        onClick = {
            state.eventSink(
                TotpScreen.Event.Verify(
                    totp = totp.text,
                )
            )
        }
    ) {
        Text("Verify")
    }
}

@Composable
private fun VerifyView(
    state: TotpScreen.State.EnterTotp,
) {
    var totp by rememberRetained {
        mutableStateOf(TextFieldValue())
    }

    TextField(
        label = { Text(text = "Code") },
        value = totp,
        onValueChange = { totp = it },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )

    Column {
        Button(
            onClick = {
                state.eventSink(
                    TotpScreen.Event.Verify(
                        totp = totp.text,
                    )
                )
            }
        ) {
            Text("Verify")
        }
    }
}
