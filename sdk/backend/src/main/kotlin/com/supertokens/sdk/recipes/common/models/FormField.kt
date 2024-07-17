package com.supertokens.sdk.recipes.common.models

typealias Validate = (value: String) -> Boolean

data class FormField(
    val id: String,
    val optional: Boolean = true,
    val validate: Validate? = null,
    val addToClaims: Boolean = false,
)
