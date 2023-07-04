package com.supertokens.sdk.recipes.common.models

import com.supertokens.sdk.common.models.User

data class SignInUpData(
    val createdNewUser: Boolean,
    val user: User,
)
