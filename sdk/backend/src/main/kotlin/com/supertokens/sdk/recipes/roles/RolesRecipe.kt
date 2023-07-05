package com.supertokens.sdk.recipes.roles

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.roles.requests.CreateOrUpdateRoleRequest
import com.supertokens.sdk.recipes.roles.requests.DeleteRoleRequest
import com.supertokens.sdk.recipes.roles.requests.RemoveRolePermissionsRequest
import com.supertokens.sdk.recipes.roles.requests.UserRoleRequest
import com.supertokens.sdk.recipes.roles.responses.CreateOrUpdateRoleResponse
import com.supertokens.sdk.recipes.roles.responses.DeleteRoleResponse
import com.supertokens.sdk.recipes.roles.responses.GetRolePermissionsResponse
import com.supertokens.sdk.recipes.roles.responses.GetRoleUsersResponse
import com.supertokens.sdk.recipes.roles.responses.GetRolesResponse
import com.supertokens.sdk.recipes.roles.responses.RemoveUserRoleResponse
import com.supertokens.sdk.recipes.roles.responses.SetUserRoleResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class RolesRecipeConfig: RecipeConfig

class RolesRecipe(
    private val superTokens: SuperTokens,
    private val config: RolesRecipeConfig
): Recipe<RolesRecipeConfig> {

    override suspend fun getExtraJwtData(user: User): Map<String, Any?> {
        val userRoles = getUserRoles(user.id)
        val userPermissions = buildSet {
            userRoles.forEach {  role ->
                addAll(getRolePermissions(role))
            }
        }

        return buildMap {
            if(userRoles.isNotEmpty()) {
                set("st-role", userRoles)
            }

            if(userPermissions.isNotEmpty()) {
                set("st-perm", userPermissions)
            }
        }
    }

    suspend fun createOrUpdateRole(role: String, permissions: List<String>): SuperTokensStatus {
        val response = superTokens.client.put(PATH_ROLE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateOrUpdateRoleRequest(
                    role = role,
                    permissions = permissions,
                )
            )
        }

        return response.parse<CreateOrUpdateRoleResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun deleteRole(role: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_ROLES_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                DeleteRoleRequest(
                    role = role,
                )
            )
        }

        return response.parse<DeleteRoleResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun getRoles(): List<String> {
        val response = superTokens.client.get(PATH_ROLES) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolesResponse, List<String>> {
            it.roles
        }
    }

    suspend fun getRolePermissions(role: String): List<String> {
        val response = superTokens.client.get("$PATH_ROLES_PERMISSIONS?role=$role") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolePermissionsResponse, List<String>> {
            it.permissions
        }
    }

    suspend fun getRoleUsers(role: String): List<String> {
        val response = superTokens.client.get("$PATH_ROLES_USERS?role=$role") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRoleUsersResponse, List<String>> {
            it.users
        }
    }

    suspend fun removeRolePermissions(role: String, permissions: List<String>): SuperTokensStatus {
        val response = superTokens.client.post(PATH_ROLES_PERMISSIONS_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RemoveRolePermissionsRequest(
                    role = role,
                    permissions = permissions,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun getPermissionRoles(permission: String): List<String> {
        val response = superTokens.client.get("$PATH_PERMISSIONS_ROLES?permission=$permission") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolesResponse, List<String>> {
            it.roles
        }
    }

    suspend fun setUserRole(userId: String, role: String): SuperTokensStatus {
        val response = superTokens.client.put(PATH_USER_ROLE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<SetUserRoleResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun removeUserRole(userId: String, role: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_USER_ROLE_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<RemoveUserRoleResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun getUserRoles(userId: String): List<String> {
        val response = superTokens.client.get("$PATH_USER_ROLES?userId=$userId") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolesResponse, List<String>> {
            it.roles
        }
    }

    companion object {
        const val ID = "userroles"

        const val PATH_ROLE = "/recipe/role"
        const val PATH_ROLES = "/recipe/roles"
        const val PATH_ROLES_REMOVE = "/recipe/role/remove"
        const val PATH_ROLES_PERMISSIONS = "/recipe/role/permissions"
        const val PATH_ROLES_PERMISSIONS_REMOVE = "/recipe/role/permissions/remove"
        const val PATH_ROLES_USERS = "/recipe/role/permissions/users"
        const val PATH_PERMISSIONS_ROLES = "/recipe/permission/roles"
        const val PATH_USER_ROLE = "/recipe/user/role"
        const val PATH_USER_ROLE_REMOVE = "/recipe/user/role/remove"
        const val PATH_USER_ROLES = "/recipe/user/roles"
    }

}

val Roles = object: RecipeBuilder<RolesRecipeConfig, RolesRecipe>() {

    override fun install(configure: RolesRecipeConfig.() -> Unit): (SuperTokens) -> RolesRecipe {
        val config = RolesRecipeConfig().apply(configure)

        return {
            RolesRecipe(it, config)
        }
    }

}

suspend fun SuperTokens.createOrUpdateRole(
    role: String,
    permissions: List<String>,
) = getRecipe<RolesRecipe>().createOrUpdateRole(role, permissions)

suspend fun SuperTokens.deleteRole(
    role: String,
) = getRecipe<RolesRecipe>().deleteRole(role)

suspend fun SuperTokens.getRoles() =
    getRecipe<RolesRecipe>().getRoles()

suspend fun SuperTokens.getRolePermissions(
    role: String,
) = getRecipe<RolesRecipe>().getRolePermissions(role)

suspend fun SuperTokens.removeRolePermissions(
    role: String,
    permissions: List<String>,
) = getRecipe<RolesRecipe>().removeRolePermissions(role, permissions)

suspend fun SuperTokens.getRoleUsers(
    role: String,
) = getRecipe<RolesRecipe>().getRoleUsers(role)

suspend fun SuperTokens.getPermissionRoles(
    permission: String,
) = getRecipe<RolesRecipe>().getPermissionRoles(permission)

suspend fun SuperTokens.setUserRole(
    userId: String,
    role: String,
) = getRecipe<RolesRecipe>().setUserRole(userId, role)

suspend fun SuperTokens.removeUserRole(
    userId: String,
    role: String,
) = getRecipe<RolesRecipe>().removeUserRole(userId, role)

suspend fun SuperTokens.getUserRoles(
    userId: String,
) = getRecipe<RolesRecipe>().getUserRoles(userId)