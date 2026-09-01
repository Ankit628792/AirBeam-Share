# AirBeam — User Guide & Manual 📖

Welcome to **AirBeam**, your offline optical file sharing and vault manager. Follow this guide to learn how to send files, use end-to-end encryption, receive optical streams, and manage stored files.

---

## 🚀 Quick Start: Sending a File ("Beam Out")

1. Open the **Beam Out** tab on the sender device.
2. Choose content to send:
   - Tap **Choose File** to pick any document, picture, or file from your device storage.
   - Or tap any of the **Sample Presets** (e.g., *Confidential Note*, *Encryption Spec PDF*, *Security Badge Image*, *vCard Contact*).
3. *(Optional)* **Enable E2E Encryption**:
   - Toggle **End-to-End AES-256 Encryption** ON.
   - Use the generated 6-digit PIN or enter your custom passphrase.
   - Share this PIN with the recipient.
4. Point the receiving device's camera at the animated QR code stream playing on the sender screen.

---

## 📥 Receiving an Optical Transfer

1. Open the **Receive** tab on the recipient device.
2. Grant camera permission if prompted.
3. Align the camera scanner box with the QR stream on the sender's screen.
4. Watch the **Real-Time Progress HUD**:
   - Green boxes indicate successfully received frame chunks.
   - The FPS counter displays real-time frame scanning speed.
5. Once all frames are collected:
   - **Unencrypted Files**: Automatically saved to local storage and added to the File Manager.
   - **Encrypted Files**: Enter the sender's PIN/passphrase and tap **Decrypt & Unlock File**.

---

## 🧪 Single-Device Simulation Mode

Want to test the optical transfer process without a second device?

1. Go to the **Beam Out** tab and select any file.
2. Scroll to **Real-Time Transfer Test (Single Device)**.
3. Tap **Simulate Beam & View Progress**.
4. The app will automatically switch to the Receiver screen and feed optical frames in real-time so you can inspect chunk matrix assembly and completion logic!

---

## 📂 File Management System

Access the **File Manager** tab to organize and inspect received and sent files:

- **Categorized Tabs**: Tap *All Files*, *Photos & Graphics*, *Documents & Notes*, *Contacts*, or *Other Files* to filter content.
- **Search Bar**: Type keywords to search through filenames or text previews.
- **File Details & Copy**: Tap the eye icon (👁️) on any item to view details (storage path, CRC integrity, MIME type) and copy text previews directly to your clipboard.
- **Delete Files**: Tap the delete icon (🗑️) to purge files from both local storage disk and transfer history.
