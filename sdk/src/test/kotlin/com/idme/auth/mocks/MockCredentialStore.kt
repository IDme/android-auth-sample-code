package com.idme.auth.mocks

import com.idme.auth.models.Credentials
import com.idme.auth.storage.CredentialStoring

class MockCredentialStore : CredentialStoring {
    var stored: Credentials? = null
    var saveCallCount = 0
    var deleteCallCount = 0

    override fun save(credentials: Credentials) {
        saveCallCount++
        stored = credentials
    }

    override fun load(): Credentials? = stored

    override fun delete() {
        deleteCallCount++
        stored = null
    }
}
