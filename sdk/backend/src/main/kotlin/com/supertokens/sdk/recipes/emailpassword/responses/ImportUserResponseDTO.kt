package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class ImportUserResponseDTO(
    override val status: String,
    val user: User? = null,
    val didUserAlreadyExist: Boolean? = null,
): BaseResponseDTO
