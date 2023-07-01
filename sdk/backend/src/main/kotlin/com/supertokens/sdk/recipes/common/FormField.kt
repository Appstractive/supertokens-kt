package com.supertokens.sdk.recipes.common

typealias Validate = (value: String) -> Boolean

data class FormField(
    val id: String,
    val optional: Boolean = true,
    val validate: Validate? = null,
    val addToClaims: Boolean = false,
)