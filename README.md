# IDmeAuthSDK for Android

A native Android SDK for integrating [ID.me](https://id.me) identity verification into your app. Supports OAuth 2.0 + PKCE and OpenID Connect (OIDC) flows with built-in token management, encrypted storage, and JWT validation.

## Requirements

- Android API 26+ (Android 8.0)
- Kotlin 2.1+
- Android Studio Ladybug or later

## Installation

### Gradle

Add the SDK module as a dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":sdk"))
}
```

Or if published to a Maven repository:

```kotlin
dependencies {
    implementation("com.idme:auth-sdk:1.0.0")
}
```

## Quick Start

### 1. Configure the SDK

```kotlin
import com.idme.auth.IDmeAuth
import com.idme.auth.configuration.*

val config = IDmeConfiguration(
    clientId = "YOUR_CLIENT_ID",
    redirectURI = "yourapp://idme/callback",
    scopes = listOf(IDmeScope.MILITARY),
    environment = IDmeEnvironment.PRODUCTION,
    authMode = IDmeAuthMode.OAUTH_PKCE,
    clientSecret = "YOUR_CLIENT_SECRET"
)

val idme = IDmeAuth(config)
```

### 2. Register Your Redirect Scheme

In your app's `AndroidManifest.xml`, register the redirect Activity with an intent-filter matching your redirect URI:

```xml
<activity
    android:name="com.idme.auth.auth.IDmeRedirectActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:theme="@android:style/Theme.Translucent.NoTitleBar">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="yourapp"
            android:host="idme"
            android:path="/callback" />
    </intent-filter>
</activity>
```

Or use a manifest placeholder in `build.gradle.kts`:

```kotlin
defaultConfig {
    manifestPlaceholders["idmeRedirectScheme"] = "yourapp"
}
```

### 3. Start the Login Flow

```kotlin
// From an Activity or using Activity context
val credentials = idme.login(activity)

// Access tokens
println(credentials.accessToken)
println(credentials.refreshToken)
println(credentials.expiresAt)
```

### 4. Retrieve User Info

```kotlin
// OIDC -- returns structured UserInfo
val userInfo = idme.userInfo()
println(userInfo.email)

// OAuth -- returns attributes and verification statuses
val attributes = idme.attributes()
for (attr in attributes.attributes) {
    println("${attr.handle}: ${attr.value}")
}

// Raw JWT payload as key-value pairs
val claims = idme.rawPayload()
for ((key, value) in claims) {
    println("$key: $value")
}
```

### 5. Token Management

The SDK stores credentials and provides token refresh:

```kotlin
// Get valid credentials, refreshing if needed
val creds = idme.credentials(minTTL = 60)

// Check expiry
if (creds.isExpired) {
    // Token has expired
}

