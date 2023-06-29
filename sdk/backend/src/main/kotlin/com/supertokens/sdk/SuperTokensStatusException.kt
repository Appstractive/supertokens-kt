package com.supertokens.sdk

class SuperTokensStatusException(
    val status: SuperTokensStatus,
): RuntimeException(status.value)