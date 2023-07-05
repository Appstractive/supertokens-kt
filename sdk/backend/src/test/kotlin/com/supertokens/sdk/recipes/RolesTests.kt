package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUserByEMail
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
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//@Ignore("Only for DEV purposes")
class RolesTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
        recipe(Roles)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<RolesRecipe>()
    }

    @Test
    fun testCreateOrUpdateRole() = runBlocking {
        val response = superTokens.createOrUpdateRole("Everyone", listOf("do:whatever"))
        assertEquals(SuperTokensStatus.OK, response)

        val roles = superTokens.getRoles()
        assertTrue(roles.contains("Everyone"))

        val permissions = superTokens.getRolePermissions("Everyone")
        assertTrue(permissions.contains("do:whatever"))

        val permissionRoles = superTokens.getPermissionRoles("do:whatever")
        assertTrue(permissionRoles.contains("Everyone"))
    }

    @Test
    fun testDeleteRole() = runBlocking {
        val response = superTokens.deleteRole("Everyone")
        assertEquals(SuperTokensStatus.OK, response)

        val roles = superTokens.getRoles()
        assertFalse(roles.contains("Everyone"))

        val permissionRoles = superTokens.getPermissionRoles("do:whatever")
        assertFalse(permissionRoles.contains("Everyone"))
    }

    @Test
    fun testRemoveRolePermissions() = runBlocking {
        val response = superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))
        assertEquals(SuperTokensStatus.OK, response)

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
        val user = superTokens.getUserByEMail("test@test.de")

        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))

        val response = superTokens.setUserRole(user.id, "Everyone")
        assertEquals(SuperTokensStatus.OK, response)

        val roles = superTokens.getUserRoles(user.id)
        assertTrue(roles.contains("Everyone"))
    }

    @Test
    fun testRemoveUserRole() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        superTokens.createOrUpdateRole("Everyone", listOf("do:whatever", "do:nothing"))

        superTokens.setUserRole(user.id, "Everyone")

        var roles = superTokens.getUserRoles(user.id)
        assertTrue(roles.contains("Everyone"))

        val response = superTokens.removeUserRole(user.id, "Everyone")
        assertEquals(SuperTokensStatus.OK, response)

        roles = superTokens.getUserRoles(user.id)
        assertFalse(roles.contains("Everyone"))
    }

}