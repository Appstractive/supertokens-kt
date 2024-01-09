package com.supertokens.sdk.core

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.buildRequestPath
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.core.requests.CreateJwtRequest
import com.supertokens.sdk.core.requests.DeleteUserRequest
import com.supertokens.sdk.core.responses.CreateJwtResponse
import com.supertokens.sdk.core.responses.GetUsersResponse
import com.supertokens.sdk.get
import com.supertokens.sdk.post
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import kotlinx.serialization.json.JsonObject

internal class CoreHandler {

    suspend fun SuperTokens.getUserById(userId: String): User {
        val response = client.get {
            url {
                path(buildRequestPath(path = PATH_GET_USER_BY_ID, includeTenantId = false))
                parameters.append("userId", userId)
            }
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.getUsersByEMail(email: String, doUnionOfAccountInfo: Boolean = true): List<User> {
        val response = client.get {
            url {
                path(buildRequestPath(path = PATH_GET_USER_BY_ACCOUNT_INFO))
                parameters.append("email", email)
                parameters.append("doUnionOfAccountInfo", doUnionOfAccountInfo.toString())
            }
        }

        return response.parse<GetUsersResponse, List<User>> {
            requireNotNull(it.users)
        }
    }

    suspend fun SuperTokens.getUsersByPhoneNumber(phoneNumber: String, doUnionOfAccountInfo: Boolean = true): List<User> {
        val response = client.get {
            url {
                path(buildRequestPath(path = PATH_GET_USER_BY_ACCOUNT_INFO))
                parameters.append("phoneNumber", phoneNumber)
                parameters.append("doUnionOfAccountInfo", doUnionOfAccountInfo.toString())
            }
        }

        return response.parse<GetUsersResponse, List<User>> {
            requireNotNull(it.users)
        }
    }

    suspend fun SuperTokens.getUsersByThirdParty(thirdPartyId: String, thirdPartyUserId: String, doUnionOfAccountInfo: Boolean = true): List<User> {
        val response = client.get {
            url {
                path(buildRequestPath(path = PATH_GET_USER_BY_ACCOUNT_INFO))
                parameters.append("thirdPartyId", thirdPartyId)
                parameters.append("thirdPartyUserId", thirdPartyUserId)
                parameters.append("doUnionOfAccountInfo", doUnionOfAccountInfo.toString())
            }
        }

        return response.parse<GetUsersResponse, List<User>> {
            requireNotNull(it.users)
        }
    }

    suspend fun SuperTokens.deleteUser(userId: String): SuperTokensStatus {
        val response = post(PATH_DELETE_USER, includeTenantId = false) {
            setBody(
                DeleteUserRequest(
                    userId = userId,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun SuperTokens.getJwks(): JsonObject {
        val response = get("/.well-known/jwks.json", includeTenantId = false)

        return response.body()
    }

    suspend fun SuperTokens.createJwt(
        issuer: String,
        validityInSeconds: Long = 86400L,
        useStaticSigningKey: Boolean = false,
        payload: Map<String, Any?>? = null
    ): String {
        val response = post(PATH_CREATE_JWT, includeTenantId = false) {
            setBody(
                CreateJwtRequest(
                    jwksDomain = issuer,
                    validity = validityInSeconds,
                    useStaticSigningKey = useStaticSigningKey,
                    payload = payload?.toJsonElement() ?: JsonObject(emptyMap())
                )
            )
        }

        return response.parse<CreateJwtResponse, String> {
            checkNotNull(it.jwt)
        }
    }

    companion object {
        const val PATH_GET_USER_BY_ID = "/user/id"
        const val PATH_GET_USER_BY_ACCOUNT_INFO = "/users/by-accountinfo"
        const val PATH_DELETE_USER = "/user/remove"
        const val PATH_CREATE_JWT = "/recipe/jwt"
    }

}

suspend fun SuperTokens.getUserById(userId: String): User = with(core) {
    return getUserById(userId = userId)
}

suspend fun SuperTokens.getUserByIdOrNull(userId: String): User? = runCatching {
    getUserById(userId = userId)
}.getOrNull()

suspend fun SuperTokens.getUsersByEMail(email: String, doUnionOfAccountInfo: Boolean = true) = with(core) {
    getUsersByEMail(
        email = email,
        doUnionOfAccountInfo = doUnionOfAccountInfo,
    )
}

suspend fun SuperTokens.getUserByEMailOrNull(email: String): User? = runCatching {
    getUsersByEMail(
        email = email,
        doUnionOfAccountInfo = true,
    ).firstOrNull()
}.getOrNull()

suspend fun SuperTokens.getUsersByPhoneNumber(phoneNumber: String, doUnionOfAccountInfo: Boolean = true) = with(core) {
    getUsersByPhoneNumber(
        phoneNumber = phoneNumber,
        doUnionOfAccountInfo = doUnionOfAccountInfo,
    )
}

suspend fun SuperTokens.getUsersByThirdParty(thirdPartyId: String, thirdPartyUserId: String, doUnionOfAccountInfo: Boolean = true) = with(core) {
    getUsersByThirdParty(
        thirdPartyId = thirdPartyId,
        thirdPartyUserId = thirdPartyUserId,
        doUnionOfAccountInfo = doUnionOfAccountInfo,
    )
}

suspend fun SuperTokens.getUsersByThirdPartyOrNull(thirdPartyId: String, thirdPartyUserId: String): User? = runCatching {
    getUsersByThirdParty(
        thirdPartyId = thirdPartyId,
        thirdPartyUserId = thirdPartyUserId,
        doUnionOfAccountInfo = true,
    ).firstOrNull()
}.getOrNull()

suspend fun SuperTokens.getUserByPhoneNumberOrNull(phoneNumber: String): User? = runCatching {
    getUsersByPhoneNumber(phoneNumber, true).firstOrNull()
}.getOrNull()

suspend fun SuperTokens.deleteUser(userId: String): SuperTokensStatus = with(core) {
    return deleteUser(userId)
}

suspend fun SuperTokens.getJwks(): JsonObject = with(core) {
    return getJwks()
}

suspend fun SuperTokens.createJwt(
    issuer: String,
    validityInSeconds: Long = 86400L,
    useStaticSigningKey: Boolean = false,
    payload: Map<String, Any?>? = null
): String = with(core) {
    return createJwt(
        issuer = issuer,
        validityInSeconds = validityInSeconds,
        useStaticSigningKey = useStaticSigningKey,
        payload = payload,
    )
}