package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.mocks.MockJWKSFetcher
import com.idme.auth.models.JWK
import com.idme.auth.models.JWKS
import com.idme.auth.utilities.Base64URL
import kotlinx.coroutines.test.runTest
import org.junit.Test

class JWTValidatorTests {

    @Test(expected = IDmeAuthError.InvalidJWT::class)
    fun `rejects non-RS256 algorithm`() = runTest {
        val mockFetcher = MockJWKSFetcher()
        mockFetcher.jwks = JWKS(emptyList())

        val validator = JWTValidator(mockFetcher, "https://api.id.me", "test-client")

        val header = Base64URL.encode("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val payload = Base64URL.encode("""{"sub":"user-123"}""".toByteArray())
        val signature = Base64URL.encode(byteArrayOf(0x01))
        val jwt = "$header.$payload.$signature"

        validator.validate(jwt, null)
    }

    @Test(expected = IDmeAuthError.JWKSKeyNotFound::class)
    fun `rejects when kid not found in JWKS`() = runTest {
        val mockFetcher = MockJWKSFetcher()
        mockFetcher.jwks = JWKS(
            listOf(
                JWK(kty = "RSA", kid = "other-kid", use = "sig", alg = "RS256", n = "abc", e = "AQAB")
            )
        )

        val validator = JWTValidator(mockFetcher, "https://api.id.me", "test-client")

        val header = Base64URL.encode("""{"alg":"RS256","kid":"missing-kid","typ":"JWT"}""".toByteArray())
        val payload = Base64URL.encode("""{"sub":"user-123"}""".toByteArray())
        val signature = Base64URL.encode(byteArrayOf(0x01))
        val jwt = "$header.$payload.$signature"

        validator.validate(jwt, null)
    }
}
