package com.supertokens.ktor.recipes.multifactor

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.passwordless.isPasswordlessEnabled
import com.supertokens.ktor.recipes.passwordless.passwordless
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.totp.isTotpEnabled
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.RECIPE_MULTI_FACTOR_AUTH
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.common.models.AuthFactor.OTP_PHONE.missing
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.responses.EmailsStatusDTO
import com.supertokens.sdk.common.responses.FactorsStatusDTO
import com.supertokens.sdk.common.responses.MultiFactorStatusResponseDTO
import com.supertokens.sdk.common.responses.PhoneStatusDTO
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.recipes.totp.getTotpDevices
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

val MfaHandlerAttributeKey = AttributeKey<MultiFactorHandler>("MfaHandler")

val ApplicationCall.mfaHandler: MultiFactorHandler get() = application.attributes[MfaHandlerAttributeKey]
val PipelineContext<Unit, ApplicationCall>.mfaHandler: MultiFactorHandler get() = application.attributes[MfaHandlerAttributeKey]
val Route.mfaHandler: MultiFactorHandler get() = application.attributes[MfaHandlerAttributeKey]

open class MultiFactorHandler(
    protected val scope: CoroutineScope,
) {
    open fun publicMfaRoutes(basePath: String = "/") = listOf(
        "$basePath${Routes.Mfa.CHECK}",
        "$basePath${Routes.Totp.VERIFY}",
        "$basePath${Routes.Totp.VERIFY_DEVICE}",
        "$basePath${Routes.Passwordless.SIGNUP_CODE_CONSUME}",
        "$basePath${Routes.Session.REFRESH}",
    )

    /**
     * A call to POST /mfa/info
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.19.0#/MultiFactorAuth%20Recipe/getMFAInfo">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.checkMfaStatus() {
        val principal = call.requirePrincipal<AuthenticatedUser>()
        val user = superTokens.getUserById(userId = principal.id)

        val sessionData = sessions.regenerateSession(
            accessToken = principal.accessToken,
            userDataInJWT = sessions.getJwtData(
                user = superTokens.getUserById(user.id),
                tenantId = null,
                recipeId = RECIPE_MULTI_FACTOR_AUTH,
                multiAuthFactor = null,
                accessToken = principal.accessToken,
            )
        ).also {
            setSessionInResponse(
                accessToken = it.accessToken,
            )
        }.session

        val userDataInJWT = sessionData.userDataInJWT ?: emptyMap()
        val factors = multiFactorAuth.getFactorsFromJwtData(userDataInJWT)

        val requiredSecondFactors = multiFactorAuth.getRequiredMultiFactors(
            superTokens,
            user,
            call.tenantId,
        )

        val hasTotpDevice =
            isTotpEnabled && superTokens.getTotpDevices(userId = principal.id).any { it.verified }

        val passwordLessMethods = user.loginMethods?.filter { it.recipeId == RECIPE_PASSWORDLESS && it.verified } ?: emptyList()
        val hasPasswordless = isPasswordlessEnabled && passwordLessMethods.isNotEmpty()
        val hasPasswordlessEmail = hasPasswordless && passwordLessMethods.any {
            it.email != null
        }

        val hasPasswordlessPhone = hasPasswordless && passwordLessMethods.any {
            it.phoneNumber != null
        }

        val alreadySetup = getAlreadySetup(
            hasTotpDevice = hasTotpDevice,
            hasPasswordlessEmail = hasPasswordlessEmail,
            hasPasswordlessPhone = hasPasswordlessPhone,
        )

        // if already mf authenticated, or none setup yet
        val allowedSetup = if(sessionData.userDataInJWT.isMultiFactorAuthenticated || alreadySetup.isEmpty()) {
            getAllowedToSetup(
                hasTotpDevice = hasTotpDevice,
                hasPasswordlessEmail = hasPasswordlessEmail,
                hasPasswordlessPhone = hasPasswordlessPhone,
            )
        }
        else {
            emptyList()
        }

        // continue next with
        val missing = requiredSecondFactors.missing(factors).filter {
            // any already setup
            alreadySetup.contains(it) ||
                    // or if none setup
                    (alreadySetup.isEmpty() &&
                            // and not yet authenticated
                            !sessionData.userDataInJWT.isMultiFactorAuthenticated &&
                            // any allowed
                            allowedSetup.contains(it))
        }

        call.respond(
            MultiFactorStatusResponseDTO(
                factors = FactorsStatusDTO(
                    alreadySetup = alreadySetup,
                    allowedToSetup = allowedSetup,
                    next = missing,
                ),
                emails = EmailsStatusDTO(),
                phoneNumbers = PhoneStatusDTO(),
            )
        )
    }

    open fun PipelineContext<Unit, ApplicationCall>.getAlreadySetup(
        hasTotpDevice: Boolean,
        hasPasswordlessEmail: Boolean,
        hasPasswordlessPhone: Boolean,
    ): List<String> {
        return buildList {
            if (isTotpEnabled && hasTotpDevice) {
                add(AuthFactor.TOTP.key)
            }
            if (isPasswordlessEnabled) {
                if (hasPasswordlessEmail) {
                    when (passwordless.flowType) {
                        PasswordlessMode.MAGIC_LINK -> add(AuthFactor.LINK_EMAIL.key)
                        PasswordlessMode.USER_INPUT_CODE -> add(AuthFactor.OTP_EMAIL.key)
                        PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> addAll(
                            listOf(AuthFactor.LINK_EMAIL.key, AuthFactor.OTP_EMAIL.key)
                        )
                    }
                }

                if (hasPasswordlessPhone) {
                    when (passwordless.flowType) {
                        PasswordlessMode.MAGIC_LINK -> add(AuthFactor.LINK_PHONE.key)
                        PasswordlessMode.USER_INPUT_CODE -> add(AuthFactor.OTP_PHONE.key)
                        PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> addAll(
                            listOf(AuthFactor.LINK_PHONE.key, AuthFactor.OTP_PHONE.key)
                        )
                    }
                }
            }
        }
    }

    open fun PipelineContext<Unit, ApplicationCall>.getAllowedToSetup(
        hasTotpDevice: Boolean,
        hasPasswordlessEmail: Boolean,
        hasPasswordlessPhone: Boolean,
    ): List<String> {
        return buildList {
            if (isTotpEnabled && !hasTotpDevice) {
                add(AuthFactor.TOTP.key)
            }
            if (isPasswordlessEnabled) {
                if (!hasPasswordlessEmail) {
                    when (passwordless.flowType) {
                        PasswordlessMode.MAGIC_LINK -> add(AuthFactor.LINK_EMAIL.key)
                        PasswordlessMode.USER_INPUT_CODE -> add(AuthFactor.OTP_EMAIL.key)
                        PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> addAll(
                            listOf(AuthFactor.LINK_EMAIL.key, AuthFactor.OTP_EMAIL.key)
                        )
                    }
                }

                if (!hasPasswordlessPhone) {
                    when (passwordless.flowType) {
                        PasswordlessMode.MAGIC_LINK -> add(AuthFactor.LINK_PHONE.key)
                        PasswordlessMode.USER_INPUT_CODE -> add(AuthFactor.OTP_PHONE.key)
                        PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK -> addAll(
                            listOf(AuthFactor.LINK_PHONE.key, AuthFactor.OTP_PHONE.key)
                        )
                    }
                }
            }
        }
    }

    open suspend fun ApplicationCall.checkMfaAuth(
        credential: JWTCredential,
    ): Boolean {
        if (!isMultiFactorAuthEnabled) {
            return true
        }

        val mfa = credential.payload.getClaim(Claims.MFA).asMap()
        if (mfa[Claims.MFA_VERIFIED] == true) {
            return true
        }

        val path = this.request.path()
        if (publicMfaRoutes(basePath = superTokens.appConfig.api.path).contains(path)) {
            return true
        }

        if (path == "${superTokens.appConfig.api.path}${Routes.Totp.CREATE_DEVICE}" &&
            mustAddTotpDevice(
                credential = credential,
            )
        ) {
            return true
        }

        return false
    }

    open suspend fun ApplicationCall.mustAddTotpDevice(
        credential: JWTCredential,
    ): Boolean {
        val requiredSecondFactors = multiFactorAuth.getRequiredMultiFactors(
            superTokens,
            superTokens.getUserById(userId = credential.payload.subject),
            tenantId,
        )

        val userDataInJWT = credential.payload.claims
        val factors = multiFactorAuth.getFactorsFromJwtData(userDataInJWT)
        val missing = requiredSecondFactors.missing(factors)

        return missing.contains(AuthFactor.TOTP.key)
    }
}

private val Map<String, Any?>?.isMultiFactorAuthenticated
    get() = (this?.get(Claims.MFA) as? Map<String, Any?>)?.get(Claims.MFA_VERIFIED) == true
