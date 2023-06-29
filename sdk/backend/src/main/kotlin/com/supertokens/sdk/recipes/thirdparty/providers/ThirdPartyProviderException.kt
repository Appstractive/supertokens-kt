package com.supertokens.sdk.recipes.thirdparty.providers

class ThirdPartyProviderException(
    response: String,
): RuntimeException(response)