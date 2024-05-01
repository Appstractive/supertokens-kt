package com.supertokens.ktor.recipes.totp

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.accessToken
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.session.isSessionsEnabled
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_TOTP
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.TotpDeviceRequestDTO
import com.supertokens.sdk.common.requests.VerifyTotpDeviceRequestDTO
import com.supertokens.sdk.common.requests.VerifyTotpRequestDTO
import com.supertokens.sdk.common.responses.CreateTotpDeviceResponseDTO
import com.supertokens.sdk.common.responses.GetTotpDevicesResponseDTO
import com.supertokens.sdk.common.responses.RemoveTotpDeviceResponseDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.responses.TotpDeviceDTO
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.recipes.multifactor.AuthFactor
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

open class TotpHandler(
    protected val scope: CoroutineScope,
) {

    /**
     * A call to GET /totp/device/list
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getDevices() {
        val user = call.requirePrincipal<AuthenticatedUser>()

        val devices = totp.getDevices(userId = user.id)

        call.respond(
            GetTotpDevicesResponseDTO(
                devices = devices.map {
                    TotpDeviceDTO(
                        name = it.name,
                        period = it.period,
                        skew = it.skew,
                        verified = it.verified,
                    )
                },
            )
        )
    }

    /**
     * A call to POST /totp/device
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.createDevice() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val body = call.receive<TotpDeviceRequestDTO>()

        val secret = totp.addDevice(
            userId = user.id,
            deviceName = body.deviceName,
        )
        val issuer = totp.issuer

        call.respond(
            CreateTotpDeviceResponseDTO(
                deviceName = body.deviceName,
                secret = secret,
                qrCodeString = "otpauth://totp/$issuer?secret=$secret&issuer=$issuer",
            )
        )
    }

    /**
     * A call to POST /totp/device/remove
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.removeDevice() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val body = call.receive<TotpDeviceRequestDTO>()

        val didExist = totp.removeDevice(
            userId = user.id,
            deviceName = body.deviceName,
        )

        call.respond(
            RemoveTotpDeviceResponseDTO(
                didDeviceExist = didExist,
            )
        )
    }

    /**
     * A call to POST /totp/device/verify
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.verifyDevice() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val body = call.receive<VerifyTotpDeviceRequestDTO>()

        totp.verifyDevice(
            userId = user.id,
            deviceName = body.deviceName,
            totp = body.totp,
            tenantId = call.tenantId,
        )

        if (isSessionsEnabled) {
            accessToken?.let { token ->
                val session = sessions.verifySession(token, checkDatabase = true)

                if(session.session.userId == user.id) {

                    val newSession = sessions.regenerateSession(
                        accessToken = token,
                        userDataInJWT = sessions.getJwtData(
                            user = superTokens.getUserById(user.id),
                            tenantId = null,
                            recipeId = RECIPE_TOTP,
                            multiAuthFactor = AuthFactor.TOTP,
                            accessToken = token,
                        )
                    )

                    setSessionInResponse(
                        accessToken = newSession.accessToken,
                    )
                }
            }
        }

        call.respond(StatusResponseDTO())
    }

    /**
     * A call to POST /totp/verify
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.verify() {
        val user = call.requirePrincipal<AuthenticatedUser>()
        val body = call.receive<VerifyTotpRequestDTO>()

        val result = totp.verifyCode(
            userId = user.id,
            totp = body.totp,
            tenantId = call.tenantId,
        )

        if (result == SuperTokensStatus.OK && isSessionsEnabled) {
            accessToken?.let { token ->
                val session = sessions.verifySession(token, checkDatabase = true)

                if(session.session.userId == user.id) {

                    val newSession = sessions.regenerateSession(
                        accessToken = token,
                        userDataInJWT = sessions.getJwtData(
                            user = superTokens.getUserById(user.id),
                            tenantId = null,
                            recipeId = RECIPE_TOTP,
                            multiAuthFactor = AuthFactor.TOTP,
                            accessToken = token,
                        )
                    )

                    setSessionInResponse(
                        accessToken = newSession.accessToken,
                    )
                }
            }
        }

        call.respond(StatusResponseDTO(status = result.value))
    }

}