package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.passwordless: PasswordlessRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.passwordless: PasswordlessRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.passwordless: PasswordlessRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.passwordlessEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val PipelineContext<Unit, ApplicationCall>.passwordlessEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val Route.passwordlessEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()