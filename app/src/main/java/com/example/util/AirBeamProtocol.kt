package com.example.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class FrameData(
    val sessionId: String,
    val frameIndex: Int,
    val totalFrames: Int,
    val crc32: Long,
    val fileName: String,
    val mimeType: String,
    val isEncrypted: Boolean,
    val isCompressed: Boolean = false,
    val chunkBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FrameData
        return sessionId == other.sessionId && frameIndex == other.frameIndex
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + frameIndex
        return result
    }
}

object AirBeamProtocol {
    private const val PREFIX_V1 = "AB1"
    private const val PREFIX_V2 = "AB2"
    private const val DELIMITER = "~"

    /**
     * Compresses bytes using GZIP if compression reduces size
     */
    fun compressGzip(bytes: ByteArray): ByteArray {
        if (bytes.isEmpty()) return bytes
        return try {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).use { gzip ->
                gzip.write(bytes)
            }
            val compressed = bos.toByteArray()
            if (compressed.size < bytes.size) compressed else bytes
        } catch (e: Exception) {
            bytes
        }
    }

    /**
     * Decompresses bytes if GZIP compressed
     */
    fun decompressGzip(bytes: ByteArray): ByteArray {
        if (bytes.isEmpty()) return bytes
        return try {
            val bis = ByteArrayInputStream(bytes)
            GZIPInputStream(bis).use { gzip ->
                val bos = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (gzip.read(buffer).also { len = it } != -1) {
                    bos.write(buffer, 0, len)
                }
                bos.toByteArray()
            }
        } catch (e: Exception) {
            bytes
        }
    }

    /**
     * Encodes a file byte array into a list of frame strings for QR rendering or Turbo Beaming.
     * Uses automatic GZIP compression and high-density chunk sizes.
     */
    fun encodeToFrames(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        passphrase: String? = null,
        chunkSize: Int = 850,
        enableCompression: Boolean = true
    ): List<String> {
        val cleanFileName = fileName.replace(DELIMITER, "_").replace("|", "_")
        val cleanMimeType = mimeType.replace(DELIMITER, "_").replace("|", "_")
        val sessionId = (System.currentTimeMillis() % 100000).toString()

        val isEncrypted = !passphrase.isNullOrBlank()
        val encryptedOrRawBytes = if (isEncrypted) {
            try {
                CryptoUtil.encrypt(bytes, passphrase!!)
            } catch (e: Exception) {
                bytes
            }
        } else {
            bytes
        }

        // Automatic GZIP compression
        var isCompressed = false
        val finalPayloadBytes = if (enableCompression && encryptedOrRawBytes.size > 200) {
            val gzipBytes = compressGzip(encryptedOrRawBytes)
            if (gzipBytes.size < encryptedOrRawBytes.size) {
                isCompressed = true
                gzipBytes
            } else {
                encryptedOrRawBytes
            }
        } else {
            encryptedOrRawBytes
        }

        val totalBytes = finalPayloadBytes.size
        val totalFrames = if (totalBytes == 0) 1 else (totalBytes + chunkSize - 1) / chunkSize

        val frames = mutableListOf<String>()

        for (i in 0 until totalFrames) {
            val start = i * chunkSize
            val end = kotlin.math.min(start + chunkSize, totalBytes)
            val chunk = if (totalBytes == 0) ByteArray(0) else finalPayloadBytes.copyOfRange(start, end)

            val crc = CRC32()
            crc.update(chunk)
            val crcValue = crc.value

            val base64Data = Base64.encodeToString(chunk, Base64.NO_WRAP)
            val encFlag = if (isEncrypted) "1" else "0"
            val compFlag = if (isCompressed) "1" else "0"

            // Format V2: AB2~sessionId~frameIndex~totalFrames~crc~cleanFileName~cleanMimeType~isEncrypted~isCompressed~base64Data
            val frameString = "$PREFIX_V2$DELIMITER$sessionId$DELIMITER$i$DELIMITER$totalFrames$DELIMITER$crcValue$DELIMITER$cleanFileName$DELIMITER$cleanMimeType$DELIMITER$encFlag$DELIMITER$compFlag$DELIMITER$base64Data"
            frames.add(frameString)
        }

        return frames
    }

    /**
     * Parses a scanned string into FrameData if valid.
     */
    fun parseFrame(text: String): FrameData? {
        if (!text.startsWith(PREFIX_V1) && !text.startsWith(PREFIX_V2)) return null
        return try {
            val parts = text.split(DELIMITER)
            if (parts.size < 8) return null

            val prefix = parts[0]
            val sessionId = parts[1]
            val frameIndex = parts[2].toInt()
            val totalFrames = parts[3].toInt()
            val crc32 = parts[4].toLong()
            val fileName = parts[5]
            val mimeType = parts[6]

            var isEncrypted = false
            var isCompressed = false
            var base64Data = ""

            if (prefix == PREFIX_V2 && parts.size >= 10) {
                isEncrypted = parts[7] == "1"
                isCompressed = parts[8] == "1"
                base64Data = parts[9]
            } else if (parts.size >= 9) {
                isEncrypted = parts[7] == "1"
                base64Data = parts[8]
            } else {
                base64Data = parts[7]
            }

            val chunkBytes = Base64.decode(base64Data, Base64.NO_WRAP)

            // Validate CRC32 checksum
            val verifyCrc = CRC32()
            verifyCrc.update(chunkBytes)
            if (verifyCrc.value != crc32) return null

            FrameData(
                sessionId = sessionId,
                frameIndex = frameIndex,
                totalFrames = totalFrames,
                crc32 = crc32,
                fileName = fileName,
                mimeType = mimeType,
                isEncrypted = isEncrypted,
                isCompressed = isCompressed,
                chunkBytes = chunkBytes
            )
        } catch (e: Exception) {
            null
        }
    }
}

