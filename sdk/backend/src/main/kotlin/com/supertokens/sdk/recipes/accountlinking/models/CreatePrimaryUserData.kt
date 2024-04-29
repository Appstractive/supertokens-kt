package com.supertokens.sdk.recipes.accountlinking.models

import com.supertokens.sdk.common.models.User

data class CreatePrimaryUserData(
    val wasAlreadyAPrimaryUser: Boolean,
    val user: User,
)
