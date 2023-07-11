package com.supertokens.sdk.common

open class SuperTokensStatusException(
    val status: SuperTokensStatus,
    message: String? = null,
): RuntimeException(message ?: status.value)