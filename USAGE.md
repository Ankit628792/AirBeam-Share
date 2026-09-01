# 📱 User & Operational Guide (USAGE.md)

This comprehensive guide explains how to use **AirBeam Share** for air-gapped optical file transfers between two Android devices or in single-device simulation mode.

---

## 🧭 Navigation Overview

AirBeam features an intuitive 4-tab bottom navigation bar:

1. 📤 **Send**: Select files, photos, datasets, or notes; configure encryption and speed modes; broadcast animated optical QR streams.
2. 📥 **Receive**: Scan optical QR streams via the hardware camera; view live chunk-matrix HUD; enter decryption passphrases.
3. 📁 **Vault (File Manager)**: Search, preview, copy, and manage received files stored locally in the secure sandbox.
4. 📜 **History**: Audit log of all sent and received transfers with timestamps, file sizes, and cryptographic status.

---

## 📤 1. Sending Files (Sender Workflow)

### Step 1: Select or Create Data
- **Sample Presets**: Tap any preset file to test immediately:
  - `4MB_Heavy_Payload_Dataset.bin` (High-speed 10+ Mbps throughput testing)
  - `AirBeam_Hero_Photo.png` (200x200 sample photo)
  - `Mission_AirBeam_Brief.pdf` (Document sample)
  - `AirBeam_Secure_Key_Notes.txt` (Text snippet)
- **Pick Files from Device**: Tap **"Pick Files"** to browse your local device storage (PDFs, APKs, audio, archives, etc.).
- **Quick Text Note**: Write or paste a text message directly into the Quick Note input card.

### Step 2: Choose Transfer Speed Mode
Select one of the 3 built-in transmission profiles:
- ⚡ **12 Mbps High-Speed Turbo**: Recommended for files up to 10 MB. High-density QR matrices with GZIP compression.
- 🚀 **50 Mbps Ultra Pipe**: Maximum throughput with high-density chunking.
- 👁️ **Standard Optical Gap (0.5 Mbps)**: Lower chunk density with standard visual cadence for older camera sensors.

### Step 3: Configure Optional AES-256-GCM Encryption
1. Toggle the **"AES-256 Encryption"** switch.
2. Enter a secure passphrase.
3. AirBeam will encrypt all chunk payloads before generating the QR code stream.

### Step 4: Broadcast Stream
1. Tap **"Start Optical Stream"** or the **Play** icon.
2. Adjust the **FPS Slider** (1 to 24 frames per second) to match the receiver camera's shutter speed.
3. Tap the **Full Screen** icon to maximize the QR code display for easier scanning across larger distances.

---

## 📥 2. Receiving Files (Receiver Workflow)

### Step 1: Grant Camera Access
- When opening the **Receive** screen for the first time, grant Camera permissions when prompted.

### Step 2: Scan the Animated Stream
1. Point your device's camera viewfinder at the sender device's screen.
2. Hold steady at a distance of approximately 15–30 cm (6–12 inches).
3. The **Realtime Progress HUD** will immediately activate:
   - 🟩 **Green blocks**: Successfully captured and verified chunks.
   - ⬜ **Grey blocks**: Missing chunks waiting to be captured on the next cycle.
   - ⚡ **Speed Indicator**: Live transmission throughput (Mbps) and frame rate (FPS).

### Step 3: Decrypt (If Encrypted)
1. Once all chunks are assembled, if the sender enabled encryption, a secure **Decryption Card** appears.
2. Enter the secret passphrase provided by the sender.
3. Tap **"Decrypt & Unlock File"**.

### Step 4: Open, View, and Share
- For text files: View the plain text content instantly in the built-in preview container and tap **"Copy to Clipboard"**.
- For binary/media files: The file is saved to the local app storage and can be shared or exported to other apps.

---

## 🔄 3. Single-Device Beam Simulation Mode

To test and demonstrate high-speed transfers on a single device without needing a second phone or physical camera:

1. Go to the **Send** tab and select any file (e.g. `4MB_Heavy_Payload_Dataset.bin`).
2. Tap the **"⚡ Simulate Loopback"** button on the playback card.
3. Switch over to the **Receive** tab.
4. Watch the frames beam in real-time at **10+ Mbps**, complete CRC checks, decompress GZIP payloads, and save to the vault!

---

## 📁 4. Managing the File Vault

- **Search & Filter**: Filter your transferred files by **All**, **Docs**, **Images**, **Media**, or **Binaries**. Use the search bar to find files by keyword.
- **Inspect Details**: Tap any card to open the **File Details Dialog** showing metadata, hash information, MIME type, and preview snippets.
- **Delete Files**: Remove files from local storage by clicking the trash icon.

---

## 💡 Best Practices for Physical Optical Transfers

1. **Screen Brightness**: Increase the sender device's screen brightness to at least 70% to maximize contrast for the receiver camera.
2. **Reflections & Glare**: Avoid direct overhead lights reflecting off the sender screen.
3. **Framing**: Keep the full QR code within the highlighted viewfinder bounding box.
4. **Frame Rate (FPS)**:
   - Modern devices: Set sender to **12–20 FPS**.
   - Older/entry-level devices: Set sender to **6–10 FPS**.
