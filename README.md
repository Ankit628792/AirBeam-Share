# 📡 AirBeam Share

> **Zero-Network Air-Gapped Optical File Sharing for Android**  
> *Transfer files, photos, datasets, and encrypted notes at high speeds through animated optical streams without Wi-Fi, Bluetooth, cellular data, or internet connectivity.*

---

## 🌟 Overview

**AirBeam Share** is an offline, security-first Android application designed for high-throughput **air-gapped data transfers**. Using dynamic, animated high-density QR code series and an advanced frame assembly engine, AirBeam transmits files directly from screen to camera.

By eliminating network radios (Wi-Fi, Bluetooth, NFC, Local Area Network, Cloud servers), AirBeam provides complete immunity against network interception, packet sniffing, and radio tracking.

---

## 🚀 Key Features

- ⚡ **High-Speed Turbo Transfer Modes (10–50 Mbps)**: Optimized chunking, memory pipelines, and automated compression enable rapid multi-megabyte transfers.
- 🗜️ **Integrated GZIP Compression**: Automatically compresses payloads before streaming, multiplying effective bandwidth up to 3x on structured data.
- 🛡️ **Military-Grade AES-256-GCM Encryption**: Optional end-to-end passphrase encryption with authenticated GCM tags and PBKDF2 key derivation.
- 📊 **Real-Time Visual Chunk HUD**: Live matrix visualizer illustrating individual frame receipt, missing chunk tracking, transmission speed (Mbps/FPS), and completion status.
- 📷 **CameraX & ZXing Optical Scanner**: Hardware-accelerated camera scanning pipeline with continuous frame decoding and out-of-order chunk assembly.
- 📦 **Multi-File Batching & Preset Datasets**: Send single files, multi-file batches, plain notes, or built-in high-speed testing payloads (e.g., 4.2 MB test files).
- 🗄️ **Local Room Database & Vault**: Persistent local storage of received files with category filtering (Documents, Images, Media, Binaries), instant text preview, and clipboard integration.
- 🎨 **Neumorphic Soft UI Design**: Polished, tactile interface built entirely with Jetpack Compose and Material 3 principles.

---

## 🏗️ Tech Stack

| Layer | Technologies |
|---|---|
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose, Material 3, Neumorphic Custom Canvas Modifiers |
| **Architecture** | MVVM (Model-View-ViewModel), Clean Architecture, StateFlow, Coroutines |
| **Camera & QR** | AndroidX CameraX (Core, Camera2, Lifecycle, View), ZXing (`com.google.zxing:core`) |
| **Persistence** | AndroidX Room (KSP Compiler, SQLite ORM, TypeConverters) |
| **Cryptography** | `javax.crypto` (AES/GCM/NoPadding, PBKDF2WithHmacSHA256, SecureRandom, CRC32) |
| **Compression** | Java `GZIPInputStream` / `GZIPOutputStream` |

---

## 📚 Documentation Index

For detailed documentation, refer to the following companion guides:

- 📖 **[INFO.md](./INFO.md)**: Deep architectural overview, protocol V2 packet structure, cryptographic security design, and performance benchmarks.
- 🛠️ **[SETUP.md](./SETUP.md)**: Development environment requirements, local build instructions, Gradle configurations, and troubleshooting.
- 📱 **[USAGE.md](./USAGE.md)**: Step-by-step user guide for sending files, scanning streams, decrypting content, and managing the local vault.

---

## 🔒 Security Guarantee

```
  [Sender Device]                                     [Receiver Device]
 +------------------+                                +------------------+
 | 1. Raw Payload   |                                | 6. Reassembled   |
 | 2. AES-256-GCM   |                                | 5. AES Decrypt   |
 | 3. GZIP Compress |                                | 4. GZIP Inflate  |
 | 4. CRC32 + Chunk |                                | 3. CRC32 Check   |
 | 5. QR Code Stream| ===== Optical Light Stream ===>| 2. Camera Sensor |
 |    (Screen)      |       (Zero Network Radios)    | 1. Frame Parser  |
 +------------------+                                +------------------+
```

- **0% Wi-Fi usage**
- **0% Bluetooth usage**
- **0% Cellular data**
- **0% Cloud reliance**
