package com.supertokens.sdk.common.models

import kotlinx.serialization.Serializable

@Serializable
data class ThirdParty(
    val id: String,
    val userId: String,
)

@Serializable
data class LoginMethod(
    val tenantIds: List<String>? = null,
    val recipeUserId: String,
    val verified: Boolean,
    val timeJoined: Long,
    val recipeId: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val thirdParty: ThirdParty? = null,
)

@Serializable
data class User(
    val id: String,
    val isPrimaryUser: Boolean? = null,
    val tenantIds: List<String>? = null,
    val emails: List<String>? = null,
    val phoneNumbers: List<String>? = null,
    val timeJoined: Long,
    val thirdParty: List<ThirdParty>? = null,
    val loginMethods: List<LoginMethod>? = null,
    val recipeUserId: String? = null,
) {

  @Deprecated("Use emails list instead", ReplaceWith("emails?.firstOrNull()"))
  val email: String?
    get() = emails?.firstOrNull()

  @Deprecated("Use phoneNumbers list instead", ReplaceWith("phoneNumbers?.firstOrNull()"))
  val phoneNumber: String?
    get() = phoneNumbers?.firstOrNull()
}
