package com.idme.auth.demo

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idme.auth.demo.BuildConfig
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

private val STANDARD_POLICIES = listOf(
    Policy(name = "Login", handle = "login", active = true, groups = emptyList()),
    Policy(name = "NIST AAL2 / IAL2", handle = "http://idmanagement.gov/ns/assurance/ial/2/aal/2", active = true, groups = emptyList()),
    Policy(name = "Military", handle = "military", active = true, groups = emptyList())
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // MARK: - Configuration Inputs

    var selectedPolicies by mutableStateOf(setOf<String>())
        private set

    var authMode by mutableStateOf(IDmeAuthMode.OAUTH_PKCE)
    var environment by mutableStateOf(IDmeEnvironment.SANDBOX)
        private set

    var verificationType by mutableStateOf(IDmeVerificationType.SINGLE)

    // MARK: - State

    val policies: List<Policy> = STANDARD_POLICIES

    var credentials by mutableStateOf<Credentials?>(null)
        private set

    var payloadClaims by mutableStateOf(listOf<Pair<String, String>>())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    val hasPayload: Boolean get() = payloadClaims.isNotEmpty()
    val isAuthenticated: Boolean get() = credentials != null

    // MARK: - Credentials

    private val redirectURI = "idmedemo://idme/callback"

    private val clientId: String
        get() = when (environment) {
            IDmeEnvironment.PRODUCTION -> "YOUR_PRODUCTION_CLIENT_ID"
            IDmeEnvironment.SANDBOX -> BuildConfig.SANDBOX_CLIENT_ID
        }

    private val clientSecret: String
        get() = when (environment) {
            IDmeEnvironment.PRODUCTION -> "YOUR_PRODUCTION_CLIENT_SECRET"
            IDmeEnvironment.SANDBOX -> BuildConfig.SANDBOX_CLIENT_SECRET
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
        environment = env
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

        val secret = clientSecret.ifBlank { null }

        val config = IDmeConfiguration(
            clientId = clientId,
            redirectURI = redirectURI,
            scopes = finalScopes,
            environment = environment,
            authMode = authMode,
            verificationType = verificationType,
            clientSecret = secret
        )

        return IDmeAuth(config, getApplication())
    }
}
