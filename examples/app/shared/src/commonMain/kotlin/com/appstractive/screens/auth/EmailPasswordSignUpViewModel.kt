package com.appstractive.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword

class EmailPasswordSignUpViewModel(
    private val apiClient: SuperTokensClient = dependencies.apiClient
) {

    val email = mutableStateOf(TextFieldValue())
    val password = mutableStateOf(TextFieldValue())
    val confirmPassword = mutableStateOf(TextFieldValue())
    val error = mutableStateOf<String?>(null)

    fun emailChanged(value: TextFieldValue) {
        email.value = value
    }

    fun passwordChanged(value: TextFieldValue) {
        password.value = value
    }

    fun confirmPasswordChanged(value: TextFieldValue) {
        confirmPassword.value = value
    }

    suspend fun submit(): Boolean {
        val result = kotlin.runCatching {
            apiClient.signInWith(EmailPassword) {
                email = this@EmailPasswordSignUpViewModel.email.value.text
                password = this@EmailPasswordSignUpViewModel.password.value.text
            }
        }

        return result.isSuccess
    }

}