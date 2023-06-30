package com.supertokens.sdk

import com.supertokens.sdk.common.SuperTokensStatus

class SuperTokensStatusException(
    val status: SuperTokensStatus,
): RuntimeException(status.value)