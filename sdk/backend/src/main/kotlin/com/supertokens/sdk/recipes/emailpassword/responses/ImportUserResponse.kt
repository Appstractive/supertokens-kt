package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ImportUserResponse(
    override val status: String,
    val user: User? = null,
    val didUserAlreadyExist: Boolean? = null,
): BaseResponse
