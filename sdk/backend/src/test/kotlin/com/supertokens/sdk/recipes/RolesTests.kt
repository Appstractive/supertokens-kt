package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.roles.Roles
import com.supertokens.sdk.recipes.roles.RolesRecipe
import com.supertokens.sdk.recipes.roles.createOrUpdateRole
import com.supertokens.sdk.recipes.roles.deleteRole
import com.supertokens.sdk.recipes.roles.getPermissionRoles
import com.supertokens.sdk.recipes.roles.getRolePermissions
import com.supertokens.sdk.recipes.roles.getRoles
import com.supertokens.sdk.recipes.roles.getUserRoles
import com.supertokens.sdk.recipes.roles.removeRolePermissions
import com.supertokens.sdk.recipes.roles.removeUserRole
import com.supertokens.sdk.recipes.roles.setUserRole
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RolesTests : BaseTest() {

    override fun SuperTokensConfig.configure() {
        recipe(Roles)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<RolesRecipe>()
    }

    @Test
    fun testCreateOrUpdateRole() = runBlocking {
        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever"))

        val roles = superTokens.getRoles()
        assertTrue(roles.contains("Everyone"))

        val permissions = superTokens.getRolePermissions("Everyone")
        assertTrue(permissions.contains("do:whatever"))

        val permissionRoles = superTokens.getPermissionRoles("do:whatever")
        assertTrue(permissionRoles.contains("Everyone"))
    }

    @Test
    fun testDeleteRole() = runBlocking {
        superTokens.deleteRole("Everyone")

        val roles = superTokens.getRoles()
        assertFalse(roles.contains("Everyone"))

        val permissionRoles = superTokens.getPermissionRoles("do:whatever")
        assertFalse(permissionRoles.contains("Everyone"))
    }

    @Test
    fun testRemoveRolePermissions() = runBlocking {
        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))

        var permissions = superTokens.getRolePermissions("Everyone")
        assertTrue(permissions.contains("do:whatever"))
        assertTrue(permissions.contains("do:nothing"))

        superTokens.removeRolePermissions("Everyone", listOf("do:nothing"))

        permissions = superTokens.getRolePermissions("Everyone")
        assertTrue(permissions.contains("do:whatever"))
        assertFalse(permissions.contains("do:nothing"))
    }

    @Test
    fun testSetUserRole() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()

        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))

        superTokens.setUserRole(user.id, "Everyone")

        val roles = superTokens.getUserRoles(user.id)
        assertTrue(roles.contains("Everyone"))
    }

    @Test
    fun testRemoveUserRole() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()

        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))

        superTokens.setUserRole(user.id, "Everyone")

        var roles = superTokens.getUserRoles(user.id)
        assertTrue(roles.contains("Everyone"))

        superTokens.removeUserRole(user.id, "Everyone")

        roles = superTokens.getUserRoles(user.id)
        assertFalse(roles.contains("Everyone"))
    }

}