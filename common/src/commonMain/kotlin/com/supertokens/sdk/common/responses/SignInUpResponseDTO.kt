package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Deprecated("Use SignInUpResponseDTO instead")
typealias SignInUpResponse = SignInUpResponseDTO

@Serializable
data class SignInUpResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val createdNewUser: Boolean?,
    val user: User?,
): BaseResponseDTO