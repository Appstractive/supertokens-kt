package com.supertokens.sdk.common

object Routes {

  const val EMAIL_EXISTS = "signup/email/exists"
  const val PHONE_NUMBER_EXISTS = "signup/phonenumber/exists"

  object EmailPassword {

    const val SIGN_IN = "signin"
    const val SIGN_UP = "signup"
    const val PASSWORD_RESET_TOKEN = "user/password/reset/token"
    const val PASSWORD_RESET = "user/password/reset"
    const val PASSWORD_CHANGE = "user/password/change"
  }

  object EmailVerification {

    const val VERIFY_TOKEN = "user/email/verify/token"
    const val VERIFY = "user/email/verify"
    const val CHECK_VERIFIED = "user/email/verify"
  }

  object Passwordless {

    const val SIGNUP_CODE = "signinup/code"
    const val SIGNUP_CODE_RESEND = "signinup/code/resend"
    const val SIGNUP_CODE_CONSUME = "signinup/code/consume"
  }

  object Session {

    const val SIGN_OUT = "signout"
    const val REFRESH = "session/refresh"
    const val JWKS = "jwt/jwks.json"
    const val OIDC = ".well-known/openid-configuration"
  }

  object ThirdParty {

    const val SIGN_IN_UP = "signinup"
    const val AUTH_URL = "authorisationurl"
    const val CALLBACK_APPLE = "callback/apple"
  }

  object Totp {

    const val GET_DEVICES = "totp/device/list"
    const val CREATE_DEVICE = "totp/device"
    const val REMOVE_DEVICE = "totp/device/remove"
    const val VERIFY_DEVICE = "totp/device/verify"
    const val VERIFY = "totp/verify"
  }

  object Mfa {
    const val CHECK = "mfa/info"
  }
}
