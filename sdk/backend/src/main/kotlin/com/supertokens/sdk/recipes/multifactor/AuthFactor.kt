package com.supertokens.sdk.recipes.multifactor

sealed interface AuthFactor {

    fun isValidFor(factors: Map<String, Number>): Boolean

    data object TOTP : AuthFactor {
        val key = "totp"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object OTP_EMAIL : AuthFactor {
        val key = "otp-email"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object OTP_PHONE : AuthFactor {
        val key = "otp-phone"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    class AnyOf(vararg factors: AuthFactor) : AuthFactor {
        val factors: List<AuthFactor> = factors.toList()

        override fun isValidFor(factors: Map<String, Number>): Boolean {
            return this.factors.any { it.isValidFor(factors) }
        }
    }

    class AllOf(vararg factors: AuthFactor, val anyOrder: Boolean = true) : AuthFactor {
        val factors: List<AuthFactor> = factors.toList()

        override fun isValidFor(factors: Map<String, Number>): Boolean {
            val allValid = this.factors.all { it.isValidFor(factors) }

            if(allValid && anyOrder) {
                return true
            }

            val sorted = factors.entries.sortedByDescending { it.value.toLong() }.map { it.key }.toMutableList()

            this.factors.forEach { authFactor ->
                if(sorted.isEmpty()) {
                    return false
                }

                while(sorted.isNotEmpty()) {
                    val key = sorted.removeAt(0)
                    if(authFactor.equalsTo(key)) {
                        break
                    }
                }
            }

            return true
        }
    }

    fun AuthFactor.equalsTo(key: String): Boolean = when(this) {
        OTP_EMAIL -> key == TOTP.key
        OTP_PHONE -> key == OTP_EMAIL.key
        TOTP -> key == OTP_PHONE.key
        else -> false
    }

    fun List<AuthFactor>.isValid(factors: Map<String, Number>): Boolean {
        return all { it.isValidFor(factors) }
    }
}