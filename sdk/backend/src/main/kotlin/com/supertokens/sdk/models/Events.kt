package com.supertokens.sdk.models

import com.supertokens.sdk.common.models.User

sealed class SuperTokensEvent {

    class UserSignUp(val user: User, val recipeId: String): SuperTokensEvent()
    class UserSignIn(val user: User, val recipeId: String): SuperTokensEvent()
    class UserPasswordChanged(val userId: String): SuperTokensEvent()
    class UserEmailChanged(val userId: String, val email: String): SuperTokensEvent()
    class UserPhoneNumberChanged(val userId: String, val phoneNumber: String): SuperTokensEvent()
    class UserEmailVerified(val userId: String, val email: String): SuperTokensEvent()
    class UserEmailUnVerified(val userId: String, val email: String): SuperTokensEvent()

}