if (creds.expiresWithin(300)) {
    // Token expires within 5 minutes
}
```

### 6. Fetch Available Policies

Discover which verification policies your organization supports:

```kotlin
val policies = idme.policies()
for (policy in policies.filter { it.active }) {
    println("${policy.name} -- scope: ${policy.handle}")
}
```

### 7. Logout

```kotlin
idme.logout()
```

## Configuration Reference

### `IDmeConfiguration`

| Parameter | Type | Default | Description |
|---|---|---|---|
| `clientId` | `String` | -- | OAuth client ID from ID.me |
| `redirectURI` | `String` | -- | Registered redirect URI |
| `scopes` | `List<IDmeScope>` | -- | Verification scopes to request |
| `environment` | `IDmeEnvironment` | `PRODUCTION` | `PRODUCTION` or `SANDBOX` |
| `authMode` | `IDmeAuthMode` | `OAUTH_PKCE` | `OAUTH`, `OAUTH_PKCE`, or `OIDC` |
| `verificationType` | `IDmeVerificationType` | `SINGLE` | `SINGLE` or `GROUPS` |
| `clientSecret` | `String?` | `null` | Required for `OAUTH` mode |

### Auth Modes

| Mode | Description |
|---|---|
| `OAUTH_PKCE` | **Recommended.** OAuth 2.0 Authorization Code with PKCE. No client secret sent to authorize endpoint. |
| `OAUTH` | Standard OAuth 2.0 Authorization Code. Requires `clientSecret`. |
| `OIDC` | OpenID Connect. Returns an ID token with JWT signature validation against ID.me's JWKS. |

### Verification Types

| Type | Description |
|---|---|
| `SINGLE` | Single policy -- directs to `/oauth/authorize`. Use for a single verification community. |
| `GROUPS` | Multiple policies -- directs to `groups.id.me`. User selects their community. **Production only.** |

### Scopes

| Scope | Raw Value |
|---|---|
| `OPENID` | `openid` |
| `PROFILE` | `profile` |
| `EMAIL` | `email` |
| `MILITARY` | `military` |
| `FIRST_RESPONDER` | `first_responder` |
| `NURSE` | `nurse` |
| `TEACHER` | `teacher` |
| `STUDENT` | `student` |
| `GOVERNMENT_EMPLOYEE` | `government` |
| `LOW_INCOME` | `low_income` |

## Error Handling

All errors are thrown as `IDmeAuthError`, a sealed class hierarchy:

```kotlin
try {
    val creds = idme.login(activity)
} catch (e: IDmeAuthError.UserCancelled) {
    // User dismissed the auth sheet
} catch (e: IDmeAuthError.TokenExchangeFailed) {
    println("Token exchange failed (${e.statusCode}): ${e.errorMessage}")
} catch (e: IDmeAuthError.NotAuthenticated) {
    println("No credentials available")
} catch (e: IDmeAuthError) {
    println(e.message)
}
```

## Architecture

- **`IDmeAuth`** -- Main facade. Manages the full auth lifecycle.
- **`TokenManager`** -- Coroutine-safe token storage and refresh with request coalescing.
- **`CredentialStore`** / **`EncryptedCredentialStore`** -- Credential persistence (in-memory or EncryptedSharedPreferences).
- **`JWTValidator`** -- RS256 signature verification against ID.me's JWKS endpoint (OIDC mode).
- **`IDmeAuthManager`** -- Bridges Chrome Custom Tab redirects with Kotlin coroutines.
- **Interface-based DI** -- All network, storage, and auth dependencies are interface-based for testability.

## Demo App

The `demo/` module contains a full Jetpack Compose demo app that showcases all SDK features:

- OAuth + PKCE and OIDC authentication flows
- Single and multi-policy (groups) verification
- Dynamic policy discovery from the `/api/public/v3/policies` endpoint
- Token display, refresh, and expiry monitoring
- Raw JWT payload inspection
- Environment switching (production/sandbox)

### Running the Demo

1. Open the project in Android Studio
2. Select the `demo` run configuration
3. Run on an API 26+ emulator or device
4. Select verification policies and tap "Authenticate"

> The demo uses pre-configured ID.me test credentials. To use your own, update the `clientId`, `clientSecret`, and `redirectURI` in `AuthViewModel.kt`.

## Project Structure

```
digital-wallet-android-sdk/
├── build.gradle.kts              (Root Gradle config)
├── settings.gradle.kts           (Module definitions)
├── sdk/                          (SDK library module)
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/idme/auth/
│       │   ├── IDmeAuthSDK.kt           (Version namespace)
│       │   ├── IDmeAuth.kt              (Main facade)
│       │   ├── auth/                    (Auth flow)
│       │   ├── configuration/           (Config models)
│       │   ├── models/                  (Data models)
│       │   ├── token/                   (Token management)
│       │   ├── jwt/                     (JWT validation)
│       │   ├── storage/                 (Credential persistence)
│       │   ├── networking/              (HTTP abstraction)
│       │   ├── errors/                  (Error types)
│       │   └── utilities/               (Helpers)
│       └── test/kotlin/com/idme/auth/   (Unit tests)
└── demo/                         (Demo app module)
    ├── build.gradle.kts
    └── src/main/kotlin/com/idme/auth/demo/
        ├── IDmeAuthDemoActivity.kt
        ├── AuthViewModel.kt
        └── ui/                          (Compose screens)
```

## License

Copyright (c) 2025 ID.me, Inc. All rights reserved.
