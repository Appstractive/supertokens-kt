package com.supertokens.sdk.recipes.emailpassword.models

import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Serializable
data class ImportUserData(
    val user: User,
    val didUserAlreadyExist: Boolean,
)
