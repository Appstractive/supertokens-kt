package com.supertokens.ktor.recipes.thirdparty

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.thirdParty: ThirdPartyRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.thirdParty: ThirdPartyRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.thirdParty: ThirdPartyRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isThirdPartyEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
val PipelineContext<Unit, ApplicationCall>.isThirdPartyEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
val Route.isThirdPartyEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()