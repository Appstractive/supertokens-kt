package com.supertokens.ktor

import com.supertokens.ktor.recipes.emailpassword.isEmailPasswordEnabled
import com.supertokens.ktor.recipes.passwordless.isPasswordlessEnabled
import com.supertokens.sdk.common.Routes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.coreRoutes(
    handler: CoreHandler,
) {

  if (isPasswordlessEnabled) {
    get(Routes.PHONE_NUMBER_EXISTS) { with(handler) { phoneNumberExists() } }
  }

  if (isPasswordlessEnabled || isEmailPasswordEnabled) {
    get(Routes.EMAIL_EXISTS) { with(handler) { emailExists() } }
  }
}
