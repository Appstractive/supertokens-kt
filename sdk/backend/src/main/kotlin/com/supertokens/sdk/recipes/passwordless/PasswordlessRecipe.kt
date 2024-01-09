package com.supertokens.sdk.recipes.passwordless

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.buildRequestPath
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.models.SignInUpData
import com.supertokens.sdk.recipes.passwordless.models.PasswordlessCodeData
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequest
import com.supertokens.sdk.get
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.post
import com.supertokens.sdk.put
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.requests.UpdateUserRequest
import com.supertokens.sdk.recipes.passwordless.requests.CreatePasswordlessCodeRequest
import com.supertokens.sdk.recipes.passwordless.requests.RevokeAllPasswordlessCodesRequest
import com.supertokens.sdk.recipes.passwordless.requests.RevokePasswordlesCodeRequest
import com.supertokens.sdk.recipes.passwordless.responses.ConsumePasswordlessCodeResponse
import com.supertokens.sdk.recipes.passwordless.responses.GetPasswordlessCodesResponse
import com.supertokens.sdk.recipes.passwordless.responses.PasswordlessCodeResponse
import com.supertokens.sdk.recipes.passwordless.responses.PasswordlessDevices
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.path

class PasswordlessRecipeConfig : RecipeConfig {

    var mode: PasswordlessMode = PasswordlessMode.MAGIC_LINK

    var emailService: EmailService? = null

}

