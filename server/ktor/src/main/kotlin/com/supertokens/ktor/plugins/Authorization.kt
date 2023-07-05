package com.supertokens.ktor.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

typealias Role = String
typealias Permission = String
typealias AuthExtractor = suspend (Principal) -> Set<Role>

class RoleBasedAuthConfiguration {
    var required: Set<String> = emptySet()
    var authCheckType: AuthCheckType = AuthCheckType.ALL
    lateinit var authType: AuthType
}

enum class AuthCheckType {
    ALL,
    ANY,
    NONE,
}

enum class AuthType {
    ROLE,
    PERMISSION,
}

class AuthorizedRouteSelector(private val description: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize ${description})"
}



class RoleBasedAuthPluginConfiguration {
    var roleExtractor: AuthExtractor = { emptySet() }
        private set

    fun extractRoles(extractor: AuthExtractor) {
        roleExtractor = extractor
    }

    var permissionExtractor: AuthExtractor = { emptySet() }
        private set

    fun extractPermissions(extractor: AuthExtractor) {
        permissionExtractor = extractor
    }

    var throwOnUnauthorizedResponse = false
}

private lateinit var pluginGlobalConfig: RoleBasedAuthPluginConfiguration
fun AuthenticationConfig.roleBased(config: RoleBasedAuthPluginConfiguration.() -> Unit) {
    pluginGlobalConfig = RoleBasedAuthPluginConfiguration().apply(config)
}

private fun Route.buildAuthorizedRoute(
    required: Set<String>,
    type: AuthType,
    authCheckType: AuthCheckType,
    build: Route.() -> Unit
): Route {
    val authorizedRoute = createChild(AuthorizedRouteSelector(required.joinToString(",")))
    authorizedRoute.install(RoleBasedAuth) {
        this.authType = type
        this.required = required
        this.authCheckType = authCheckType
    }
    authorizedRoute.build()
    return authorizedRoute
}

fun Route.withRole(role: Role, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = setOf(role), authCheckType = AuthCheckType.ALL, build = build)

fun Route.withRoles(vararg roles: Role, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = roles.toSet(), authCheckType = AuthCheckType.ALL, build = build)

fun Route.withAnyRole(vararg roles: Role, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = roles.toSet(), authCheckType = AuthCheckType.ANY, build = build)

fun Route.withoutRoles(vararg roles: Role, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = roles.toSet(), authCheckType = AuthCheckType.NONE, build = build)

fun Route.withPermission(permission: Permission, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = setOf(permission), authCheckType = AuthCheckType.ALL, build = build)

fun Route.withPermissions(vararg permissions: Permission, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = permissions.toSet(), authCheckType = AuthCheckType.ALL, build = build)

fun Route.withAnyPermission(vararg permissions: Permission, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = permissions.toSet(), authCheckType = AuthCheckType.ANY, build = build)

fun Route.withoutPermission(vararg permissions: Permission, build: Route.() -> Unit) =
    buildAuthorizedRoute(type = AuthType.PERMISSION, required = permissions.toSet(), authCheckType = AuthCheckType.NONE, build = build)


val RoleBasedAuth =
    createRouteScopedPlugin(name = "RoleBasedAuthorization", createConfiguration = ::RoleBasedAuthConfiguration) {
        if (::pluginGlobalConfig.isInitialized.not()) {
            error("RoleBasedAuthPlugin not initialized. Setup plugin by calling AuthenticationConfig#roleBased in authenticate block")
        }
        with(pluginConfig) {
            on(AuthenticationChecked) { call ->
                val principal = call.principal<Principal>() ?: return@on
                val userAuth = when(authType) {
                    AuthType.ROLE -> pluginGlobalConfig.roleExtractor.invoke(principal)
                    AuthType.PERMISSION -> pluginGlobalConfig.permissionExtractor.invoke(principal)
                }
                val denyReasons = mutableListOf<String>()

                when (authCheckType) {
                    AuthCheckType.ALL -> {
                        val missing = required - userAuth
                        if (missing.isNotEmpty()) {
                            denyReasons += "Principal lacks required authorization(s) ${missing.joinToString(" and ")}"
                        }
                    }

                    AuthCheckType.ANY -> {
                        if (userAuth.none { it in required }) {
                            denyReasons += "Principal has none of the sufficient authorization(s) ${
                                required.joinToString(
                                    " or "
                                )
                            }"
                        }
                    }

                    AuthCheckType.NONE -> {
                        if (userAuth.any { it in required }) {
                            denyReasons += "Principal has forbidden authorization(s) ${
                                (required.intersect(userAuth)).joinToString(
                                    " and "
                                )
                            }"

                        }
                    }
                }
                if (denyReasons.isNotEmpty()) {
                    if (pluginGlobalConfig.throwOnUnauthorizedResponse) {
                        throw UnauthorizedAccessException(denyReasons)
                    } else {
                        val message = denyReasons.joinToString(". ")
                        if (application.developmentMode) {
                            application.log.warn("Authorization failed for ${call.request.path()} $message")
                        }
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
    }

class UnauthorizedAccessException(val denyReasons: MutableList<String>) : Exception()