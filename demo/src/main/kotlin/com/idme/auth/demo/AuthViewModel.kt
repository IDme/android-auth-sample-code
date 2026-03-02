package com.idme.auth.demo

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idme.auth.IDmeAuth
import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.configuration.IDmeEnvironment
import com.idme.auth.configuration.IDmeScope
import com.idme.auth.configuration.IDmeVerificationType
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.models.Credentials
import com.idme.auth.models.Policy
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // MARK: - Configuration Inputs

    var selectedPolicies by mutableStateOf(setOf<String>())
        private set

    var authMode by mutableStateOf(IDmeAuthMode.OAUTH_PKCE)
    var environment by mutableStateOf(IDmeEnvironment.PRODUCTION)
        private set

    var verificationType by mutableStateOf(IDmeVerificationType.SINGLE)

    // MARK: - State

    var policies by mutableStateOf(listOf<Policy>())
        private set

    var credentials by mutableStateOf<Credentials?>(null)
        private set

    var payloadClaims by mutableStateOf(listOf<Pair<String, String>>())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingPolicies by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    val hasPayload: Boolean get() = payloadClaims.isNotEmpty()
    val isAuthenticated: Boolean get() = credentials != null

    // MARK: - Credentials

    private val redirectURI = "idmedemo://idme/callback"

    private val clientId: String
        get() = when (environment) {
            IDmeEnvironment.PRODUCTION -> "YOUR_PRODUCTION_CLIENT_ID"
            IDmeEnvironment.SANDBOX -> "YOUR_SANDBOX_CLIENT_ID"
        }

    private val clientSecret: String
        get() = when (environment) {
            IDmeEnvironment.PRODUCTION -> "YOUR_PRODUCTION_CLIENT_SECRET"
            IDmeEnvironment.SANDBOX -> "YOUR_SANDBOX_CLIENT_SECRET"
        }

    // MARK: - Private

    private var idmeAuth: IDmeAuth? = null

    // MARK: - Policy selection

    fun togglePolicy(handle: String) {
        selectedPolicies = if (handle in selectedPolicies) {
            selectedPolicies - handle
        } else {
            selectedPolicies + handle
        }
    }

    fun updateEnvironment(env: IDmeEnvironment) {
        if (env != environment) {
            environment = env
            viewModelScope.launch { fetchPolicies() }
        }
    }

    // MARK: - Policies

    fun fetchPolicies() {
        viewModelScope.launch {
            isLoadingPolicies = true

            try {
                val auth = buildAuth(listOf(IDmeScope.MILITARY))
                val fetched = auth.policies()
                policies = fetched.filter { it.active }
                // Clear selections that no longer exist
                val validHandles = policies.map { it.handle }.toSet()
                selectedPolicies = selectedPolicies.intersect(validHandles)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to fetch policies", e)
                policies = emptyList()
            }

            isLoadingPolicies = false
        }
    }

    // MARK: - Actions

    fun login(activity: Activity) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            credentials = null
            payloadClaims = emptyList()

            try {
                val scopes = selectedPolicies.mapNotNull { IDmeScope.fromValue(it) }
                val auth = buildAuth(scopes)
                idmeAuth = auth
                val creds = auth.login(activity)
                credentials = creds
            } catch (e: IDmeAuthError.UserCancelled) {
                // User dismissed -- not an error to display
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
            }

            isLoading = false
        }
    }

    fun refreshCredentials() {
        viewModelScope.launch {
            val auth = idmeAuth
            if (auth == null) {
                errorMessage = "Not authenticated"
                return@launch
            }

            isLoading = true
            errorMessage = null

            try {
                credentials = auth.credentials(minTTL = 0)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
            }

            isLoading = false
        }
    }

    fun fetchPayload() {
        viewModelScope.launch {
            val auth = idmeAuth
            if (auth == null) {
                errorMessage = "Not authenticated"
                return@launch
            }

            isLoading = true
            errorMessage = null

            try {
                payloadClaims = auth.rawPayload()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
            }

            isLoading = false
        }
    }

    fun logout() {
        idmeAuth?.logout()
        idmeAuth = null
        credentials = null
        payloadClaims = emptyList()
        errorMessage = null
    }

    // MARK: - Private Helpers

    private fun buildAuth(scopes: List<IDmeScope>): IDmeAuth {
        val finalScopes = scopes.toMutableList()

        // OIDC mode needs the openid scope
        if (authMode == IDmeAuthMode.OIDC && IDmeScope.OPENID !in finalScopes) {
            finalScopes.add(0, IDmeScope.OPENID)
        }

        val config = IDmeConfiguration(
            clientId = clientId,
            redirectURI = redirectURI,
            scopes = finalScopes,
            environment = environment,
            authMode = authMode,
            verificationType = verificationType,
            clientSecret = clientSecret
        )

        return IDmeAuth(config)
    }
}
