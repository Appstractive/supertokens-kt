package com.supertokens.ktor.recipes.multifactor

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.sdk.common.Routes
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.multiFactorRoutes(
    handler: MultiFactorHandler,
) {
    authenticate(SuperTokensAuth) {
        post(Routes.Mfa.CHECK) {
            with(handler) {
                checkMfaStatus()
            }
        }
    }
}