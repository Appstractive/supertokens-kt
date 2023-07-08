package com.supertokens.sdk.common

open class SuperTokensStatusException(
    val status: SuperTokensStatus,
): RuntimeException(status.value)