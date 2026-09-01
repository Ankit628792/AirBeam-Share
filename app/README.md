# AirBeam ⚡ — Optical Air-Gap P2P File Transfer & Vault

**AirBeam** is a 100% offline, zero-network peer-to-peer Android application designed for ultra-secure file transfer and management. By encoding files into a high-density stream of optical QR code frames, AirBeam transfers data across physical space directly into another device's camera — bypassing the internet, Wi-Fi, cellular, and Bluetooth entirely.

---

## 🛡️ Key Features & Capabilities

### 🔒 End-to-End Encryption (E2E)
- **AES-256-GCM Cryptography**: Payload chunks are encrypted on the sender's device before optical rendering using AES-256 in Galois/Counter Mode (GCM).
- **PBKDF2 Key Derivation**: High-iteration key derivation from user passphrases or automatically generated 6-digit PINs.
- **Zero-Knowledge Air-Gap**: The receiver requires the passphrase/PIN to decrypt the payload once all optical frames are collected.

### 📱 Categorized File Management System
- **Categorization**: Auto-classifies shared files into **Photos & Graphics**, **Documents & Notes**, **Contacts**, and **Other Files**.
- **Search & Filtering**: Real-time keyword filtering across filenames and preview text content.
- **Local Vault Storage**: Files are saved securely in app storage with full local disk management and file deletion.
- **Detail View & Text Copy**: Instant monospace content preview and one-tap copy for text notes and vCards.

### ⚡ Optical Transmission Protocol (AirBeam v1)
- **High-Density Chunking**: Automatically breaks arbitrary files (images, documents, notes, vCards) into sequential QR code frames.
- **CRC32 Checksum Integrity**: Every frame contains a CRC32 checksum for corruption detection.
- **Real-Time Progress HUD**: Live visual grid matrix showing collected vs. missing chunks, scanning FPS, and completion status.
- **Single-Device Beam Simulation**: Built-in interactive test mode to test real-time frame streaming and matrix assembly without needing two physical phones.

---

## 🏗️ Technical Architecture

- **UI Framework**: Jetpack Compose with Material 3 (M3) immersive dark palette.
- **Architecture**: MVVM with `ViewModel`, `StateFlow`, and Kotlin Coroutines.
- **Database**: Room Database with TypeConverters for transfer logs and file metadata.
- **CameraX & ZXing**: Live camera stream decoding via CameraX and ZXing barcode engine.
- **Storage**: Local internal storage file provider (`airbeam_shared`).

---

## 📱 Screenshots & Application Flow

1. **Beam Out (Sender)**: Select files or preset samples, toggle E2E encryption, and stream QR frames.
2. **Receive (Scanner)**: Point camera at sender's screen, monitor real-time chunk matrix, and unlock with passphrase.
3. **File Manager**: Organize, search, preview, and delete shared air-gapped files.
4. **AirGap Info**: Details regarding physical security compliance and optical protocol specifications.
