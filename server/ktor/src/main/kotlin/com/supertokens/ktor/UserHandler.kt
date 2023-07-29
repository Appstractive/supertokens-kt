package com.supertokens.ktor

import com.supertokens.ktor.recipes.roles.setDefaultRoles
import com.supertokens.sdk.common.models.User
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

open class UserHandler {

    open suspend fun PipelineContext<Unit, ApplicationCall>.onUserSignedUp(user: User) {
        setDefaultRoles(user)
    }

    open suspend fun PipelineContext<Unit, ApplicationCall>.onUserSignedIn(user: User) = Unit

}

val UserHandlerAttributeKey = AttributeKey<UserHandler>("UserHandler")

val ApplicationCall.userHandler: UserHandler get() = application.attributes[UserHandlerAttributeKey]
val PipelineContext<Unit, ApplicationCall>.userHandler: UserHandler get() = application.attributes[UserHandlerAttributeKey]
val Route.userHandler: UserHandler get() = application.attributes[UserHandlerAttributeKey]