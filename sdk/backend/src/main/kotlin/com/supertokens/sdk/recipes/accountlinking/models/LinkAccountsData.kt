package com.supertokens.sdk.recipes.accountlinking.models

import com.supertokens.sdk.common.models.User

data class LinkAccountsData(
    val accountsAlreadyLinked: Boolean,
    val user: User,
)
