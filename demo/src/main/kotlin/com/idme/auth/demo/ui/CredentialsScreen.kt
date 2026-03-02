package com.idme.auth.demo.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.idme.auth.demo.AuthViewModel
import com.idme.auth.models.Credentials
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CredentialsScreen(viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val creds = viewModel.credentials

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Token Exchange",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (creds != null) {
            CredentialsList(creds, viewModel)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No Credentials",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Log in on the Login tab to view your credentials.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialsList(creds: Credentials, viewModel: AuthViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TokenSection("Access Token", creds.accessToken)
        }

        creds.refreshToken?.let { refreshToken ->
            item {
                TokenSection("Refresh Token", refreshToken)
            }
        }

        creds.idToken?.let { idToken ->
            item {
                TokenSection("ID Token (OIDC)", idToken)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Token Info",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoRow("Type", creds.tokenType)
                    ExpiryRow(creds)
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.refreshCredentials() },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Refresh Now")
            }
        }
    }
}

@Composable
private fun TokenSection(title: String, token: String) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Token", token))
                copied = true
            }
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    truncateToken(token),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                CopiedBadge(visible = copied)
            }
        }
    }
}

@Composable
private fun CopiedBadge(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            color = Color(0xFF4CAF50),
            shape = CircleShape
        ) {
            Text(
                "Copied!",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
private fun ExpiryRow(creds: Credentials) {
    val remaining = creds.expiresAt - System.currentTimeMillis()
    val color = when {
        creds.isExpired -> Color.Red
        remaining < 300_000 -> Color(0xFFFF9800) // Orange/yellow
        else -> Color(0xFF4CAF50) // Green
    }

    val relativeTime = DateUtils.getRelativeTimeSpanString(
        creds.expiresAt,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS
    ).toString()

    val absoluteTime = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        .format(Date(creds.expiresAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Expires", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(horizontalAlignment = Alignment.End) {
            Text(relativeTime, color = color)
            Text(
                absoluteTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun truncateToken(token: String): String {
    return if (token.length > 40) {
        token.take(20) + "..." + token.takeLast(10)
    } else {
        token
    }
}
