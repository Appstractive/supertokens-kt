package com.supertokens.sdk.recipes.session.models

import com.supertokens.sdk.models.SessionData
import com.supertokens.sdk.models.Token

data class VerifySessionData(
    val session: SessionData,
    val accessToken: Token?,
)