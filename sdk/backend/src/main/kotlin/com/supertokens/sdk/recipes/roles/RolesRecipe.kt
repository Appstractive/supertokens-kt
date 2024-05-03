package com.supertokens.sdk.recipes.roles

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_ROLES
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.get
import com.supertokens.sdk.post
import com.supertokens.sdk.put
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.recipes.roles.requests.CreateOrUpdateRoleRequest
import com.supertokens.sdk.recipes.roles.requests.DeleteRoleRequest
import com.supertokens.sdk.recipes.roles.requests.RemoveRolePermissionsRequest
import com.supertokens.sdk.recipes.roles.requests.UserRoleRequest
import com.supertokens.sdk.recipes.roles.responses.CreateOrUpdateRoleResponseDTO
import com.supertokens.sdk.recipes.roles.responses.DeleteRoleResponseDTO
import com.supertokens.sdk.recipes.roles.responses.GetRolePermissionsResponseDTO
import com.supertokens.sdk.recipes.roles.responses.GetRoleUsersResponseDTO
import com.supertokens.sdk.recipes.roles.responses.GetRolesResponseDTO
import com.supertokens.sdk.recipes.roles.responses.RemoveUserRoleResponseDTO
import com.supertokens.sdk.recipes.roles.responses.SetUserRoleResponseDTO
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.header
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

    override suspend fun getExtraJwtData(
        user: User,
        tenantId: String?,
        recipeId: String,
        multiAuthFactor: AuthFactor?,
        accessToken: String?
    ): Map<String, Any?> {
        if (!addRolesToToken && !addPermissionsToToken) {
            return emptyMap()
        }

        val userRoles = getUserRoles(
            userId = user.id,
            tenantId = tenantId,
        )

        val userPermissions = if (addPermissionsToToken) {
            buildSet {
                userRoles.forEach { role ->
                    addAll(getRolePermissions(role))
                }
            }
        } else emptySet()

        return buildMap {
            if (addRolesToToken && userRoles.isNotEmpty()) {
                set(Claims.ROLES, userRoles)
            }

            if (userPermissions.isNotEmpty()) {
                set(Claims.PERMISSIONS, userPermissions)
            }
        }
    }

    /**
     * Creates a role with permissions, can also be used to add permissions to a role
     *
     * @return true, if the role was newly created
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun createOrUpdateRole(role: String, permissions: List<String>): Boolean {
        val response = superTokens.put(PATH_ROLE, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_ROLES)

            setBody(
                CreateOrUpdateRoleRequest(
                    role = role,
                    permissions = permissions,
                )
            )
        }

        return response.parse<CreateOrUpdateRoleResponseDTO, Boolean> {
            it.createdNewRole
        }
    }

    /**
     * Deletes a role
     *
     * @return true, if the did exist
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun deleteRole(role: String): Boolean {
        val response = superTokens.post(PATH_ROLES_REMOVE, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_ROLES)

            setBody(
                DeleteRoleRequest(
                    role = role,
                )
            )
        }

        return response.parse<DeleteRoleResponseDTO, Boolean> {
            it.didRoleExist
        }
    }

    /**
     * Retrive all created roles
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getRoles(): List<String> {
        val response = superTokens.get(PATH_ROLES, tenantId = null) {
            header(HEADER_RECIPE_ID, RECIPE_ROLES)
        }

        return response.parse<GetRolesResponseDTO, List<String>> {
            it.roles
        }
    }

    /**
     * Retrive the permissions associated with a role
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getRolePermissions(role: String): List<String> {
        val response = superTokens.get(
            PATH_ROLES_PERMISSIONS,
            tenantId = null,
            queryParams = mapOf(
                "role" to role,
            )
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ROLES)
        }

        return response.parse<GetRolePermissionsResponseDTO, List<String>> {
            it.permissions
        }
    }

    /**
     * Retrive the users associated with the role.
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getRoleUsers(role: String, tenantId: String?): List<String> {
        val response = superTokens.get(
            PATH_ROLES_USERS,
            tenantId = tenantId,
            queryParams = mapOf(
                "role" to role,
            ),
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ROLES)
        }

        return response.parse<GetRoleUsersResponseDTO, List<String>> {
            it.users
        }
    }

    /**
     * Removes permissions mapped to a role, if no permissions are passed all permissions mapped to the role are removed
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun removeRolePermissions(role: String, permissions: List<String>): SuperTokensStatus {
        val response = superTokens.post(PATH_ROLES_PERMISSIONS_REMOVE, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_ROLES)

            setBody(
                RemoveRolePermissionsRequest(
                    role = role,
                    permissions = permissions,
                )
            )
        }

        return response.parse<StatusResponseDTO, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    /**
     * Retrieve the roles associated with the permission
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getPermissionRoles(permission: String): List<String> {
        val response = superTokens.get(
            PATH_PERMISSIONS_ROLES,
            tenantId = null,
            queryParams = mapOf(
                "permission" to permission,
            )
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ROLES)
        }

        return response.parse<GetRolesResponseDTO, List<String>> {
            it.roles
        }
    }

    /**
     * Creates a User Role mapping
     *
     * @return true, if the user already had the role assigned
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun setUserRole(userId: String, role: String, tenantId: String?): Boolean {
        val response = superTokens.put(PATH_USER_ROLE, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_ROLES)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<SetUserRoleResponseDTO, Boolean> {
            it.didUserAlreadyHaveRole
        }
    }

    /**
     * Removes a User Role mapping
     *
     * @return true, if the user had the role
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun removeUserRole(userId: String, role: String, tenantId: String?): Boolean {
        val response = superTokens.post(PATH_USER_ROLE_REMOVE, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_ROLES)

            setBody(
                UserRoleRequest(
                    userId = userId,
                    role = role,
                )
            )
        }

        return response.parse<RemoveUserRoleResponseDTO, Boolean> {
            it.didUserHaveRole
        }
    }

    /**
     * Retrive the roles associated with the user.
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getUserRoles(userId: String, tenantId: String?): List<String> {
        val response = superTokens.get(
            PATH_USER_ROLES,
            tenantId = tenantId,
            queryParams = mapOf(
                "userId" to userId,
            ),
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ROLES)
        }

        return response.parse<GetRolesResponseDTO, List<String>> {
            it.roles
        }
    }

    companion object {
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
    tenantId: String? = null,
) = getRecipe<RolesRecipe>().getRoleUsers(
    role = role,
    tenantId = tenantId,
)

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
    tenantId: String? = null,
) = getRecipe<RolesRecipe>().setUserRole(
    userId = userId,
    role = role,
    tenantId = tenantId,
)

/**
 * Removes a User Role mapping
 */
suspend fun SuperTokens.removeUserRole(
    userId: String,
    role: String,
    tenantId: String? = null,
) = getRecipe<RolesRecipe>().removeUserRole(
    userId = userId,
    role = role,
    tenantId = tenantId,
)

/**
 * Retrive the roles associated with the user.
 */
suspend fun SuperTokens.getUserRoles(
    userId: String,
    tenantId: String? = null,
) = getRecipe<RolesRecipe>().getUserRoles(
    userId = userId,
    tenantId = tenantId,
)