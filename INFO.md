# 📘 Technical Architecture & Protocol Specification (INFO.md)

This document provides in-depth technical documentation on the internal architecture, protocol specification, cryptographic security model, and performance characteristics of **AirBeam Share**.

---

## 1. System Architecture

AirBeam Share follows modern **MVVM (Model-View-ViewModel)** with unidirection data flow (UDF) powered by Kotlin Coroutines and `StateFlow`.

```
                  ┌─────────────────────────────────────────────────┐
                  │                 Presentation Layer              │
                  │  (Jetpack Compose + Material 3 + Neumorphism)   │
                  └──────────────┬───────────────────▲──────────────┘
                                 │ UI Events         │ StateFlow
                                 ▼                   │
                  ┌──────────────────────────────────┴──────────────┐
                  │                AirBeamViewModel                 │
                  │  - Send Queue & Encoder Orchestrator            │
                  │  - Receive Engine & Frame Assembler             │
                  │  - Realtime Throughput & Progress Calculator    │
                  └──────────────┬───────────────────▲──────────────┘
                                 │                   │
            ┌────────────────────┼───────────────────┼────────────────────┐
            ▼                    ▼                   ▼                    ▼
   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
   │ AirBeamProtocol │  │   CryptoUtil    │  │ FileStorageMgr  │  │ TransferRecord  │
   │ - Frame Chunking│  │ - AES-256-GCM   │  │ - App Sandbox   │  │   Repository    │
   │ - GZIP Engine   │  │ - PBKDF2 HMAC   │  │ - MIME resolver │  │ - Room SQLite   │
   │ - CRC32 Checks  │  │ - IV & Tag Mgmt │  │ - Category Sort │  │ - Reactive Flow │
   └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 2. AirBeam Optical Protocol Specification (V2)

The AirBeam transmission format is designed for high-density, error-resilient visual streaming over animated QR codes.

### 2.1 Packet Format

Each visual frame is encoded as a single string using delimiter-separated fields:

```
AB2~<sessionId>~<frameIndex>~<totalFrames>~<crc32>~<fileName>~<mimeType>~<isEncrypted>~<isCompressed>~<base64Data>
```

### 2.2 Field Descriptions

| Field | Type | Description |
|---|---|---|
| `prefix` | `String` | Protocol version identifier: `AB2` (supports legacy `AB1` backward compatibility). |
| `sessionId` | `String` | Unique 8-character identifier for the current transfer session. |
| `frameIndex` | `Int` | 0-indexed sequence number of the current frame ($0 \le i < N$). |
| `totalFrames` | `Int` | Total number of chunks ($N$) required to reconstruct the complete file. |
| `crc32` | `Long` | CRC-32 checksum calculated over the raw chunk bytes before Base64 encoding. |
| `fileName` | `String` | Sanitized file name with delimiters escaped. |
| `mimeType` | `String` | Standard MIME type (e.g. `image/png`, `application/pdf`, `text/plain`). |
| `isEncrypted` | `Char` | `1` if encrypted with AES-256-GCM, `0` if plaintext. |
| `isCompressed` | `Char` | `1` if compressed using GZIP, `0` if uncompressed. |
| `base64Data` | `String` | Base64 encoded payload of the chunk (without line wraps). |

---

## 3. Cryptography & Security Model

AirBeam provides an authenticated encryption layer using industry-standard primitives:

### 3.1 Encryption Pipeline
1. **Key Derivation (PBKDF2)**:
   - Derives a 256-bit AES key from the user-provided passphrase.
   - Salt: 16 bytes generated via `java.security.SecureRandom`.
   - Iteration Count: 10,000 rounds using `PBKDF2WithHmacSHA256`.
2. **Authenticated Cipher (AES-256-GCM)**:
   - Cipher Mode: `AES/GCM/NoPadding`.
   - Initialization Vector (IV): 12 bytes cryptographically secure random per transfer.
   - Authentication Tag: 128-bit integrity tag appended automatically by GCM.
3. **Payload Structure on Wire**:
   ```
   [16-byte Salt] + [12-byte IV] + [Ciphertext + 16-byte GCM Tag]
   ```
4. **Zero-Knowledge Decryption**:
   - The recipient must enter the exact passphrase.
   - If the key is incorrect or any bit in the stream is corrupted/tampered with, AES-GCM verification immediately fails with `AEADBadTagException`.

---

## 4. High-Throughput Transfer Engine

To handle multi-megabyte files (such as 4.2 MB datasets or large documents), AirBeam implements three performance optimization layers:

### 4.1 Transfer Speed Profiles

| Mode | Target Speed | Chunk Size | Strategy |
|---|---|---|---|
| **⚡ 12 Mbps Turbo** | 12 Mbps | 850–1200 bytes | GZIP compression + optimized QR module matrix |
| **🚀 50 Mbps Ultra** | 50 Mbps | 2000 bytes | Batch frame streaming + memory-buffered pipeline |
| **👁️ Optical Visual** | 0.5 Mbps | 350–400 bytes | Tuned for standard 60 Hz display refresh to camera sensors |

### 4.2 Automated GZIP Compression
- Before chunking, files exceeding 200 bytes pass through an in-memory `GZIPOutputStream`.
- If the compressed byte count is smaller than raw data, `isCompressed=1` is set.
- On the receiver side, the reassembled stream is decompressed via `GZIPInputStream` in a single pass.

### 4.3 Out-of-Order Assembly & CRC Validation
- Chunks arrive in arbitrary order (looping stream).
- A sparse `Map<Int, ByteArray>` buffers incoming frames in memory.
- Duplicate scans are discarded in $O(1)$ time.
- Missing frames are dynamically highlighted in the visual Chunk Matrix HUD.

---

## 5. Room Database Schema

Local history and file records are stored in an encrypted SQLite database via AndroidX Room:

### Table: `transfer_records`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Unique record ID |
| `sessionId` | `TEXT` | `NOT NULL` | Session identifier |
| `fileName` | `TEXT` | `NOT NULL` | Name of the transferred file |
| `fileSize` | `INTEGER` | `NOT NULL` | Size in bytes |
| `mimeType` | `TEXT` | `NOT NULL` | File MIME type |
| `isEncrypted` | `INTEGER` | `NOT NULL` | Boolean flag (1 = encrypted) |
| `direction` | `TEXT` | `NOT NULL` | `SENT` or `RECEIVED` |
| `transferType` | `TEXT` | `NOT NULL` | `TEXT`, `PHOTO`, `FILE`, `DATASET` |
| `localFilePath` | `TEXT` | `NULLABLE` | Local sandbox storage path |
| `previewText` | `TEXT` | `NULLABLE` | Cached text snippet for rapid preview |
| `timestamp` | `INTEGER` | `NOT NULL` | Unix timestamp in milliseconds |

---

## 6. Hardware & Environment Requirements

- **Operating System**: Android 7.0 (API Level 24) or higher.
- **Target OS**: Android 16 (API Level 36).
- **Camera Requirement**: Standard back-facing camera with auto-focus support.
- **Display Requirement**: Screen capable of displaying high-contrast QR matrix codes (minimum recommended resolution: 720p).
