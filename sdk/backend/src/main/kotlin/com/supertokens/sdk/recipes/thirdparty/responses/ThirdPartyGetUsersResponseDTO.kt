package com.supertokens.sdk.recipes.thirdparty.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyGetUsersResponseDTO(
    override val status: String,
    val users: List<User>,
): BaseResponseDTO
