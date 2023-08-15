package com.supertokens.sdk.common

const val HEADER_ACCESS_TOKEN = "st-access-token"
const val HEADER_REFRESH_TOKEN = "st-refresh-token"
const val HEADER_ANTI_CSRF = "anti-csrf"

const val COOKIE_ACCESS_TOKEN = "sAccessToken"
const val COOKIE_REFRESH_TOKEN = "sRefreshToken"

const val FORM_FIELD_EMAIL_ID = "email"
const val FORM_FIELD_PASSWORD_ID = "password"


object Claims {

    const val ISSUER = "iss"
    const val AUDIENCE = "aud"
    const val USER_ID = "sub"
    const val EMAIL = "email"
    const val EMAIL_VERIFIED = "st-ev"
    const val PHONE_NUMBER = "phoneNumber"

}

object ThirdPartyProvider {

    const val APPLE = "apple"
    const val BITBUCKET = "bitbucket"
    const val FACEBOOK = "facebook"
    const val GITHUB = "github"
    const val GITLAB = "gitlab"
    const val GOOGLE = "google"

}