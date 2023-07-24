package com.appstractive.screens.auth.emailpassword

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.appstractive.ViewModel
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.sendVerificationEmail
import kotlinx.coroutines.CoroutineScope

class EmailPasswordSignUpViewModel(
    scope: CoroutineScope,
    private val client: SuperTokensClient = dependencies.superTokensClient
): ViewModel(scope) {

    val email = mutableStateOf(TextFieldValue("test@test.de"))
    val password = mutableStateOf(TextFieldValue("a1234567"))
    val confirmPassword = mutableStateOf(TextFieldValue("a1234567"))

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
        // TODO validate input
        isLoading.value = true
        val result = runCatching {
            client.signUpWith(EmailPassword) {
                email = this@EmailPasswordSignUpViewModel.email.value.text
                password = this@EmailPasswordSignUpViewModel.password.value.text
            }
        }.onSuccess {
            runCatching {
                client.sendVerificationEmail()
            }
        }.onFailure {
            error.value = it.message
            return false
        }

        isLoading.value = false

        return result.isSuccess
    }

}