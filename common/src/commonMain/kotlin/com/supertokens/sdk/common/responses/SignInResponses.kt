package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class FormFieldError(
    val id: String,
    val error: String,
)

@Serializable
data class SignInResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val user: UserResponse? = null,
    val formFields: List<FormFieldError>? = null,
    val message: String? = null,
): BaseResponse
