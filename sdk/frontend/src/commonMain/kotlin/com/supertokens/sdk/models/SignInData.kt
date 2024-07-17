package com.supertokens.sdk.models

import com.supertokens.sdk.common.models.User

data class SignInData(
    val createdNewUser: Boolean,
    val user: User,
)