class PasswordlessRecipe(
    private val superTokens: SuperTokens,
    private val config: PasswordlessRecipeConfig
) : Recipe<PasswordlessRecipeConfig> {

    val flowType: PasswordlessMode = config.mode

    val emailService: EmailService? = config.emailService

    /**
     * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
     */
    suspend fun createEmailCode(email: String, tenantId: String?): PasswordlessCodeData {
        val response = superTokens.post(PATH_CREATE_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    email = email,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    /**
     * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
     */
    suspend fun createPhoneNumberCode(phoneNumber: String, tenantId: String?): PasswordlessCodeData {
        val response = superTokens.post(PATH_CREATE_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    phoneNumber = phoneNumber,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    /**
     * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
     */
    suspend fun createDeviceIdCode(deviceId: String, userInputCode: String, tenantId: String?): PasswordlessCodeData {
        val response = superTokens.post(PATH_CREATE_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    deviceId = deviceId,
                    userInputCode = userInputCode,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    /**
     * Restarts a sign in process the user can use to sign in.
     */
    suspend fun recreateCode(deviceId: String, tenantId: String?): PasswordlessCodeData {
        val response = superTokens.post(PATH_CREATE_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    deviceId = deviceId,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    /**
     * Tries to consume the passed linkCode to sign the user in
     */
    suspend fun consumeLinkCode(preAuthSessionId: String, linkCode: String, tenantId: String?): SignInUpData {
        val response = superTokens.post(PATH_CONSUME_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ConsumePasswordlessCodeRequest(
                    preAuthSessionId = preAuthSessionId,
                    linkCode = linkCode,
                )
            )
        }

        return response.parse<ConsumePasswordlessCodeResponse, SignInUpData> {
            SignInUpData(
                user = checkNotNull(it.user),
                createdNewUser = checkNotNull(it.createdNewUser),
            )
        }.also {
            if(it.createdNewUser) {
                superTokens._events.tryEmit(SuperTokensEvent.UserSignUp(it.user))
            }
            else {
                superTokens._events.tryEmit(SuperTokensEvent.UserSignIn(it.user))
            }
        }
    }

    /**
     * Tries to consume the passed userInputCode+deviceId combo to sign the user in
     */
    suspend fun consumeUserInputCode(preAuthSessionId: String, deviceId: String, userInputCode: String, tenantId: String?): SignInUpData {
        val response = superTokens.post(PATH_CONSUME_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ConsumePasswordlessCodeRequest(
                    preAuthSessionId = preAuthSessionId,
                    deviceId = deviceId,
                    userInputCode = userInputCode,
                )
            )
        }

        return response.parse<ConsumePasswordlessCodeResponse, SignInUpData> {
            SignInUpData(
                user = it.user ?: throw RuntimeException("OK StatusResponse without user"),
                createdNewUser = it.createdNewUser ?: throw RuntimeException("OK StatusResponse without createdNewUser"),
            )
        }
    }

    /**
     * Revokes a code by id
     */
    suspend fun revokeCode(codeId: String, tenantId: String?): SuperTokensStatus {
        val response = superTokens.post(PATH_REVOKE_CODE, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokePasswordlesCodeRequest(
                    codeId = codeId,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    /**
     * Revokes all codes issued for the user
     */
    suspend fun revokeEmailCodes(email: String, tenantId: String?): SuperTokensStatus {
        val response = superTokens.post(PATH_REVOKE_CODES, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokeAllPasswordlessCodesRequest(
                    email = email,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    /**
     * Revokes all codes issued for the user
     */
    suspend fun revokePhoneNumberCodes(phoneNumber: String, tenantId: String?): SuperTokensStatus {
        val response = superTokens.post(PATH_REVOKE_CODES, tenantId = tenantId) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokeAllPasswordlessCodesRequest(
                    phoneNumber = phoneNumber,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    /**
     * Lists all active passwordless codes of the user
     */
    suspend fun getCodesByEmail(email: String, tenantId: String?): List<PasswordlessDevices> {
        val response = superTokens.get(
            PATH_GET_CODES,
            tenantId = tenantId,
            queryParams = mapOf(
                "email" to email
            ),
        ) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    /**
     * Lists all active passwordless codes of the user
     */
    suspend fun getCodesByDeviceId(deviceId: String, tenantId: String?): List<PasswordlessDevices> {
        val response = superTokens.get(
            PATH_GET_CODES,
            tenantId = tenantId,
            queryParams = mapOf(
                "deviceId" to deviceId
            ),
        ) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    /**
     * Lists all active passwordless codes of the user
     */
    suspend fun getCodesByPhoneNumber(phoneNumber: String, tenantId: String?): List<PasswordlessDevices> {
        val response = superTokens.get(
            PATH_GET_CODES,
            tenantId = tenantId,
            queryParams = mapOf(
                "phoneNumber" to phoneNumber
            ),
        ) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    /**
     * Lists all active passwordless codes of the user
     */
    suspend fun getCodesByPreAuthSessionId(preAuthSessionId: String, tenantId: String?): List<PasswordlessDevices> {
        val response = superTokens.get(
            PATH_GET_CODES,
            tenantId = tenantId,
            queryParams = mapOf(
                "preAuthSessionId" to preAuthSessionId
            ),
        ) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    /**
     * Update a user's phone number
     */
    suspend fun updatePhoneNumber(userId: String, phoneNumber: String, tenantId: String?): SuperTokensStatus {
        val response = superTokens.put(PATH_UPDATE_USER, tenantId = tenantId) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UpdateUserRequest(
                    recipeUserId = userId,
                    phoneNumber = phoneNumber,
                )
            )
        }

        return response.parse().also {
            superTokens._events.tryEmit(SuperTokensEvent.UserPhoneNumberChanged(
                userId = userId,
                phoneNumber = phoneNumber
            ))
        }
    }

    companion object {
        const val ID = "passwordless"

        const val PATH_CREATE_CODE = "/recipe/signinup/code"
        const val PATH_CONSUME_CODE = "/recipe/signinup/code/consume"
        const val PATH_REVOKE_CODE = "/recipe/signinup/code/remove"
        const val PATH_REVOKE_CODES = "/recipe/signinup/codes/remove"
        const val PATH_GET_CODES = "/recipe/signinup/codes"
        const val PATH_UPDATE_USER = "/recipe/user"
    }

}

val Passwordless = object : RecipeBuilder<PasswordlessRecipeConfig, PasswordlessRecipe>() {

    override fun install(configure: PasswordlessRecipeConfig.() -> Unit): (SuperTokens) -> PasswordlessRecipe {
        val config = PasswordlessRecipeConfig().apply(configure)

        return {
            PasswordlessRecipe(it, config)
        }
    }

}

/**
 * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
 */
suspend fun SuperTokens.createPasswordlessEmailCode(
    email: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().createEmailCode(
    email = email,
    tenantId = tenantId,
)

/**
 * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
 */
suspend fun SuperTokens.createPasswordlessPhoneNumberCode(
    phoneNumber: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().createPhoneNumberCode(
    phoneNumber = phoneNumber,
    tenantId = tenantId,
)

/**
 * Starts a sign in process by requesting a linkCode and a deviceId + userInputCode combination the user can use to sign in.
 */
suspend fun SuperTokens.createPasswordlessDeviceIdCode(
    deviceId: String,
    userInputCode: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().createDeviceIdCode(
    deviceId = deviceId,
    userInputCode = userInputCode,
    tenantId = tenantId,
)

/**
 * Restarts a sign in process the user can use to sign in.
 */
suspend fun SuperTokens.recreatePasswordlessCode(
    deviceId: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().recreateCode(
    deviceId = deviceId,
    tenantId = tenantId,
)

/**
 * Tries to consume the passed linkCode to sign the user in
 */
suspend fun SuperTokens.consumePasswordlessLinkCode(
    preAuthSessionId: String,
    linkCode: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().consumeLinkCode(
    preAuthSessionId = preAuthSessionId,
    linkCode = linkCode,
    tenantId = tenantId,
)

/**
 * Tries to consume the passed userInputCode+deviceId combo to sign the user in
 */
suspend fun SuperTokens.consumePasswordlessUserInputCode(
    preAuthSessionId: String,
    deviceId: String,
    userInputCode: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().consumeUserInputCode(
    preAuthSessionId = preAuthSessionId,
    deviceId = deviceId,
    userInputCode = userInputCode,
    tenantId = tenantId,
)

/**
 * Revokes a code by id
 */
suspend fun SuperTokens.revokePasswordlessCode(
    codeId: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().revokeCode(
    codeId = codeId,
    tenantId = tenantId,
)

/**
 * Revokes all codes issued for the user
 */
suspend fun SuperTokens.revokePasswordlessEmailCodes(
    email: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().revokeEmailCodes(
    email = email,
    tenantId = tenantId,
)

/**
 * Revokes all codes issued for the user
 */
suspend fun SuperTokens.revokePasswordlessPhoneNumberCodes(
    phoneNumber: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().revokePhoneNumberCodes(
    phoneNumber = phoneNumber,
    tenantId = tenantId,
)

/**
 * Lists all active passwordless codes of the user
 */
suspend fun SuperTokens.getPasswordlessCodesByEmail(
    email: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().getCodesByEmail(
    email = email,
    tenantId = tenantId,
)

/**
 * Lists all active passwordless codes of the user
 */
suspend fun SuperTokens.getPasswordlessCodesByDeviceId(
    deviceId: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().getCodesByDeviceId(
    deviceId = deviceId,
    tenantId = tenantId,
)

/**
 * Lists all active passwordless codes of the user
 */
suspend fun SuperTokens.getPasswordlessCodesByPhoneNumber(
    phoneNumber: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().getCodesByPhoneNumber(
    phoneNumber = phoneNumber,
    tenantId = tenantId,
)

/**
 * Lists all active passwordless codes of the user
 */
suspend fun SuperTokens.getPasswordlessCodesByPreAuthSessionId(
    preAuthSessionId: String,
    tenantId: String? = null,
) = getRecipe<PasswordlessRecipe>().getCodesByPreAuthSessionId(
    preAuthSessionId = preAuthSessionId,
    tenantId = tenantId,
)