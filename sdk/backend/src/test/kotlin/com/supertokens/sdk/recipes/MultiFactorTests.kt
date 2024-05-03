package com.supertokens.sdk.recipes

import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.common.models.AuthFactor.OTP_PHONE.isValid
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultiFactorTests {

    @Test
    fun testSingleAuthFactor() {
        val factors = listOf(AuthFactor.TOTP)

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )
    }

    @Test
    fun testMultipleAuthFactor() {
        val factors = listOf(
            AuthFactor.TOTP,
            AuthFactor.OTP_PHONE,
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )
    }

    @Test
    fun testAnyAuthFactor() {
        val factors = listOf(
            AuthFactor.AnyOf(AuthFactor.TOTP, AuthFactor.OTP_PHONE),
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                )
            )
        )
    }

    @Test
    fun testAllAuthFactor() {
        val factors = listOf(
            AuthFactor.AllOf(AuthFactor.TOTP, AuthFactor.OTP_PHONE),
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                )
            )
        )
    }

    @Test
    fun testAllInOrderAuthFactor() {
        val factors = listOf(
            AuthFactor.AllOf(
                AuthFactor.TOTP,
                AuthFactor.OTP_PHONE,
                anyOrder = false,
            ),
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.TOTP.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis() + 100000,
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis() - 100000,
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis() + 100000,
                    AuthFactor.TOTP.key to System.currentTimeMillis() - 100000,
                )
            )
        )

        assertTrue(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis(),
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis() + 100000,
                    AuthFactor.TOTP.key to System.currentTimeMillis() - 100000,
                )
            )
        )

        assertFalse(
            factors.isValid(
                mapOf(
                    AuthFactor.OTP_EMAIL.key to System.currentTimeMillis(),
                    AuthFactor.TOTP.key to System.currentTimeMillis() + 100000,
                    AuthFactor.OTP_PHONE.key to System.currentTimeMillis() - 100000,
                )
            )
        )
    }

}