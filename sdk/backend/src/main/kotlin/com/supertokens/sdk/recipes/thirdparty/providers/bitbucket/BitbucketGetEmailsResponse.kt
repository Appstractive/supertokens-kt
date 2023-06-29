package com.supertokens.sdk.recipes.thirdparty.providers.bitbucket

import kotlinx.serialization.Serializable

@Serializable
data class BitbucketGetEmailsResponse(
    val email: String,
    val is_primary: Boolean,
    val is_confirmed: Boolean,
)
