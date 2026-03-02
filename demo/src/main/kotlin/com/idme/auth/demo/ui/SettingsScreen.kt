package com.idme.auth.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idme.auth.IDmeAuthSDK
import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.configuration.IDmeEnvironment
import com.idme.auth.demo.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout") },
            text = { Text("This will clear all stored credentials. You'll need to verify again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutConfirmation = false
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        // Authentication Mode
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Authentication Mode",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.authMode == IDmeAuthMode.OAUTH_PKCE,
                        onClick = { viewModel.authMode = IDmeAuthMode.OAUTH_PKCE },
                        label = { Text("OAuth + PKCE") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = viewModel.authMode == IDmeAuthMode.OIDC,
                        onClick = { viewModel.authMode = IDmeAuthMode.OIDC },
                        label = { Text("OIDC") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    if (viewModel.authMode == IDmeAuthMode.OIDC)
                        "OpenID Connect. Returns an ID token with identity claims."
                    else
                        "OAuth 2.0 with PKCE challenge. Recommended for mobile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Environment
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Environment",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.environment == IDmeEnvironment.PRODUCTION,
                        onClick = { viewModel.updateEnvironment(IDmeEnvironment.PRODUCTION) },
                        label = { Text("Production") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = viewModel.environment == IDmeEnvironment.SANDBOX,
                        onClick = { viewModel.updateEnvironment(IDmeEnvironment.SANDBOX) },
                        label = { Text("Sandbox") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    if (viewModel.environment == IDmeEnvironment.SANDBOX)
                        "Uses api.idmelabs.com. Groups verification is not available."
                    else
                        "Uses api.id.me. All features available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // SDK Info
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SDK Version")
                Text(IDmeAuthSDK.version)
            }
        }

        // Logout
        if (viewModel.isAuthenticated) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showLogoutConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
