# 🛠️ Setup & Development Guide (SETUP.md)

This guide walks through configuring your development environment, building AirBeam Share from source, executing local tests, and resolving common issues.

---

## 1. Prerequisites & System Requirements

Before setting up the project, ensure you have the following software installed:

- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
- **Java Development Kit (JDK)**: JDK 17 or JDK 21 (recommended: Amazon Corretto or Eclipse Temurin).
- **Android SDK**:
  - `compileSdk`: 36 (Android 16 preview / API 36)
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `targetSdk`: 36
- **Gradle**: Gradle 8.9+ with Kotlin DSL support.
- **Android Build Tools & Platform Tools**: Installed via Android Studio SDK Manager.

---

## 2. Project Clone & Repository Structure

```bash
# Clone the repository
git clone <repository-url> airbeam-share
cd airbeam-share
```

### Directory Structure Overview

```
airbeam-share/
├── app/
│   ├── build.gradle.kts            # App module build dependencies and configurations
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml # Permissions (CAMERA) and application declarations
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/           # Room Database, DAO, Entities, Converters
│   │   │   │   ├── ui/             # Jetpack Compose Screens, Components, Theme
│   │   │   │   ├── util/           # Protocol, Crypto, File Storage, QR Generators
│   │   │   │   └── viewmodel/      # AirBeamViewModel & State Definitions
│   │   │   └── res/                # XML Resources, Drawables, M3 Color Values
│   │   └── test/                   # Local JVM Unit, Robolectric & Roborazzi tests
├── gradle/
│   └── libs.versions.toml          # Gradle Version Catalog (dependencies and plugins)
├── build.gradle.kts                # Root project build configuration
├── settings.gradle.kts             # Module inclusion & repository definitions
├── metadata.json                   # AI Studio application metadata
├── README.md                       # Project overview
├── INFO.md                         # Protocol and architecture specification
├── SETUP.md                        # Setup and build instructions
└── USAGE.md                        # User and operational guide
```

---

## 3. Building the Project

### Using Gradle Command Line

From the root project directory:

```bash
# Verify and assemble debug APK
gradle :app:assembleDebug

# Run unit tests on local JVM
gradle :app:testDebugUnitTest

# Clean build artifacts (use sparingly)
gradle clean
```

The generated debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 4. Permissions & Hardware Configuration

AirBeam Share requires only one runtime hardware permission:

### Camera (`android.permission.CAMERA`)
- **Purpose**: Used exclusively for capturing video frames from the device camera to decode incoming animated QR code streams in real-time.
- **Privacy Guarantee**: No video streams or captured images are recorded to disk or transmitted to any remote servers. Frame processing happens entirely in volatile memory via CameraX ImageAnalysis and ZXing.

Declared in `app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

## 5. Testing & Verification

AirBeam includes local JVM unit and integration tests using **Robolectric** and **Roborazzi**:

```bash
# Run all local JVM unit tests
gradle :app:testDebugUnitTest

# Verify UI screenshot tests (if configured with Roborazzi)
gradle :app:verifyRoborazziDebug
```

---

## 6. Troubleshooting Common Issues

### Issue 1: Camera Preview Not Working on Emulator
- **Cause**: The Android Emulator camera might not be configured.
- **Solution**: Open Android Emulator Settings → **Camera** → Set **Back Camera** to **Webcam0** (your computer's webcam) or **VirtualScene**. Alternatively, test using the built-in **Beam Simulation Mode** on the Send/Receive screens.

### Issue 2: Compilation error regarding Kotlin Symbol Processing (KSP)
- **Cause**: Version mismatch between Kotlin compiler and KSP plugin.
- **Solution**: Check `gradle/libs.versions.toml` to ensure the `ksp` plugin version aligns with the `kotlin` version.

### Issue 3: Room Migration Warnings
- **Cause**: Modifying entity schemas without updating schema version or providing fallback destructive migration.
- **Solution**: `AppDatabase.kt` includes `.fallbackToDestructiveMigration()` during development mode to ensure non-breaking database initialization.
