CLAUDE.md has been written. It covers:

- **What it does**: OAuth 2.0 + PKCE / OIDC SDK for ID.me verification on Android
- **Tech stack**: exact dependency versions, AGP/Kotlin/SDK versions
- **Architecture**: module layout, table of key files with one-line roles
- **Build/test/deploy**: all `./gradlew` commands + release workflow
- **Secrets**: `local.properties` keys, GitHub Actions secrets, runtime config shape
- **11 gotchas**: groups-only-in-production, OAUTH_PKCE vs OIDC data differences, manifest redirect re-declaration, concurrent refresh coalescing, flexible boolean, scope separator difference, state mismatch enforcement, token expiry cap, redirect scheme whitelist, no-op logger, and nonce instance scope
