# Verbinal for Android

A native Android companion for the [CANFAR Science Portal](https://www.canfar.net/), built with Kotlin, Jetpack Compose, and Material 3.

This is the Android counterpart of [Verbinal for Windows](https://github.com/szautkin/CanfarDesktop) (C#/WinUI 3) and [Verbinal for Linux](https://github.com/szautkin/CanfarDesktopUbuntu) (Rust/GTK 4).

[![CI](https://github.com/szautkin/canfar-android/actions/workflows/ci.yml/badge.svg)](https://github.com/szautkin/canfar-android/actions/workflows/ci.yml)
[![License: AGPL-3.0](https://img.shields.io/badge/license-AGPL--3.0-blue)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API 31+](https://img.shields.io/badge/API-31%2B-brightgreen.svg)](https://developer.android.com/about/versions/12)

## Features

- **Session Management** — Launch, monitor, extend, and delete CANFAR science sessions (Notebook, Desktop, CARTA, Firefly)
- **Session Details** — View session events and logs in a tabbed dialog
- **Storage Quota** — View VOSpace home directory usage at a glance
- **Platform Load** — Real-time cluster CPU and RAM utilisation
- **Recent Launches** — Quick re-launch from session history
- **Standard & Advanced Launch** — Pick from the CANFAR image catalogue or supply a custom registry image with auth credentials
- **Auto-Refresh** — Active sessions poll automatically while any session is pending
- **Secure Credentials** — Tokens stored in Android EncryptedSharedPreferences with optional "Remember me"

## Screenshot

*(coming soon)*

## Requirements

- Android 12+ (API 31)
- A [CADC](https://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/) account

## Building

```bash
# Clone the repository
git clone https://github.com/szautkin/canfar-android.git
cd canfar-android

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

## Code Quality

```bash
# Check formatting (ktlint via Spotless)
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Run Android lint
./gradlew lint
```

## Project Structure

```
app/src/main/java/net/canfar/verbinal/
  data/
    model/        # Data classes (Session, Image, UserInfo, etc.)
    remote/       # Retrofit API interfaces (Skaha, Auth, Storage)
    repository/   # Repository layer
  di/             # Hilt dependency injection modules
  ui/
    dashboard/    # Main dashboard with session, storage, platform widgets
      launch/     # Session launch form
      platform/   # Platform load widget
      recent/     # Recent launches widget
      sessions/   # Active sessions list & card
      storage/    # Storage quota widget
    login/        # Authentication screen
    navigation/   # NavHost setup
    theme/        # Material 3 theming
    components/   # Shared UI components
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt dependency injection
- **Networking:** Retrofit + OkHttp + Kotlin Serialization
- **Code Quality:** Spotless + ktlint + Android Lint
- **CI:** GitHub Actions

## API Endpoints

All communication is with CANFAR services over HTTPS. No telemetry, analytics, or third-party calls.

| Service | Base URL | Purpose |
|---------|----------|---------|
| Auth | `ws-cadc.canfar.net/ac` | Login, token validation, user info |
| Sessions | `ws-uv.canfar.net/skaha/v1` | Session CRUD, images, context, stats |
| Storage | `ws-uv.canfar.net/arc` | VOSpace quota |

## License

[GNU Affero General Public License v3.0](LICENSE)

Copyright (C) 2025 Serhii Zautkin

## Privacy

See [PRIVACY.md](PRIVACY.md). In short: no data collection, no telemetry, no third-party services. All data stays on your device or goes directly to CANFAR.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
