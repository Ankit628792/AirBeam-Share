package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class StoredFileItem(
    val id: Long = 0,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val category: FileCategory,
    val timestamp: Long,
    val isEncrypted: Boolean,
    val previewText: String? = null,
    val fileAbsolutePath: String? = null
) {
    val sizeFormatted: String
        get() = when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> String.format("%.1f KB", fileSize / 1024f)
            else -> String.format("%.1f MB", fileSize / (1024f * 1024f))
        }
}

enum class FileCategory(val displayName: String) {
    ALL("All Files"),
    PHOTO("Photos & Graphics"),
    DOCUMENT("Documents & Notes"),
    CONTACT("Contacts"),
    OTHER("Other Files")
}

object FileStorageManager {

    fun determineCategory(fileName: String, mimeType: String): FileCategory {
        val lowerName = fileName.lowercase()
        val lowerMime = mimeType.lowercase()

        return when {
            lowerMime.startsWith("image") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") -> FileCategory.PHOTO
            lowerMime.contains("vcard") || lowerName.endsWith(".vcf") -> FileCategory.CONTACT
            lowerMime.startsWith("text") || lowerMime.contains("json") || lowerMime.contains("pdf") || lowerName.endsWith(".txt") || lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") -> FileCategory.DOCUMENT
            else -> FileCategory.OTHER
        }
    }

    /**
     * Saves received file bytes into app storage directory
     */
    fun saveFileToStorage(context: Context, fileName: String, bytes: ByteArray): String? {
        return try {
            val storageDir = File(context.filesDir, "airbeam_shared")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val targetFile = File(storageDir, "${System.currentTimeMillis()}_$fileName")
            targetFile.writeBytes(bytes)
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes file from local storage
     */
    fun deleteLocalFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reads file bytes from storage
     */
    fun readFileBytes(path: String?): ByteArray? {
        if (path.isNullOrEmpty()) return null
        return try {
            val file = File(path)
            if (file.exists()) file.readBytes() else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Bundles multiple files into a ZIP byte array
     */
    fun createZipBundle(files: List<Pair<String, ByteArray>>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            files.forEach { (fileName, bytes) ->
                val entry = ZipEntry(fileName)
                zos.putNextEntry(entry)
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    /**
     * Unpacks a ZIP byte array into a list of (fileName, bytes) pairs
     */
    fun unpackZipBundle(zipBytes: ByteArray): List<Pair<String, ByteArray>> {
        val result = mutableListOf<Pair<String, ByteArray>>()
        try {
            ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val name = entry.name
                        val baos = ByteArrayOutputStream()
                        zis.copyTo(baos)
                        result.add(Pair(name, baos.toByteArray()))
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
