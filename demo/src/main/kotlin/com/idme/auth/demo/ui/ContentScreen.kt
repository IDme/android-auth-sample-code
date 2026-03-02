package com.idme.auth.demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.demo.AuthViewModel

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
fun ContentScreen(viewModel: AuthViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val payloadTabLabel = if (viewModel.authMode == IDmeAuthMode.OIDC) "UserInfo" else "Attributes"

    val tabs = listOf(
        TabItem("Login", Icons.Default.Person),
        TabItem("Token Exchange", Icons.Default.Lock),
        TabItem(payloadTabLabel, Icons.Default.AccountCircle),
        TabItem("Settings", Icons.Default.Settings)
    )

    // Error dialog
    val errorMessage = viewModel.errorMessage
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        when (selectedTab) {
            0 -> LoginScreen(viewModel, modifier)
            1 -> CredentialsScreen(viewModel, modifier)
            2 -> UserInfoScreen(viewModel, modifier)
            3 -> SettingsScreen(viewModel, modifier)
        }
    }
}
