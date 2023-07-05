package com.supertokens.ktor.recipes.roles

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.roles.RolesRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.roles: RolesRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.roles: RolesRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.roles: RolesRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.rolesEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()
val PipelineContext<Unit, ApplicationCall>.rolesEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()
val Route.rolesEnabled: Boolean
    get() =
        application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()