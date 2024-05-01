package com.supertokens.sdk.recipes.multifactor

internal interface Factor: AuthFactor {
    val key: String
}

sealed interface AuthFactor {

    fun isValidFor(factors: Map<String, Number>): Boolean

    data object TOTP : Factor {
        override val key = "totp"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object OTP_EMAIL : Factor {
        override val key = "otp-email"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object OTP_PHONE : Factor {
        override val key = "otp-phone"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object LINK_EMAIL : Factor {
        override val key = "link-email"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    data object LINK_PHONE : Factor {
        override val key = "link-phone"
        override fun isValidFor(factors: Map<String, Number>) = factors.containsKey(key)
    }

    class AnyOf(vararg factors: AuthFactor) : AuthFactor {
        private val factors: List<AuthFactor> = factors.toList()

        override fun isValidFor(factors: Map<String, Number>): Boolean {
            return this.factors.any { it.isValidFor(factors) }
        }
    }

    class AllOf(vararg factors: AuthFactor, val anyOrder: Boolean = true) : AuthFactor {
        private val factors: List<AuthFactor> = factors.toList()

        override fun isValidFor(factors: Map<String, Number>): Boolean {
            val allValid = this.factors.all { it.isValidFor(factors) }

            if(allValid && anyOrder) {
                return true
            }

            val sorted = factors.entries.sortedBy { it.value.toLong() }.map { it.key }.toMutableList()

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
        is Factor -> key == this.key
        else -> false
    }

    fun List<AuthFactor>.isValid(factors: Map<String, Number>): Boolean {
        return isEmpty() || all { it.isValidFor(factors) }
    }
}