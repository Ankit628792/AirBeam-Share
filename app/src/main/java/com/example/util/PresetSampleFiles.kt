package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream

data class PresetFile(
    val id: String,
    val name: String,
    val typeName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val description: String
) {
    val sizeFormatted: String
        get() = "${bytes.size} bytes (${bytes.size / 1024} KB)"
}

object PresetSampleFiles {
    fun getSampleFiles(): List<PresetFile> {
        val sampleTextNote = """
========================================
    AIRBEAM SECURE OPTICAL AIR-GAP NOTE
========================================
Timestamp: ${System.currentTimeMillis()}
Status: Transmitted via 100% Optical QR Stream
Network Mode: OFF (No Internet, No WiFi, No Bluetooth)

Features:
- Encoded using AirBeam Chunk Protocol (AB1)
- Real-time chunk assembly matrix
- Built-in CRC32 checksum verification per frame
- Zero electromagnetic emission risk

"Information wants to be free, but stay off the network!"
========================================
        """.trimIndent()

        val sampleContact = """
BEGIN:VCARD
VERSION:3.0
N:Beam;Agent;;;
FN:Agent AirBeam
ORG:Zero-Network Optical AirGap
TEL;TYPE=CELL:+1-800-AIRBEAM
EMAIL:agent@airbeam.local
NOTE:Transmitted via Light Beams!
END:VCARD
        """.trimIndent()

        // Generate a vibrant sample 200x200 bitmap photo
        val samplePhotoBytes = createSamplePhotoBytes()

        // Generate a 4.2 MB test binary dataset for testing high-speed 10+ Mbps transfers
        val sample4MBDataBytes = ByteArray(4_200_000) { i ->
            (i xor (i shr 3) xor 0x5A).toByte()
        }

        return listOf(
            PresetFile(
                id = "sample_4mb_dataset",
                name = "4MB_Heavy_Payload_Dataset.bin",
                typeName = "4.2 MB Large File",
                mimeType = "application/octet-stream",
                bytes = sample4MBDataBytes,
                description = "4.2 MB large payload for testing 10+ Mbps speed"
            ),
            PresetFile(
                id = "sample_photo",
                name = "AirBeam_Hero_Photo.png",
                typeName = "Photo / Image",
                mimeType = "image/png",
                bytes = samplePhotoBytes,
                description = "High-contrast sample graphic photo"
            ),
            PresetFile(
                id = "sample_note",
                name = "Secure_AirGap_Note.txt",
                typeName = "Text Note",
                mimeType = "text/plain",
                bytes = sampleTextNote.toByteArray(Charsets.UTF_8),
                description = "Encrypted text payload note"
            ),
            PresetFile(
                id = "sample_contact",
                name = "Agent_AirBeam.vcf",
                typeName = "Contact Card",
                mimeType = "text/x-vcard",
                bytes = sampleContact.toByteArray(Charsets.UTF_8),
                description = "vCard contact information"
            )
        )
    }

    private fun createSamplePhotoBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = Color.parseColor("#06B6D4")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, 160f, 160f, paint)

        paint.color = Color.parseColor("#0F172A")
        canvas.drawCircle(80f, 80f, 60f, paint)

        paint.color = Color.parseColor("#10B981")
        canvas.drawCircle(80f, 80f, 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("BEAM", 80f, 88f, paint)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
