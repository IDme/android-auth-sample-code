# Releasing to Maven Central

This document covers how to create and publish a new release of the ID.me Auth Sample Code SDK to Maven Central and GitHub Packages.

## Overview

The release process is fully automated via the [`release.yml`](../.github/workflows/release.yml) GitHub Actions workflow. A single manual trigger:

1. Validates the version and checks for tag conflicts
2. Builds the AAR and all Maven artifacts
3. Generates SLSA build provenance attestations
4. Publishes to both GitHub Packages and Maven Central (Sonatype OSSRH)
5. Creates and pushes a git tag
6. Creates a GitHub Release with the AAR and attestation bundle

**Maven coordinates:** `com.idmelabs.auth:android-auth-sample-code:<version>`

---

## Prerequisites

Before triggering a release, confirm the following are in place.

### GitHub Secrets

All secrets must be configured under the `release` GitHub Actions environment (not the default repository secrets). Navigate to **Settings → Environments → release** to manage them.

| Secret | Description |
|---|---|
| `SONATYPE_USERNAME` | Sonatype Central Portal account username |
| `SONATYPE_PASSWORD` | Sonatype Central Portal account password or token |
| `SIGNING_KEY_ID` | GPG key ID (last 8 characters of the key fingerprint) |
| `SIGNING_KEY` | GPG private key in ASCII-armored format |
| `SIGNING_PASSWORD` | Passphrase for the GPG key |

`GITHUB_TOKEN` is provided automatically by GitHub Actions and does not need to be configured manually.

### GPG Key Setup

If a GPG key has not been created yet:

```bash
# Generate a new GPG key (use RSA 4096, no expiry)
gpg --full-generate-key

# List keys to find your key ID
gpg --list-secret-keys --keyid-format LONG

# Export the private key for the SIGNING_KEY secret
gpg --armor --export-secret-keys <KEY_ID>

# Publish the public key to a keyserver so Maven Central can verify signatures
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

The `SIGNING_KEY_ID` is the **last 8 characters** of the full fingerprint shown by `--list-secret-keys`.

### Sonatype Account

Publishing to Maven Central requires a Sonatype account with publishing rights for the `com.idmelabs.auth` namespace. If you are setting this up for the first time, refer to the [Sonatype Central Portal documentation](https://central.sonatype.org/register/central-portal/) to register and claim the namespace.

---

## Triggering a Release

1. Go to the **Actions** tab of the repository on GitHub.
2. Select the **Release Auth Sample** workflow from the left sidebar.
3. Click **Run workflow**.
4. Fill in the inputs:
   - **SDK Version** (required): The semantic version to release, e.g. `1.2.0` or `1.2.0-beta.1`.
   - **Make draft release** (optional): Check this box to create a draft GitHub Release (useful for review before publishing).
5. Click **Run workflow** to start the job.

### Version Format

Versions must follow semantic versioning: `MAJOR.MINOR.PATCH` or `MAJOR.MINOR.PATCH-PRERELEASE`.

Valid examples:
- `1.0.0`
- `2.1.3`
- `1.0.0-beta.1`
- `1.0.0-rc.2`

The workflow will fail early if the version does not match this format or if the tag `v<version>` already exists.

---

## What the Workflow Does

### Step-by-Step

1. **Checkout** — Full git history is fetched (`fetch-depth: 0`) to support tagging.
2. **Environment setup** — JDK 17 (Temurin), Android SDK, and Gradle are configured.
3. **Version validation** — The version string is validated against the semver regex and checked for existing tags.
4. **Build release AAR** — Runs `./gradlew :sdk:assembleRelease -PreleaseVersion=<version>`. Output: `sdk/build/outputs/aar/sdk-release.aar`.
5. **Build Maven artifacts locally** — Runs `./gradlew :sdk:publishReleasePublicationToMavenLocalRepository -PreleaseVersion=<version>`. Produces POM, AAR, sources JAR, and Dokka javadoc JAR in `~/.m2`.
6. **Generate attestations** — SLSA build provenance is generated for both the release AAR and all Maven artifacts using `actions/attest-build-provenance`.
7. **Publish to GitHub Packages** — All Maven artifacts (POM, AAR, module metadata, attestation bundle) are uploaded via the GitHub Packages Maven registry API.
8. **Publish to Maven Central** — Runs:
   ```
   ./gradlew :sdk:publishReleasePublicationToSonatypeRepository \
     closeAndReleaseSonatypeStagingRepository \
     -PreleaseVersion=<version>
   ```
   This publishes signed artifacts to Sonatype staging and then closes and releases the staging repository to Maven Central.
9. **Create git tag** — Tags the current commit as `v<version>` and pushes to `origin`.
10. **Create GitHub Release** — A release is created with the AAR and SLSA attestation bundle attached.

### Published Artifacts

For version `1.2.0`, the following files are published:

```
com/idmelabs/auth/android-auth-sample-code/1.2.0/
├── android-auth-sample-code-1.2.0.aar
├── android-auth-sample-code-1.2.0.pom
├── android-auth-sample-code-1.2.0-sources.jar
├── android-auth-sample-code-1.2.0-javadoc.jar
├── android-auth-sample-code-1.2.0.module
└── android-auth-sample-code-1.2.0.intoto.jsonl  (attestation bundle)
```

All artifacts are signed with the configured GPG key.

---

## Verifying a Release

### Maven Central

After the workflow completes, the release is available at:

```
https://central.sonatype.com/artifact/com.idmelabs.auth/android-auth-sample-code
```

Maven Central propagation typically takes 10–30 minutes after the staging repository is released.

### GitHub Packages

Artifacts are immediately available at:

```
https://github.com/IDme/android-auth-sample-code/packages
```

### SLSA Attestation

To verify the build provenance of a downloaded artifact:

```bash
gh attestation verify android-auth-sample-code-<version>.aar --repo IDme/android-auth-sample-code
```

---

## Troubleshooting

### Tag already exists

The workflow checks for existing tags before proceeding. If the tag `v<version>` already exists, the workflow will fail with:

```
Tag v<version> already exists!
```

Either choose a different version or, if the previous release failed partway through, delete the existing tag manually:

```bash
git push --delete origin v<version>
git tag -d v<version>
```

### Sonatype staging repository not closed

If the `closeAndReleaseSonatypeStagingRepository` task fails, check the [Sonatype Central Portal](https://central.sonatype.com/) for the status of the staging repository. Common causes:

- GPG signature validation failed (key not published to a keyserver)
- Missing required POM fields
- Artifact checksums mismatch

### Signing failures

If the build fails with a PGP signing error, verify that:

- `SIGNING_KEY` contains the full ASCII-armored private key, including the `-----BEGIN PGP PRIVATE KEY BLOCK-----` header and footer
- `SIGNING_KEY_ID` matches the key exported in `SIGNING_KEY`
- `SIGNING_PASSWORD` is the correct passphrase for the key

---

## Local Publishing (for testing)

To test the build locally without publishing to any remote registry:

```bash
# Build the AAR
./gradlew :sdk:assembleRelease -PreleaseVersion=1.0.0-SNAPSHOT

# Publish to local Maven repository (~/.m2)
./gradlew :sdk:publishReleasePublicationToMavenLocalRepository -PreleaseVersion=1.0.0-SNAPSHOT
```

To consume the locally published artifact in another project, add `mavenLocal()` to the project's repository list:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}
```
