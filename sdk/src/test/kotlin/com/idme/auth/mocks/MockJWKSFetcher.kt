package com.idme.auth.mocks

import com.idme.auth.jwt.JWKSFetching
import com.idme.auth.models.JWKS

class MockJWKSFetcher : JWKSFetching {
    var jwks: JWKS? = null
    var error: Exception? = null

    override suspend fun fetchJWKS(): JWKS {
        error?.let { throw it }
        return jwks!!
    }
}
