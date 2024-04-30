package com.supertokens.ktor.recipes.totp

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.sdk.common.Routes
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.totpRoutes(handler: TotpHandler) {

    authenticate(SuperTokensAuth) {
        get(Routes.Totp.GET_DEVICES) {
            with(handler) {
                getDevices()
            }
        }

        post(Routes.Totp.CREATE_DEVICE) {
            with(handler) {
                createDevice()
            }
        }

        post(Routes.Totp.REMOVE_DEVICE) {
            with(handler) {
                removeDevice()
            }
        }

        post(Routes.Totp.VERIFY_DEVICE) {
            with(handler) {
                verifyDevice()
            }
        }

        post(Routes.Totp.VERIFY) {
            with(handler) {
                verify()
            }
        }
    }
}