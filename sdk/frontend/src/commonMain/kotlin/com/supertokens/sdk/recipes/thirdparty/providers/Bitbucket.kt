package com.supertokens.sdk.recipes.thirdparty.providers

import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.recipes.thirdparty.Provider
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyAuthCode
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyTokens

object Bitbucket: Provider {

    override val id = ThirdPartyProvider.BITBUCKET

    object AuthCode: ThirdPartyAuthCode(id)
    object Tokens: ThirdPartyTokens(id)

}