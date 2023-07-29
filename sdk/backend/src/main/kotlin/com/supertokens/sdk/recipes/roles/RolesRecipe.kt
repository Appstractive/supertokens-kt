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

class RolesRecipeConfig : RecipeConfig {

    var addRolesToToken: Boolean = true
    var addPermissionsToToken: Boolean = true

    var defaultUserRoles: List<String> = emptyList()

}

class RolesRecipe(
    private val superTokens: SuperTokens,
    private val config: RolesRecipeConfig
) : Recipe<RolesRecipeConfig> {

    val addRolesToToken = config.addRolesToToken
    val addPermissionsToToken = config.addPermissionsToToken
    val defaultUserRoles = config.defaultUserRoles

    override suspend fun getExtraJwtData(user: User): Map<String, Any?> {
        if (!addRolesToToken && !addPermissionsToToken) {
            return emptyMap()
        }

        val userRoles = getUserRoles(user.id)

        val userPermissions = if (addPermissionsToToken) {
            buildSet {
                userRoles.forEach { role ->
                    addAll(getRolePermissions(role))
                }
            }
        } else emptySet()

        return buildMap {
            if (addRolesToToken && userRoles.isNotEmpty()) {
                set("st-role", userRoles)
            }

            if (userPermissions.isNotEmpty()) {
                set("st-perm", userPermissions)
            }
        }
    }

    /**
     * Creates a role with permissions, can also be used to add permissions to a role
     *
     * @return true, if the role was newly created
     */
    suspend fun createOrUpdateRole(role: String, permissions: List<String>): Boolean {
        val response = superTokens.client.put(PATH_ROLE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateOrUpdateRoleRequest(
                    role = role,
                    permissions = permissions,
                )
            )
        }

        return response.parse<CreateOrUpdateRoleResponse, Boolean> {
            it.createdNewRole
        }
    }

    /**
     * Deletes a role
     *
     * @return true, if the did exist
     */
    suspend fun deleteRole(role: String): Boolean {
        val response = superTokens.client.post(PATH_ROLES_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                DeleteRoleRequest(
                    role = role,
                )
            )
        }

        return response.parse<DeleteRoleResponse, Boolean> {
            it.didRoleExist
        }
    }

    /**
     * Retrive all created roles
     */
    suspend fun getRoles(): List<String> {
        val response = superTokens.client.get(PATH_ROLES) {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolesResponse, List<String>> {
            it.roles
        }
    }

    /**
     * Retrive the permissions associated with a role
     */
    suspend fun getRolePermissions(role: String): List<String> {
        val response = superTokens.client.get("$PATH_ROLES_PERMISSIONS?role=$role") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolePermissionsResponse, List<String>> {
            it.permissions
        }
    }

    /**
     * Retrive the users associated with the role.
     */
    suspend fun getRoleUsers(role: String): List<String> {
        val response = superTokens.client.get("$PATH_ROLES_USERS?role=$role") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRoleUsersResponse, List<String>> {
            it.users
        }
    }

    /**
     * Removes permissions mapped to a role, if no permissions are passed all permissions mapped to the role are removed
     */
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

    /**
     * Retrive the roles associated with the permission
     */
    suspend fun getPermissionRoles(permission: String): List<String> {
        val response = superTokens.client.get("$PATH_PERMISSIONS_ROLES?permission=$permission") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetRolesResponse, List<String>> {
            it.roles
        }
    }

    /**
     * Creates a User Role mapping
     *
     * @return true, if the user already had the role assigned
     */
    suspend fun setUserRole(userId: String, role: String): Boolean {
        val response = superTokens.client.put(PATH_USER_ROLE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<SetUserRoleResponse, Boolean> {
            it.didUserAlreadyHaveRole
        }
    }

    /**
     * Removes a User Role mapping
     *
     * @return true, if the user had the role
     */
    suspend fun removeUserRole(userId: String, role: String): Boolean {
        val response = superTokens.client.post(PATH_USER_ROLE_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<RemoveUserRoleResponse, Boolean> {
            it.didUserHaveRole
        }
    }

    /**
     * Retrive the roles associated with the user.
     */
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

val Roles = object : RecipeBuilder<RolesRecipeConfig, RolesRecipe>() {

    override fun install(configure: RolesRecipeConfig.() -> Unit): (SuperTokens) -> RolesRecipe {
        val config = RolesRecipeConfig().apply(configure)

        return {
            RolesRecipe(it, config)
        }
    }

}

/**
 * Creates a role with permissions, can also be used to add permissions to a role
 */
suspend fun SuperTokens.createOrUpdateRole(
    role: String,
    permissions: List<String>,
) = getRecipe<RolesRecipe>().createOrUpdateRole(role, permissions)

/**
 * Deletes a role
 */
suspend fun SuperTokens.deleteRole(
    role: String,
) = getRecipe<RolesRecipe>().deleteRole(role)

/**
 * Retrive all created roles
 */
suspend fun SuperTokens.getRoles() =
    getRecipe<RolesRecipe>().getRoles()

/**
 * Retrive the permissions associated with a role
 */
suspend fun SuperTokens.getRolePermissions(
    role: String,
) = getRecipe<RolesRecipe>().getRolePermissions(role)

/**
 * Removes permissions mapped to a role, if no permissions are passed all permissions mapped to the role are removed
 */
suspend fun SuperTokens.removeRolePermissions(
    role: String,
    permissions: List<String>,
) = getRecipe<RolesRecipe>().removeRolePermissions(role, permissions)

/**
 * Retrive the users associated with the role.
 */
suspend fun SuperTokens.getRoleUsers(
    role: String,
) = getRecipe<RolesRecipe>().getRoleUsers(role)

/**
 * Retrive the roles associated with the permission
 */
suspend fun SuperTokens.getPermissionRoles(
    permission: String,
) = getRecipe<RolesRecipe>().getPermissionRoles(permission)

/**
 * Creates a User Role mapping
 */
suspend fun SuperTokens.setUserRole(
    userId: String,
    role: String,
) = getRecipe<RolesRecipe>().setUserRole(userId, role)

/**
 * Removes a User Role mapping
 */
suspend fun SuperTokens.removeUserRole(
    userId: String,
    role: String,
) = getRecipe<RolesRecipe>().removeUserRole(userId, role)

/**
 * Retrive the roles associated with the user.
 */
suspend fun SuperTokens.getUserRoles(
    userId: String,
) = getRecipe<RolesRecipe>().getUserRoles(userId)