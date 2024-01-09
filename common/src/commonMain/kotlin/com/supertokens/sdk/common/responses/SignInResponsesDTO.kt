package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Serializable
data class FormFieldErrorDTO(
    val id: String,
    val error: String,
)

@Serializable
data class SignInResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val user: User? = null,
    val formFields: List<FormFieldErrorDTO>? = null,
    val message: String? = null,
): BaseResponseDTO
