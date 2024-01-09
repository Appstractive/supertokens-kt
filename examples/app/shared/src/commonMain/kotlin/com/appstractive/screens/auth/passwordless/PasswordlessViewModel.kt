package com.appstractive.screens.auth.passwordless

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.appstractive.ViewModel
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.PasswordlessInputCode
import kotlinx.coroutines.CoroutineScope

enum class InputState {
    EMAIL_OR_PHONE,
    CODE,
}

class PasswordlessViewModel(
    private val client: SuperTokensClient = dependencies.superTokensClient,
) : ViewModel() {

    val inputState = mutableStateOf(InputState.EMAIL_OR_PHONE)

    val email = mutableStateOf(TextFieldValue(""))
    val phoneNumber = mutableStateOf(TextFieldValue(""))

    val code = mutableStateOf(TextFieldValue(""))
    val flowType = mutableStateOf(PasswordlessMode.USER_INPUT_CODE)

    private var preAuthSessionId: String = ""
    private var deviceId: String = ""

    fun emailChanged(value: TextFieldValue) {
        email.value = value
    }

    fun phoneNumberChanged(value: TextFieldValue) {
        phoneNumber.value = value
    }

    fun codeChanged(value: TextFieldValue) {
        code.value = value
    }

    suspend fun send(): Boolean = when(inputState.value) {
        InputState.EMAIL_OR_PHONE -> {
            isLoading.value = true
            val data = runCatching {
                client.signUpWith(Passwordless) {
                    email = this@PasswordlessViewModel.email.value.text.takeIf { it.isNotBlank() }
                    phoneNumber = this@PasswordlessViewModel.phoneNumber.value.text.takeIf { it.isNotBlank() }
                }
            }.getOrElse {
                error.value = it.message

                isLoading.value = false

                return false
            }

            preAuthSessionId = data.preAuthSessionId
            deviceId = data.deviceId
            flowType.value = data.flowType

            inputState.value = InputState.CODE

            isLoading.value = false

            true
        }
        InputState.CODE -> false
    }

    suspend fun confirm(): Boolean = when(inputState.value) {
        InputState.EMAIL_OR_PHONE -> false
        InputState.CODE -> {
            isLoading.value = true
            val data = when(flowType.value) {
                PasswordlessMode.MAGIC_LINK -> TODO("Needs to be handled by deep linking")
                PasswordlessMode.USER_INPUT_CODE, PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> {
                    runCatching {
                        client.signInWith(PasswordlessInputCode) {
                            preAuthSessionId = this@PasswordlessViewModel.preAuthSessionId
                            deviceId = this@PasswordlessViewModel.deviceId
                            userInputCode = code.value.text
                        }
                    }
                }
            }.getOrElse {
                error.value = it.message

                isLoading.value = false

                return false
            }

            if(data.createdNewUser) {
                // request email verification if needed
            }

            isLoading.value = false

            true
        }
    }

    fun cancel() {
        preAuthSessionId = ""
        deviceId = ""
        code.value = TextFieldValue()
        inputState.value = InputState.EMAIL_OR_PHONE
    }

}