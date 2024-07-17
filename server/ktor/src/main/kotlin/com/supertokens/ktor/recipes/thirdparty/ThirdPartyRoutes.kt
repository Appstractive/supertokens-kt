package com.supertokens.ktor.recipes.thirdparty

import com.supertokens.sdk.common.Routes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.thirdPartyRoutes(
    handler: ThirdPartyHandler,
) {

  post(Routes.ThirdParty.SIGN_IN_UP) { with(handler) { signInUp() } }

  get(Routes.ThirdParty.AUTH_URL) { with(handler) { getAuthorizationUrl() } }

  post(Routes.ThirdParty.CALLBACK_APPLE) { with(handler) { appleAuthCallback() } }
}
