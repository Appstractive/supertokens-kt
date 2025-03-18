package com.supertokens.ktor.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.log
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.principal
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext

typealias Role = String

typealias Permission = String

typealias AuthExtractor = suspend (Any) -> Set<Role>

class RoleBasedAuthConfiguration {
  var required: Set<String> = emptySet()
  var authCheckType: AuthCheckType = AuthCheckType.ALL
  var failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden
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
  override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
      RouteSelectorEvaluation.Constant

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
  failStatusCode: HttpStatusCode,
  build: Route.() -> Unit
): Route {
  val authorizedRoute = createChild(AuthorizedRouteSelector(required.joinToString(",")))
  authorizedRoute.install(RoleBasedAuth) {
    this.authType = type
    this.required = required
    this.authCheckType = authCheckType
    this.failStatusCode = failStatusCode
  }
  authorizedRoute.build()
  return authorizedRoute
}

fun Route.withRole(
  role: Role,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.ROLE,
        required = setOf(role),
        authCheckType = AuthCheckType.ALL,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withRoles(
  vararg roles: Role,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.ROLE,
        required = roles.toSet(),
        authCheckType = AuthCheckType.ALL,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withAnyRole(
  vararg roles: Role,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.ROLE,
        required = roles.toSet(),
        authCheckType = AuthCheckType.ANY,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withoutRoles(
  vararg roles: Role,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.ROLE,
        required = roles.toSet(),
        authCheckType = AuthCheckType.NONE,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withPermission(
  permission: Permission,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.PERMISSION,
        required = setOf(permission),
        authCheckType = AuthCheckType.ALL,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withPermissions(
  vararg permissions: Permission,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.PERMISSION,
        required = permissions.toSet(),
        authCheckType = AuthCheckType.ALL,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withAnyPermission(
  vararg permissions: Permission,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.PERMISSION,
        required = permissions.toSet(),
        authCheckType = AuthCheckType.ANY,
        failStatusCode = failStatusCode,
        build = build,
    )

fun Route.withoutPermission(
  vararg permissions: Permission,
  failStatusCode: HttpStatusCode = HttpStatusCode.Forbidden,
  build: Route.() -> Unit
) =
    buildAuthorizedRoute(
        type = AuthType.PERMISSION,
        required = permissions.toSet(),
        authCheckType = AuthCheckType.NONE,
        failStatusCode = failStatusCode,
        build = build,
    )

val RoleBasedAuth =
    createRouteScopedPlugin(
        name = "RoleBasedAuthorization", createConfiguration = ::RoleBasedAuthConfiguration,
    ) {
      if (::pluginGlobalConfig.isInitialized.not()) {
        error(
            "RoleBasedAuthPlugin not initialized. Setup plugin by calling AuthenticationConfig#roleBased in authenticate block",
        )
      }
      with(pluginConfig) {
        on(AuthenticationChecked) { call ->
          val principal = call.principal<Any>() ?: return@on
          val userAuth =
              when (authType) {
                AuthType.ROLE -> pluginGlobalConfig.roleExtractor.invoke(principal)
                AuthType.PERMISSION -> pluginGlobalConfig.permissionExtractor.invoke(principal)
              }
          val denyReasons = mutableListOf<String>()

          when (authCheckType) {
            AuthCheckType.ALL -> {
              val missing = required - userAuth
              if (missing.isNotEmpty()) {
                denyReasons +=
                    "Principal lacks required authorization(s) ${missing.joinToString(" and ")}"
              }
            }

            AuthCheckType.ANY -> {
              if (userAuth.none { it in required }) {
                denyReasons +=
                    "Principal has none of the sufficient authorization(s) ${
                      required.joinToString(
                          " or ",
                      )
                    }"
              }
            }

            AuthCheckType.NONE -> {
              if (userAuth.any { it in required }) {
                denyReasons +=
                    "Principal has forbidden authorization(s) ${
                      (required.intersect(userAuth)).joinToString(
                          " and ",
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
              call.respond(failStatusCode)
            }
          }
        }
      }
    }

class UnauthorizedAccessException(val denyReasons: MutableList<String>) : Exception()
