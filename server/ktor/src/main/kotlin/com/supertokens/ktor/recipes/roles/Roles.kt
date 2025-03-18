package com.supertokens.ktor.recipes.roles

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.recipes.roles.RolesRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.roles: RolesRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.roles: RolesRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.roles: RolesRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.roles: RolesRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.rolesEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()
val PipelineContext<Unit, ApplicationCall>.rolesEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()
val Route.rolesEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()
val RoutingContext.rolesEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<RolesRecipe>()

suspend fun RoutingContext.setDefaultRoles(user: User) {
  if (rolesEnabled) {
    roles.defaultUserRoles.forEach { roles.setUserRole(user.id, it, tenantId = call.tenantId) }
  }
}
