package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransferDirection {
    SENT,
    RECEIVED
}

@Entity(tableName = "transfer_records")
data class TransferRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val direction: TransferDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val totalChunks: Int,
    val previewText: String? = null,
    val localFilePath: String? = null,
    val isEncrypted: Boolean = false,
    val category: String = "OTHER"
)
